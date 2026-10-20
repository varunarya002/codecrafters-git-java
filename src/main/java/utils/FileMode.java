package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;

public enum FileMode {
    REGULAR("100644", ObjectType.BLOB),
    EXECUTABLE("100755", ObjectType.BLOB),
    SYMLINK("120000", ObjectType.BLOB),
    DIRECTORY("40000", ObjectType.TREE),
    GITLINK("160000", ObjectType.COMMIT);

    private static final int PADDED_WIDTH = 6;
    private static final String PAD = "0";

    private final String value;
    private final ObjectType type;

    FileMode(String value, ObjectType type) {
        this.value = value;
        this.type = type;
    }

    public String value() {
        return value;
    }

    public ObjectType type() {
        return type;
    }

    public String paddedValue() {
        return PAD.repeat(PADDED_WIDTH - value.length()) + value;
    }

    /** Accepts both the stored form ({@code 40000}) and the padded form ({@code 040000}). */
    public static FileMode from(String mode) {
        return Arrays.stream(values())
                .filter(m -> m.value.equals(mode) || m.paddedValue().equals(mode))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported file mode: " + mode));
    }

    /** Git marks a file executable when its owner execute bit is set. */
    public static FileMode of(Path regularFile) throws IOException {
        return Files.getPosixFilePermissions(regularFile).contains(PosixFilePermission.OWNER_EXECUTE)
                ? EXECUTABLE
                : REGULAR;
    }
}
