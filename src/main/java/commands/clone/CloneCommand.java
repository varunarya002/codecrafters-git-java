package commands.clone;

import commands.Command;
import pack.PackIndexWriter;
import pack.PackParser;
import pack.PackedObject;
import transport.RemoteRefs;
import transport.Transport;
import utils.GitConstants;
import utils.GitPath;
import utils.RefWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/** {@code clone <url> <dir>}: fetches a repository over HTTP into a new directory and checks out HEAD. */
public class CloneCommand implements Command {

    @Override
    public void execute(String[] args) throws IOException {
        if (args.length != 3) {
            throw new IllegalArgumentException("usage: clone <url> <dir>");
        }
        final Path target = Path.of(args[2]);
        if (Files.exists(target)) {
            throw new IllegalArgumentException("destination path '" + args[2] + "' already exists");
        }

        System.out.println("Cloning into '" + args[2] + "'...");
        try {
            clone(args[1], target);
        } catch (IOException | RuntimeException e) {
            deleteQuietly(target);
            throw e;
        }
    }

    private void clone(String url, Path target) throws IOException {
        final Transport transport = Transport.open(url);
        final RemoteRefs refs = transport.discover();

        final Path packDir = GitPath.PACK.resolveIn(target);
        Files.createDirectories(packDir);
        Files.createDirectories(GitPath.HEADS.resolveIn(target));

        final Map<String, PackedObject> objects = new LinkedHashMap<>();
        for (byte[] packBytes : transport.fetchPacks(refs)) {
            final PackParser.Result parsed = PackParser.parse(packBytes);
            final String name = GitConstants.PACK_PREFIX + parsed.checksum();
            Files.write(packDir.resolve(name + GitConstants.PACK_EXTENSION), packBytes);
            Files.write(packDir.resolve(name + GitConstants.INDEX_EXTENSION),
                    PackIndexWriter.write(parsed.index(), parsed.checksum()));
            objects.putAll(parsed.objects());
        }

        writeHead(target, refs);
        new Checkout(objects).checkout(refs.headSha(), target);
    }

    private void writeHead(Path target, RemoteRefs refs) throws IOException {
        final Path head = GitPath.HEAD.resolveIn(target);
        if (refs.headBranch() == null) {
            RefWriter.writeSha(head, refs.headSha());
            return;
        }
        RefWriter.writeSymbolic(head, refs.headBranch());
        RefWriter.writeSha(GitPath.ROOT.resolveIn(target, refs.headBranch()), refs.headSha());
    }

    private static void deleteQuietly(Path target) {
        if (!Files.exists(target)) {
            return;
        }
        try (var paths = Files.walk(target)) {
            paths.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.delete(p);
                } catch (IOException ignored) {
                    // Best effort cleanup after a failed clone.
                }
            });
        } catch (IOException ignored) {
            // Best effort cleanup after a failed clone.
        }
    }
}
