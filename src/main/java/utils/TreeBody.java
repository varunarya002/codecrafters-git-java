package utils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class TreeBody {

    private static final int SHA_LENGTH = 20;

    private TreeBody() {
    }

    /** One tree entry: a mode, a name, and the raw 20-byte SHA of the object it points to. */
    public record Entry(String mode, String name, byte[] sha) {
    }

    public static List<Entry> parse(byte[] body) {
        final List<Entry> entries = new ArrayList<>();
        int offset = 0;
        while (offset < body.length) {
            final int space = GitObject.indexOf(body, (byte) ' ', offset);
            final int nul = space < 0 ? -1 : GitObject.indexOf(body, (byte) 0, space + 1);
            if (space < 0 || nul < 0 || body.length - nul - 1 < SHA_LENGTH) {
                throw new IllegalArgumentException("Malformed tree object");
            }

            final String mode = new String(body, offset, space - offset, StandardCharsets.US_ASCII);
            final String name = new String(body, space + 1, nul - space - 1, StandardCharsets.UTF_8);
            final byte[] sha = Arrays.copyOfRange(body, nul + 1, nul + 1 + SHA_LENGTH);
            entries.add(new Entry(mode, name, sha));
            offset = nul + 1 + SHA_LENGTH;
        }
        return entries;
    }
}
