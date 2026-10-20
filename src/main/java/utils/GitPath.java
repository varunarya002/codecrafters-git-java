package utils;

import java.nio.file.Path;

public enum GitPath {
    ROOT(""),
    OBJECTS("objects"),
    PACK("objects/pack"),
    REFS("refs"),
    HEADS("refs/heads"),
    HEAD("HEAD");

    private static final String GIT_DIR = ".git";
    private static final String SEPARATOR = "/";
    private final String subpath;
    private final String path;

    GitPath(String subpath) {
        this.subpath = subpath;
        this.path = subpath.isEmpty() ? GIT_DIR : GIT_DIR + SEPARATOR + subpath;
    }

    public Path resolve(String... parts) {
        return Path.of(path, parts);
    }

    /** Resolves this path under {@code root} instead of the current directory, e.g. a clone target. */
    public Path resolveIn(Path root, String... parts) {
        return root.resolve(resolve(parts));
    }

    /** The ref name for {@code name} under this path, e.g. {@code HEADS.refName("main")} is {@code refs/heads/main}. */
    public String refName(String name) {
        return subpath + SEPARATOR + name;
    }

    public String value() {
        return path;
    }
}
