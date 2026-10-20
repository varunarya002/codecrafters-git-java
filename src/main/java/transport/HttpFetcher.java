package transport;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

final class HttpFetcher {

    private final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    byte[] get(String url) throws IOException {
        final var request = HttpRequest.newBuilder(URI.create(url)).GET().build();
        return send(request, url);
    }

    byte[] post(String url, byte[] body, String contentType) throws IOException {
        final var request = HttpRequest.newBuilder(URI.create(url))
                .header("Content-Type", contentType)
                .POST(HttpRequest.BodyPublishers.ofByteArray(body))
                .build();
        return send(request, url);
    }

    private byte[] send(HttpRequest request, String url) throws IOException {
        final HttpResponse<byte[]> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("interrupted while fetching " + url, e);
        }
        if (response.statusCode() != 200) {
            throw new IOException("GET or POST " + url + " failed with HTTP " + response.statusCode());
        }
        return response.body();
    }
}
