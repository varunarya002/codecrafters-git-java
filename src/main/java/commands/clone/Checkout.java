package commands.clone;

import pack.PackedObject;
import utils.CommitHeader;
import utils.FileMode;
import utils.GitConstants;
import utils.ObjectType;
import utils.TreeBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HexFormat;
import java.util.Map;

/** Writes the tree of a commit into a working directory. */
final class Checkout {

    private final Map<String, PackedObject> objects;

    Checkout(Map<String, PackedObject> objects) {
        this.objects = objects;
    }

    void checkout(String commitSha, Path root) throws IOException {
        final String body = new String(require(commitSha, ObjectType.COMMIT).body(), StandardCharsets.UTF_8);
        final String treePrefix = CommitHeader.TREE.prefix();
        if (!body.startsWith(treePrefix)) {
            throw new IOException("commit " + commitSha + " has no tree");
        }
        writeTree(body.substring(treePrefix.length(), body.indexOf(GitConstants.NEWLINE)), root);
    }

    private void writeTree(String treeSha, Path dir) throws IOException {
        Files.createDirectories(dir);
        for (TreeBody.Entry entry : TreeBody.parse(require(treeSha, ObjectType.TREE).body())) {
            final String sha = HexFormat.of().formatHex(entry.sha());
            final Path path = dir.resolve(entry.name());
            switch (FileMode.from(entry.mode())) {
                case DIRECTORY -> writeTree(sha, path);
                case GITLINK -> Files.createDirectories(path); // submodule: nothing to fetch, leave an empty dir
                case SYMLINK -> Files.createSymbolicLink(path, Path.of(new String(blob(sha), StandardCharsets.UTF_8)));
                case EXECUTABLE -> {
                    Files.write(path, blob(sha));
                    path.toFile().setExecutable(true);
                }
                case REGULAR -> Files.write(path, blob(sha));
            }
        }
    }

    private byte[] blob(String sha) throws IOException {
        return require(sha, ObjectType.BLOB).body();
    }

    private PackedObject require(String sha, ObjectType type) throws IOException {
        final PackedObject object = objects.get(sha);
        if (object == null || object.type() != type) {
            throw new IOException("missing " + type.value() + " object " + sha);
        }
        return object;
    }
}
