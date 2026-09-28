package commands.catfile;

import utils.CommandUtil;
import utils.GitObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public enum CatFileMode {
    PRETTY_PRINT("-p") {
        @Override
        public void execute(String objectHash) throws IOException {
            final GitObject object = GitObject.read(CommandUtil.extractObjectPath(objectHash));
            if ("tree".equals(object.type())) {
                printTree(object.body());
            } else {
                // blob, commit, tag: body is the verbatim payload — write the raw bytes so binary
                // content and trailing newlines survive untouched.
                System.out.write(object.body());
                System.out.flush();
            }
        }
    };

    private final String flag;

    CatFileMode(String flag) {
        this.flag = flag;
    }

    public abstract void execute(String objectHash) throws IOException;

    public static CatFileMode from(String flag) {
        return Arrays.stream(values())
                .filter(f -> f.flag.equals(flag))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown flag: " + flag));
    }

    /**
     * Renders a tree body as git does: one entry per line, {@code <mode> <type> <hexsha>\t<name>}.
     * Each on-disk entry is {@code <mode> <name>\0<20-byte raw SHA>}.
     */
    private static void printTree(byte[] body) {
        final StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < body.length) {
            final int space = GitObject.indexOf(body, (byte) ' ', i);
            final String mode = new String(body, i, space - i, StandardCharsets.US_ASCII);

            final int nul = GitObject.indexOf(body, (byte) 0, space + 1);
            final String name = new String(body, space + 1, nul - (space + 1), StandardCharsets.UTF_8);

            final byte[] sha = Arrays.copyOfRange(body, nul + 1, nul + 1 + 20);
            i = nul + 1 + 20;

            out.append(padMode(mode)).append(' ')
                    .append(typeForMode(mode)).append(' ')
                    .append(toHex(sha)).append('\t')
                    .append(name).append('\n');
        }
        System.out.print(out);
    }

    private static String padMode(String mode) {
        return "0".repeat(Math.max(0, 6 - mode.length())) + mode;
    }

    private static String typeForMode(String mode) {
        return switch (mode) {
            case "40000" -> "tree";
            case "160000" -> "commit";
            default -> "blob";
        };
    }

    private static String toHex(byte[] bytes) {
        final StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
