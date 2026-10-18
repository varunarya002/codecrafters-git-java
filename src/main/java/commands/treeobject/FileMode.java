package commands.treeobject;

import utils.ObjectType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;

enum FileMode {
    REGULAR("100644", ObjectType.BLOB),
    EXECUTABLE("100755", ObjectType.BLOB),
    DIRECTORY("40000", ObjectType.TREE),
    GITLINK("160000", ObjectType.COMMIT);

    private static final int PADDED_WIDTH = 6;

    private final String value;
    private final ObjectType type;

    FileMode(String value, ObjectType type) {
        this.value = value;
        this.type = type;
    }

    String value() {
        return value;
    }

    ObjectType type() {
        return type;
    }

    String paddedValue() {
        return "0".repeat(PADDED_WIDTH - value.length()) + value;
    }

    static FileMode from(String mode) {
        return switch (mode) {
            case "100644" -> REGULAR;
            case "100755" -> EXECUTABLE;
            case "40000", "040000" -> DIRECTORY;
            case "160000" -> GITLINK;
            default -> throw new IllegalArgumentException("Unsupported file mode: " + mode);
        };
    }

    /** Git marks a file executable when its owner execute bit is set. */
    static FileMode of(Path regularFile) throws IOException {
        return Files.getPosixFilePermissions(regularFile).contains(PosixFilePermission.OWNER_EXECUTE)
                ? EXECUTABLE
                : REGULAR;
    }
}
