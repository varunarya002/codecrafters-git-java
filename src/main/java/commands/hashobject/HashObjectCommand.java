package commands.hashobject;

import commands.Command;
import utils.CommandUtil;
import utils.Sha1;
import utils.ZlibCompressor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class HashObjectCommand implements Command {

    @Override
    public void execute(String[] args) {
        try {
            final byte[] content = Files.readAllBytes(Path.of(args[2]));
            final byte[] blob = blob(content);
            final String hash = Sha1.hexDigest(blob);

            if ("-w".equals(args[1])) {
                writeObject(hash, blob);
            }

            System.out.println(hash);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Hashes and stores an object of the given type, returning its SHA-1 hex digest. */
    public static String write(String type, byte[] content) throws IOException {
        final byte[] object = object(type, content);
        final String hash = Sha1.hexDigest(object);
        writeObject(hash, object);
        return hash;
    }

    private static byte[] blob(byte[] content) throws IOException {
        return object("blob", content);
    }

    private static byte[] object(String type, byte[] content) throws IOException {
        final var out = new ByteArrayOutputStream();
        out.write((type + " " + content.length + "\0").getBytes(StandardCharsets.UTF_8));
        out.write(content);
        return out.toByteArray();
    }

    private static void writeObject(String hash, byte[] blob) throws IOException {
        final Path objectPath = CommandUtil.extractObjectPath(hash);
        if (Files.exists(objectPath)) {
            // Same content, same name: git keeps the existing (read-only) file.
            return;
        }
        Files.createDirectories(objectPath.getParent());
        Files.write(objectPath, ZlibCompressor.compress(blob));
    }
}
