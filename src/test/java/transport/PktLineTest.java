package transport;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PktLineTest {

    @Test
    void encodesLengthIncludingHeader() {
        assertEquals("0009done\n", new String(PktLine.encode("done\n"), StandardCharsets.US_ASCII));
    }

    @Test
    void readsPayloadsAndFlushPackets() throws IOException {
        final byte[] data = concat(PktLine.encode("first\n"), PktLine.FLUSH.getBytes(StandardCharsets.US_ASCII),
                PktLine.encode("second"));
        final var reader = new PktLine.Reader(data, 0);

        assertEquals("first", reader.next());
        assertNull(reader.next());
        assertEquals("second", reader.next());
    }

    private static byte[] concat(byte[]... parts) {
        final var out = new java.io.ByteArrayOutputStream();
        for (byte[] part : parts) {
            out.writeBytes(part);
        }
        return out.toByteArray();
    }
}
