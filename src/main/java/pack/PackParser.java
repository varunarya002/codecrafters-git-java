package pack;

import utils.ObjectType;
import utils.ObjectWriter;
import utils.Sha1;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.CRC32;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/** Parses a packfile (version 2 or 3) and resolves its OFS and REF deltas. */
public final class PackParser {

    public record Result(String checksum, List<IndexEntry> index, Map<String, PackedObject> objects) {
    }

    /** The magic bytes every packfile starts with. */
    public static final String SIGNATURE = "PACK";

    private static final int VERSION_OFFSET = 4;
    private static final int COUNT_OFFSET = 8;
    private static final int HEADER_LENGTH = 12;
    private static final int MIN_LENGTH = HEADER_LENGTH + Sha1.RAW_LENGTH;

    private static final int TYPE_SHIFT = 4;
    private static final int TYPE_MASK = 0x7;
    private static final int FIRST_SIZE_MASK = 0xf;
    private static final int FIRST_SIZE_BITS = 4;

    private static final int COMMIT = 1;
    private static final int TREE = 2;
    private static final int BLOB = 3;
    private static final int TAG = 4;
    private static final int OFS_DELTA = 6;
    private static final int REF_DELTA = 7;

    private static final int INFLATE_BUFFER_SIZE = 8192;

    private PackParser() {
    }

    public static boolean startsWithSignature(byte[] data, int pos) {
        return pos + SIGNATURE.length() <= data.length
                && new String(data, pos, SIGNATURE.length(), StandardCharsets.US_ASCII).equals(SIGNATURE);
    }

    public static Result parse(byte[] data) throws IOException {
        if (data.length < MIN_LENGTH || !startsWithSignature(data, 0)) {
            throw new IOException("not a packfile");
        }
        final int version = ByteBuffer.wrap(data, VERSION_OFFSET, Integer.BYTES).getInt();
        if (version != 2 && version != 3) {
            throw new IOException("unsupported pack version " + version);
        }
        final int count = ByteBuffer.wrap(data, COUNT_OFFSET, Integer.BYTES).getInt();

        final int end = data.length - Sha1.RAW_LENGTH;
        final String checksum = HexFormat.of().formatHex(data, end, data.length);
        if (!Sha1.hexDigest(Arrays.copyOfRange(data, 0, end)).equals(checksum)) {
            throw new IOException("packfile checksum mismatch");
        }

        final List<RawEntry> entries = readEntries(data, count, end);
        final Map<Long, PackedObject> byOffset = new HashMap<>();
        final Map<String, PackedObject> bySha = new LinkedHashMap<>();
        final List<IndexEntry> index = new ArrayList<>();
        final List<RawEntry> deltas = new ArrayList<>();

        for (RawEntry entry : entries) {
            if (entry.type() == OFS_DELTA || entry.type() == REF_DELTA) {
                deltas.add(entry);
                continue;
            }
            final PackedObject object = store(toObjectType(entry.type()), entry.body());
            byOffset.put(entry.offset(), object);
            bySha.put(object.sha(), object);
            index.add(new IndexEntry(object.sha(), entry.crc(), entry.offset()));
        }

        // A delta's base may come later in the pack, so keep retrying until nothing more resolves.
        while (!deltas.isEmpty()) {
            boolean progress = false;
            for (Iterator<RawEntry> it = deltas.iterator(); it.hasNext(); ) {
                final RawEntry delta = it.next();
                final PackedObject base = delta.type() == OFS_DELTA
                        ? byOffset.get(delta.baseOffset())
                        : bySha.get(delta.baseSha());
                if (base == null) {
                    continue;
                }
                final PackedObject object = store(base.type(), DeltaApplier.apply(base.body(), delta.body()));
                byOffset.put(delta.offset(), object);
                bySha.put(object.sha(), object);
                index.add(new IndexEntry(object.sha(), delta.crc(), delta.offset()));
                it.remove();
                progress = true;
            }
            if (!progress) {
                throw new IOException("packfile has " + deltas.size() + " deltas with missing bases");
            }
        }
        return new Result(checksum, index, bySha);
    }

    private static List<RawEntry> readEntries(byte[] data, int count, int end) throws IOException {
        final List<RawEntry> entries = new ArrayList<>(count);
        int pos = HEADER_LENGTH;
        for (int i = 0; i < count; i++) {
            final long start = pos;
            // Entry header: type in bits 4-6 of the first byte, size as a varint starting with its low 4 bits.
            int c = data[pos++] & 0xff;
            final int type = (c >> TYPE_SHIFT) & TYPE_MASK;
            long size = c & FIRST_SIZE_MASK;
            int shift = FIRST_SIZE_BITS;
            while (VarInt.hasMore(c)) {
                c = data[pos++] & 0xff;
                size |= (long) (c & VarInt.PAYLOAD_MASK) << shift;
                shift += VarInt.PAYLOAD_BITS;
            }

            long baseOffset = -1;
            String baseSha = null;
            if (type == OFS_DELTA) {
                // Big-endian varint where each continuation adds one, so encodings never overlap.
                c = data[pos++] & 0xff;
                long distance = c & VarInt.PAYLOAD_MASK;
                while (VarInt.hasMore(c)) {
                    c = data[pos++] & 0xff;
                    distance = ((distance + 1) << VarInt.PAYLOAD_BITS) | (c & VarInt.PAYLOAD_MASK);
                }
                baseOffset = start - distance;
            } else if (type == REF_DELTA) {
                baseSha = HexFormat.of().formatHex(data, pos, pos + Sha1.RAW_LENGTH);
                pos += Sha1.RAW_LENGTH;
            }

            final Inflated inflated = inflate(data, pos, end, size);
            pos += inflated.consumed();
            final CRC32 crc = new CRC32();
            crc.update(data, (int) start, pos - (int) start);
            entries.add(new RawEntry(start, type, baseOffset, baseSha, inflated.body(), crc.getValue()));
        }
        if (pos != end) {
            throw new IOException("packfile has " + (end - pos) + " unexpected trailing bytes");
        }
        return entries;
    }

    private static Inflated inflate(byte[] data, int from, int end, long expectedSize) throws IOException {
        final Inflater inflater = new Inflater();
        try {
            inflater.setInput(data, from, end - from);
            final var body = new ByteArrayOutputStream();
            final byte[] buffer = new byte[INFLATE_BUFFER_SIZE];
            while (!inflater.finished()) {
                final int n = inflater.inflate(buffer);
                if (n == 0 && inflater.needsInput()) {
                    throw new IOException("truncated packfile entry");
                }
                body.write(buffer, 0, n);
            }
            if (body.size() != expectedSize) {
                throw new IOException("packfile entry size mismatch");
            }
            return new Inflated(body.toByteArray(), (end - from) - inflater.getRemaining());
        } catch (DataFormatException e) {
            throw new IOException("corrupt packfile entry", e);
        } finally {
            inflater.end();
        }
    }

    private static PackedObject store(ObjectType type, byte[] body) throws IOException {
        return new PackedObject(ObjectWriter.hash(type, body), type, body);
    }

    private static ObjectType toObjectType(int type) throws IOException {
        return switch (type) {
            case COMMIT -> ObjectType.COMMIT;
            case TREE -> ObjectType.TREE;
            case BLOB -> ObjectType.BLOB;
            case TAG -> ObjectType.TAG;
            default -> throw new IOException("unsupported packed object type " + type);
        };
    }

    private record RawEntry(long offset, int type, long baseOffset, String baseSha, byte[] body, long crc) {
    }

    private record Inflated(byte[] body, int consumed) {
    }
}
