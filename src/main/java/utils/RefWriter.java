package utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Writes the two kinds of ref file: a symbolic ref ({@code ref: refs/heads/main}) and a direct SHA. */
public final class RefWriter {

    private RefWriter() {
    }

    public static void writeSymbolic(Path file, String refName) throws IOException {
        Files.writeString(file, GitConstants.REF_PREFIX + refName + GitConstants.NEWLINE);
    }

    public static void writeSha(Path file, String sha) throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, sha + GitConstants.NEWLINE);
    }
}
