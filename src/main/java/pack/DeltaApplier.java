package pack;

import java.io.IOException;

/** Applies a git delta (copy and insert instructions) to a base object. */
public final class DeltaApplier {

    /** High bit set: copy from the base. Clear: insert the next {@code opcode} literal bytes. */
    private static final int COPY_OPCODE = 0x80;
    private static final int OFFSET_BYTES = 4;
    private static final int SIZE_BYTES = 3;
    private static final int FIRST_SIZE_FLAG = 0x10;
    /** A copy whose size bytes are all absent means 64 KiB. */
    private static final int DEFAULT_COPY_SIZE = 0x10000;

    private DeltaApplier() {
    }

    public static byte[] apply(byte[] base, byte[] delta) throws IOException {
        final int[] pos = {0};
        final long sourceSize = VarInt.readLittleEndian(delta, pos);
        final long targetSize = VarInt.readLittleEndian(delta, pos);
        if (sourceSize != base.length) {
            throw new IOException("delta base size mismatch: expected " + sourceSize + ", got " + base.length);
        }

        final byte[] out = new byte[(int) targetSize];
        int outPos = 0;
        while (pos[0] < delta.length) {
            final int opcode = delta[pos[0]++] & 0xff;
            if ((opcode & COPY_OPCODE) != 0) {
                final int offset = readCopyField(delta, pos, opcode, 1, OFFSET_BYTES);
                int size = readCopyField(delta, pos, opcode, FIRST_SIZE_FLAG, SIZE_BYTES);
                if (size == 0) {
                    size = DEFAULT_COPY_SIZE;
                }
                System.arraycopy(base, offset, out, outPos, size);
                outPos += size;
            } else if (opcode != 0) {
                System.arraycopy(delta, pos[0], out, outPos, opcode);
                pos[0] += opcode;
                outPos += opcode;
            } else {
                throw new IOException("delta opcode 0 is reserved");
            }
        }
        if (outPos != targetSize) {
            throw new IOException("delta produced " + outPos + " bytes, expected " + targetSize);
        }
        return out;
    }

    /** Reads a little-endian field whose bytes are present only where the opcode's flag bits are set. */
    private static int readCopyField(byte[] delta, int[] pos, int opcode, int firstFlag, int byteCount) {
        int value = 0;
        for (int i = 0; i < byteCount; i++) {
            if ((opcode & (firstFlag << i)) != 0) {
                value |= (delta[pos[0]++] & 0xff) << (Byte.SIZE * i);
            }
        }
        return value;
    }
}
