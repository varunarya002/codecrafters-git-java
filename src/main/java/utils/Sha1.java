package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Sha1 {

    public static void validateHexDigest(String hash) {
        if (hash == null || !hash.matches("[0-9a-fA-F]{40}")) {
            throw new IllegalArgumentException("Invalid SHA-1 hash: " + hash);
        }
    }

    public static String hexDigest(byte[] data) {
        try {
            final byte[] digest = MessageDigest.getInstance("SHA-1").digest(data);
            final StringBuilder hex = new StringBuilder(digest.length * 2);
            for (byte b : digest) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
