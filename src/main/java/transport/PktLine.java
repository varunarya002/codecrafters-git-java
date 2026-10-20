package transport;

import java.io.IOException;
import utils.GitConstants;

import java.nio.charset.StandardCharsets;

/** Git pkt-line framing: a 4-hex-digit length (including itself) followed by the payload. */
public final class PktLine {

    public static final String FLUSH = "0000";

    private PktLine() {
    }

    public static byte[] encode(String payload) {
        final byte[] body = payload.getBytes(StandardCharsets.UTF_8);
        final String header = String.format("%04x", body.length + 4);
        final byte[] out = new byte[body.length + 4];
        System.arraycopy(header.getBytes(StandardCharsets.US_ASCII), 0, out, 0, 4);
        System.arraycopy(body, 0, out, 4, body.length);
        return out;
    }

    public static final class Reader {

        private final byte[] data;
        private int pos;

        public Reader(byte[] data, int pos) {
            this.data = data;
            this.pos = pos;
        }

        public int position() {
            return pos;
        }

        /** Returns the next payload without its trailing newline, or {@code null} for a flush packet. */
        public String next() throws IOException {
            if (pos + 4 > data.length) {
                throw new IOException("truncated pkt-line header at offset " + pos);
            }
            final int length = Integer.parseInt(new String(data, pos, 4, StandardCharsets.US_ASCII), 16);
            pos += 4;
            if (length == 0) {
                return null;
            }
            if (length < 4 || pos + length - 4 > data.length) {
                throw new IOException("invalid pkt-line length " + length);
            }
            final String payload = new String(data, pos, length - 4, StandardCharsets.UTF_8);
            pos += length - 4;
            return payload.endsWith(GitConstants.NEWLINE)
                    ? payload.substring(0, payload.length() - GitConstants.NEWLINE.length())
                    : payload;
        }
    }
}
