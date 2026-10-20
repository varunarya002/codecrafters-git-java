package utils;

/** Separators and file names shared across commands; format values owned by one domain live with that domain. */
public final class GitConstants {

    public static final String NEWLINE = "\n";
    public static final String SPACE = " ";
    public static final String TAB = "\t";
    public static final String NUL = "\0";
    public static final byte SPACE_BYTE = ' ';
    public static final byte NUL_BYTE = 0;

    public static final String REF_PREFIX = "ref: ";
    public static final String DEFAULT_BRANCH = "main";

    public static final String PACK_PREFIX = "pack-";
    public static final String PACK_EXTENSION = ".pack";
    public static final String INDEX_EXTENSION = ".idx";

    private GitConstants() {
    }
}
