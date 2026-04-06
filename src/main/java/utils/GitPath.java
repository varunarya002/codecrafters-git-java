package utils;

import java.nio.file.Path;

public enum GitPath {
    ROOT(""),
    OBJECTS("objects"),
    REFS("refs"),
    HEAD("HEAD");

    private static final String GIT_DIR = ".git";
    private final String path;

    GitPath(String subpath) {
        this.path = subpath.isEmpty() ? GIT_DIR : GIT_DIR + "/" + subpath;
    }

    public Path resolve(String... parts) {
        return Path.of(path, parts);
    }

    public String value() {
        return path;
    }
}
