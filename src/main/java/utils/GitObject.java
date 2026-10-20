package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * A loose git object read from .git/objects.
 *
 * <p>The on-disk format is {@code <type> <size>\0<body>} after zlib decompression. All parsing here
 * happens on the raw {@code byte[]} — the body is only decoded to text by callers that know it is
 * text, because a body may be binary (e.g. the 20-byte raw SHAs inside a tree object).
 */
public class GitObject {

    private final ObjectType type;
    private final byte[] body;

    private GitObject(ObjectType type, byte[] body) {
        this.type = type;
        this.body = body;
    }

    public ObjectType type() {
        return type;
    }

    public byte[] body() {
        return body;
    }

    /** The {@code <type> <size>\0} header that precedes every object's body. */
    public static String header(ObjectType type, int size) {
        return type.value() + GitConstants.SPACE + size + GitConstants.NUL;
    }

    public static GitObject read(Path path) throws IOException {
        final byte[] raw = ZlibDecompressor.decompress(path);
        final int nul = indexOf(raw, GitConstants.NUL_BYTE, 0);
        final String header = new String(raw, 0, nul, StandardCharsets.US_ASCII);
        final int space = header.indexOf(GitConstants.SPACE);
        final ObjectType type = ObjectType.from(header.substring(0, space));
        final int size = Integer.parseInt(header.substring(space + 1));
        final byte[] body = Arrays.copyOfRange(raw, nul + 1, nul + 1 + size);
        return new GitObject(type, body);
    }

    public static int indexOf(byte[] array, byte target, int from) {
        for (int i = from; i < array.length; i++) {
            if (array[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
