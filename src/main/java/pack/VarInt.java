package pack;

/** Base-128 variable-length integers: 7 payload bits per byte, high bit set when another byte follows. */
final class VarInt {

    static final int CONTINUATION = 0x80;
    static final int PAYLOAD_MASK = 0x7f;
    static final int PAYLOAD_BITS = 7;

    private VarInt() {
    }

    static boolean hasMore(int b) {
        return (b & CONTINUATION) != 0;
    }

    /** Reads a little-endian varint (least significant group first), advancing {@code pos[0]}. */
    static long readLittleEndian(byte[] data, int[] pos) {
        long value = 0;
        int shift = 0;
        int b;
        do {
            b = data[pos[0]++] & 0xff;
            value |= (long) (b & PAYLOAD_MASK) << shift;
            shift += PAYLOAD_BITS;
        } while (hasMore(b));
        return value;
    }
}
