package utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.InflaterInputStream;

public class ZlibDecompressor {

    public static byte[] decompress(Path path) throws IOException {
        try (var in = new InflaterInputStream(new BufferedInputStream(Files.newInputStream(path)))) {
            return in.readAllBytes();
        }
    }

    public static String getContent(Path path) throws IOException {
        return new String(decompress(path));
    }
}
