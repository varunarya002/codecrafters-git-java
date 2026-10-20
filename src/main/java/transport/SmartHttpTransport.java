package transport;

import pack.PackParser;
import utils.GitConstants;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/** Smart HTTP: ref advertisement via GET info/refs, then a pack negotiated via POST git-upload-pack. */
final class SmartHttpTransport implements Transport {

    /** The first pkt-line of a smart ref advertisement, length prefix included. */
    static final String SERVICE_HEADER = new String(
            PktLine.encode(Protocol.SERVICE_ANNOUNCEMENT + GitConstants.NEWLINE), StandardCharsets.US_ASCII);

    private final HttpFetcher fetcher;
    private final String base;
    private final byte[] advertisement;

    SmartHttpTransport(HttpFetcher fetcher, String base, byte[] advertisement) {
        this.fetcher = fetcher;
        this.base = base;
        this.advertisement = advertisement;
    }

    @Override
    public RemoteRefs discover() throws IOException {
        final var reader = new PktLine.Reader(advertisement, 0);
        if (!Protocol.SERVICE_ANNOUNCEMENT.equals(reader.next()) || reader.next() != null) {
            throw new IOException("malformed smart HTTP ref advertisement");
        }

        String headSha = null;
        String headBranch = null;
        String line;
        while ((line = reader.next()) != null) {
            final int nul = line.indexOf(GitConstants.NUL);
            final String ref = nul < 0 ? line : line.substring(0, nul);
            if (nul >= 0 && headSha == null) {
                headBranch = symrefTarget(line.substring(nul + 1));
            }
            final String[] parts = ref.split(GitConstants.SPACE, 2);
            if (parts.length == 2 && Protocol.HEAD.equals(parts[1])) {
                headSha = parts[0];
            }
        }
        if (headSha == null) {
            throw new IOException("remote advertised no HEAD");
        }
        return new RemoteRefs(headSha, headBranch);
    }

    @Override
    public List<byte[]> fetchPacks(RemoteRefs refs) throws IOException {
        final var request = new ByteArrayOutputStream();
        request.writeBytes(PktLine.encode(Protocol.WANT + GitConstants.SPACE + refs.headSha() + GitConstants.NEWLINE));
        request.writeBytes(PktLine.FLUSH.getBytes(StandardCharsets.US_ASCII));
        request.writeBytes(PktLine.encode(Protocol.DONE + GitConstants.NEWLINE));
        final byte[] response = fetcher.post(base + Protocol.UPLOAD_PACK_PATH, request.toByteArray(),
                Protocol.UPLOAD_PACK_REQUEST_TYPE);
        return List.of(packAfterNegotiation(response));
    }

    /** Skips the NAK/ACK pkt-lines in front of the packfile; the pack itself is sent raw without side-band. */
    private static byte[] packAfterNegotiation(byte[] response) throws IOException {
        final var reader = new PktLine.Reader(response, 0);
        while (!PackParser.startsWithSignature(response, reader.position())) {
            if (reader.position() >= response.length) {
                throw new IOException("server response contained no packfile");
            }
            reader.next();
        }
        return Arrays.copyOfRange(response, reader.position(), response.length);
    }

    private static String symrefTarget(String capabilities) {
        for (String capability : capabilities.split(GitConstants.SPACE)) {
            if (capability.startsWith(Protocol.SYMREF_HEAD)) {
                return capability.substring(Protocol.SYMREF_HEAD.length());
            }
        }
        return null;
    }
}
