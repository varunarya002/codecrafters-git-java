package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class ObjectWriter {

    /** Returns the SHA-1 hex digest of the object without storing it. */
    public static String hash(ObjectType type, byte[] content) throws IOException {
        return Sha1.hexDigest(object(type, content));
    }

    /** Hashes the object and stores it under .git/objects, returning its SHA-1 hex digest. */
    public static String write(ObjectType type, byte[] content) throws IOException {
        final byte[] object = object(type, content);
        final String hash = Sha1.hexDigest(object);
        writeObject(hash, object);
        return hash;
    }

    private static byte[] object(ObjectType type, byte[] content) throws IOException {
        final var out = new ByteArrayOutputStream();
        out.write(GitObject.header(type, content.length).getBytes(StandardCharsets.UTF_8));
        out.write(content);
        return out.toByteArray();
    }

    private static void writeObject(String hash, byte[] object) throws IOException {
        final Path objectPath = CommandUtil.extractObjectPath(hash);
        if (Files.exists(objectPath)) {
            // Same content, same name: git keeps the existing (read-only) file.
            return;
        }
        Files.createDirectories(objectPath.getParent());
        Files.write(objectPath, ZlibCompressor.compress(object));
    }
}
