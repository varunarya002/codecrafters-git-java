package commands.treeobject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;

enum FileMode {
    REGULAR("100644"),
    EXECUTABLE("100755"),
    DIRECTORY("40000");

    private final String value;

    FileMode(String value) {
        this.value = value;
    }

    String value() {
        return value;
    }

    /** Git marks a file executable when its owner execute bit is set. */
    static FileMode of(Path regularFile) throws IOException {
        return Files.getPosixFilePermissions(regularFile).contains(PosixFilePermission.OWNER_EXECUTE)
                ? EXECUTABLE
                : REGULAR;
    }
}
