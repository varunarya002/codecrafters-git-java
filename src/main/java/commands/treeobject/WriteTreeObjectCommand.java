package commands.treeobject;

import commands.Command;
import commands.hashobject.HashObjectCommand;

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

    private static final String GIT_DIR = ".git";

    @Override
    public void execute(String[] args) {
        try {
            final String treeSha = writeTree(Path.of("."));
            System.out.println(treeSha != null ? treeSha : HashObjectCommand.write("tree", new byte[0]));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** Stores the tree for {@code dir}, or returns null when it holds no files (git skips empty directories). */
    private static String writeTree(Path dir) throws IOException {
        final List<Entry> entries = new ArrayList<>();
        try (Stream<Path> children = Files.list(dir)) {
            for (Path child : children.toList()) {
                final String name = child.getFileName().toString();
                if (name.equals(GIT_DIR)) {//This is to ignore .git directory
                    continue;
                }
                if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                    final String hash = writeTree(child);
                    if (hash != null) {
                        entries.add(new Entry(FileMode.DIRECTORY, name, hash));
                    }
                } else if (Files.isRegularFile(child, LinkOption.NOFOLLOW_LINKS)) {
                    final String hash = HashObjectCommand.write("blob", Files.readAllBytes(child));
                    entries.add(new Entry(FileMode.of(child), name, hash));
                }
            }
        }
        if (entries.isEmpty()) {
            return null;
        }
        entries.sort(Comparator.comparing(Entry::sortKey));
        return HashObjectCommand.write("tree", body(entries));
    }

    private static byte[] body(List<Entry> entries) throws IOException {
        final var out = new ByteArrayOutputStream();
        for (Entry entry : entries) {
            out.write((entry.mode().value() + " " + entry.name() + "\0").getBytes(StandardCharsets.UTF_8));
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
            return mode == FileMode.DIRECTORY ? name + "/" : name;
        }
    }
}
