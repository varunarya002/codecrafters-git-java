package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class Sha1 {

    /** Length of a raw SHA-1 digest in bytes, as stored in trees, packs and indexes. */
    public static final int RAW_LENGTH = 20;
    /** Length of a SHA-1 digest written as hex. */
    public static final int HEX_LENGTH = RAW_LENGTH * 2;

    private static final String ALGORITHM = "SHA-1";
    private static final String HEX_PATTERN = "[0-9a-fA-F]{" + HEX_LENGTH + "}";

    public static void validateHexDigest(String hash) {
        if (hash == null || !hash.matches(HEX_PATTERN)) {
            throw new IllegalArgumentException("Invalid SHA-1 hash: " + hash);
        }
    }

    public static String hexDigest(byte[] data) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance(ALGORITHM).digest(data));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
