package commands.treeobject;

import commands.Command;
import utils.CommandUtil;
import utils.GitObject;
import utils.Sha1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class ReadTreeObjectCommand implements Command {

    @Override
    public void execute(String[] args) {
        final ParsedArguments parsed = parseArguments(args);
        final String treeSha = parsed.treeSha();

        try {
            final GitObject object = GitObject.read(CommandUtil.extractObjectPath(treeSha));
            if (!"tree".equals(object.type())) {
                throw new IllegalArgumentException("Not a tree object: " + treeSha);
            }
            printEntries(object.body(), parsed.flag());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ParsedArguments parseArguments(String[] args) {
        final ParsedArguments parsed;
        if (args.length == 2) {
            parsed = new ParsedArguments(TreeOutputMode.FULL, args[1]);
        } else if (args.length == 3) {
            parsed = new ParsedArguments(TreeOutputMode.fromFlag(args[1]), args[2]);
        } else {
            throw new IllegalArgumentException("Usage: ls-tree [--name-only] <tree-sha>");
        }
        Sha1.validateHexDigest(parsed.treeSha());
        return parsed;
    }

    private static void printEntries(byte[] body, TreeOutputMode flag) {
        final StringBuilder output = new StringBuilder();
        int offset = 0;
        while (offset < body.length) {
            final int space = GitObject.indexOf(body, (byte) ' ', offset);
            final int nul = space < 0 ? -1 : GitObject.indexOf(body, (byte) 0, space + 1);
            if (space < 0 || nul < 0 || body.length - nul - 1 < 20) {
                throw new IllegalArgumentException("Malformed tree object");
            }

            final String mode = new String(body, offset, space - offset, StandardCharsets.US_ASCII);
            final String name = new String(body, space + 1, nul - space - 1, StandardCharsets.UTF_8);
            output.append(flag.format(mode, name, body, nul + 1)).append('\n');
            offset = nul + 21;
        }
        System.out.print(output);
    }

    private record ParsedArguments(TreeOutputMode flag, String treeSha) {
    }
}
