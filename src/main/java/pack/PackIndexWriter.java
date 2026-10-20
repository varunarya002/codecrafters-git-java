package pack;

import utils.Sha1;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;

/** Writes a version 2 pack index (.idx). */
public final class PackIndexWriter {

    private static final byte[] SIGNATURE = {(byte) 0xff, 't', 'O', 'c'};
    private static final int VERSION = 2;
    /** One cumulative count per possible first byte of a SHA. */
    private static final int FANOUT_SIZE = 256;
    /** Offsets at or above this need the 64-bit offset table, which is not implemented. */
    private static final long MAX_SMALL_OFFSET = 0x80000000L;

    private PackIndexWriter() {
    }

    public static byte[] write(List<IndexEntry> entries, String packChecksum) throws IOException {
        final List<IndexEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(IndexEntry::sha))
                .toList();

        final var bytes = new ByteArrayOutputStream();
        final var out = new DataOutputStream(bytes);
        out.write(SIGNATURE);
        out.writeInt(VERSION);

        final int[] fanout = new int[FANOUT_SIZE];
        for (IndexEntry entry : sorted) {
            fanout[HexFormat.fromHexDigits(entry.sha(), 0, 2)]++;
        }
        int total = 0;
        for (int count : fanout) {
            total += count;
            out.writeInt(total);
        }

        for (IndexEntry entry : sorted) {
            out.write(HexFormat.of().parseHex(entry.sha()));
        }
        for (IndexEntry entry : sorted) {
            out.writeInt((int) entry.crc());
        }
        for (IndexEntry entry : sorted) {
            if (entry.offset() >= MAX_SMALL_OFFSET) {
                throw new IOException("packfile offsets above 2 GiB are not supported");
            }
            out.writeInt((int) entry.offset());
        }
        out.write(HexFormat.of().parseHex(packChecksum));
        out.flush();

        final byte[] content = bytes.toByteArray();
        final byte[] trailer = HexFormat.of().parseHex(Sha1.hexDigest(content));
        final byte[] index = new byte[content.length + trailer.length];
        System.arraycopy(content, 0, index, 0, content.length);
        System.arraycopy(trailer, 0, index, content.length, trailer.length);
        return index;
    }
}
