package transport;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/** Fetches a repository's refs and packfiles over HTTP. */
public interface Transport {

    /** Picks smart HTTP when the server speaks it, otherwise falls back to dumb HTTP. */
    static Transport open(String url) throws IOException {
        final String base = url.endsWith(Protocol.URL_SEPARATOR)
                ? url.substring(0, url.length() - Protocol.URL_SEPARATOR.length())
                : url;
        final var fetcher = new HttpFetcher();
        final byte[] advertisement = fetcher.get(base + Protocol.SMART_INFO_REFS);
        final String header = SmartHttpTransport.SERVICE_HEADER;
        if (new String(advertisement, 0, Math.min(advertisement.length, header.length()), StandardCharsets.US_ASCII)
                .equals(header)) {
            return new SmartHttpTransport(fetcher, base, advertisement);
        }
        return new DumbHttpTransport(fetcher, base);
    }

    RemoteRefs discover() throws IOException;

    /** Returns raw packfile bytes that together contain every object reachable from the HEAD commit. */
    List<byte[]> fetchPacks(RemoteRefs refs) throws IOException;
}
