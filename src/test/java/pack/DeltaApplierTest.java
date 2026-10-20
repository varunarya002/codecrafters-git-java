package pack;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeltaApplierTest {

    @Test
    void copiesFromBaseAndInsertsLiteralBytes() throws IOException {
        final byte[] base = "hello world".getBytes(StandardCharsets.US_ASCII);
        final byte[] delta = bytes(
                11, 8,              // source size 11, target size 8
                0x90, 5,            // copy: offset 0, size 5 -> "hello"
                0x03, 'a', 'b', ' ' // insert 3 literal bytes -> "ab "
        );

        assertArrayEquals("helloab ".getBytes(StandardCharsets.US_ASCII), DeltaApplier.apply(base, delta));
    }

    @Test
    void copyWithZeroSizeMeansSixtyFourKiB() throws IOException {
        final byte[] base = new byte[0x10000];
        for (int i = 0; i < base.length; i++) {
            base[i] = (byte) i;
        }
        // target size 0x10000 as varint: 0x80 0x80 0x04
        final byte[] delta = bytes(0x80, 0x80, 0x04, 0x80, 0x80, 0x04, 0x80);
        assertArrayEquals(base, DeltaApplier.apply(base, delta));
    }

    @Test
    void rejectsBaseSizeMismatch() {
        final byte[] delta = bytes(4, 1, 0x01, 'x');
        assertThrows(IOException.class, () -> DeltaApplier.apply(new byte[3], delta));
    }

    private static byte[] bytes(int... values) {
        final byte[] out = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            out[i] = (byte) values[i];
        }
        return out;
    }
}
