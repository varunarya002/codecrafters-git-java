package commands.treeobject;

import commands.Command;
import utils.CommandUtil;
import utils.GitObject;
import utils.ObjectType;
import utils.Sha1;

import java.io.IOException;

public class ReadTreeObjectCommand implements Command {

    @Override
    public void execute(String[] args) throws IOException {
        final ParsedArguments parsed = parseArguments(args);
        final String treeSha = parsed.treeSha();

        final GitObject object = GitObject.read(CommandUtil.extractObjectPath(treeSha));
        if (object.type() != ObjectType.TREE) {
            throw new IllegalArgumentException("Not a tree object: " + treeSha);
        }
        System.out.print(parsed.flag().render(object.body()));
    }

    private static ParsedArguments parseArguments(String[] args) {
        final ParsedArguments parsed;
        if (args.length == 2) {
            parsed = new ParsedArguments(TreeOutputMode.FULL, args[1]);
        } else if (args.length == 3) {
            parsed = new ParsedArguments(TreeOutputMode.fromFlag(args[1]), args[2]);
        } else {
            throw new IllegalArgumentException(TreeOutputMode.USAGE);
        }
        Sha1.validateHexDigest(parsed.treeSha());
        return parsed;
    }

    private record ParsedArguments(TreeOutputMode flag, String treeSha) {
    }
}
