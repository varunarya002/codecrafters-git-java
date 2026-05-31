package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;

public class ZlibCompressor {

    public static byte[] compress(byte[] data) {
        final var out = new ByteArrayOutputStream();
        try (var deflater = new DeflaterOutputStream(out)) {
            deflater.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return out.toByteArray();
    }
}
