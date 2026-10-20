package transport;

import utils.GitConstants;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Dumb HTTP: reads static files from the server. Only packfiles listed in objects/info/packs are supported;
 * loose objects are not fetched.
 */
final class DumbHttpTransport implements Transport {

    private final HttpFetcher fetcher;
    private final String base;

    DumbHttpTransport(HttpFetcher fetcher, String base) {
        this.fetcher = fetcher;
        this.base = base;
    }

    @Override
    public RemoteRefs discover() throws IOException {
        final String head = text(fetcher.get(base + Protocol.HEAD_PATH)).trim();
        if (!head.startsWith(GitConstants.REF_PREFIX)) {
            return new RemoteRefs(head, null);
        }
        final String branch = head.substring(GitConstants.REF_PREFIX.length());
        for (String line : lines(fetcher.get(base + Protocol.INFO_REFS))) {
            final String[] parts = line.split(GitConstants.TAB, 2);
            if (parts.length == 2 && parts[1].equals(branch)) {
                return new RemoteRefs(parts[0], branch);
            }
        }
        throw new IOException("remote has no ref " + branch);
    }

    @Override
    public List<byte[]> fetchPacks(RemoteRefs refs) throws IOException {
        final List<byte[]> packs = new ArrayList<>();
        for (String line : lines(fetcher.get(base + Protocol.PACKS_LIST))) {
            if (line.startsWith(Protocol.PACK_LINE_PREFIX)) {
                final String packName = line.substring(Protocol.PACK_LINE_PREFIX.length()).trim();
                packs.add(fetcher.get(base + Protocol.PACK_DIR + packName));
            }
        }
        if (packs.isEmpty()) {
            throw new IOException("dumb HTTP remote has no packfiles (loose objects are not supported)");
        }
        return packs;
    }

    private static String[] lines(byte[] body) {
        return text(body).split(GitConstants.NEWLINE);
    }

    private static String text(byte[] body) {
        return new String(body, StandardCharsets.UTF_8);
    }
}
