package commands.treeobject;

import commands.Command;
import utils.CommandUtil;
import utils.GitObject;
import utils.Sha1;

import java.io.IOException;

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
            System.out.print(parsed.flag().render(object.body()));
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

    private record ParsedArguments(TreeOutputMode flag, String treeSha) {
    }
}
