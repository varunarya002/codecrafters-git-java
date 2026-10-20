package commands.treeobject;

import commands.Command;
import utils.FileMode;
import utils.GitConstants;
import utils.GitPath;
import utils.ObjectType;
import utils.ObjectWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;

public class WriteTreeObjectCommand implements Command {

    private static final Path WORKING_DIRECTORY = Path.of(".");
    private static final String DIRECTORY_SORT_SUFFIX = "/";

    @Override
    public void execute(String[] args) throws IOException {
        final String treeSha = writeTree(WORKING_DIRECTORY);
        System.out.println(treeSha != null ? treeSha : ObjectWriter.write(ObjectType.TREE, new byte[0]));
    }

    /** Stores the tree for {@code dir}, or returns null when it holds no files (git skips empty directories). */
    private static String writeTree(Path dir) throws IOException {
        final List<Entry> entries = new ArrayList<>();
        try (Stream<Path> children = Files.list(dir)) {
            for (Path child : children.toList()) {
                final String name = child.getFileName().toString();
                if (name.equals(GitPath.ROOT.value())) {//This is to ignore .git directory
                    continue;
                }
                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                    final String hash = writeTree(child);
                    if (hash != null) {
                        entries.add(new Entry(FileMode.DIRECTORY, name, hash));
                    }
                } else if (Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS)) {
                    final String hash = ObjectWriter.write(ObjectType.BLOB, Files.readAllBytes(child));
                    entries.add(new Entry(FileMode.of(child), name, hash));
                }
            }
        }
        if (entries.isEmpty()) {
            return null;
        }
        entries.sort(Comparator.comparing(Entry::sortKey));
        return ObjectWriter.write(ObjectType.TREE, body(entries));
    }

    private static byte[] body(List<Entry> entries) throws IOException {
        final var out = new ByteArrayOutputStream();
        for (Entry entry : entries) {
            out.write((entry.mode().value() + GitConstants.SPACE + entry.name() + GitConstants.NUL)
                    .getBytes(StandardCharsets.UTF_8));
            out.write(HexFormat.of().parseHex(entry.hash()));
        }
        return out.toByteArray();
    }

    /**
     * A tree entry. Git sorts a directory as if its name ended in "/", so {@code a.txt} comes before
     * the directory {@code a} but after the file {@code a}.
     */
    private record Entry(FileMode mode, String name, String hash) {

        String sortKey() {
            return mode == FileMode.DIRECTORY ? name + DIRECTORY_SORT_SUFFIX : name;
        }
    }
}
