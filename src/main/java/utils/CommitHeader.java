package utils;

/** The header lines at the top of a commit body, each written as {@code <key> <value>\n}. */
public enum CommitHeader {
    TREE("tree"),
    PARENT("parent"),
    AUTHOR("author"),
    COMMITTER("committer");

    private final String key;

    CommitHeader(String key) {
        this.key = key;
    }

    public String prefix() {
        return key + GitConstants.SPACE;
    }

    public String line(String value) {
        return prefix() + value + GitConstants.NEWLINE;
    }
}
