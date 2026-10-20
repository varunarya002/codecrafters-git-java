package commands.treeobject;

import utils.FileMode;
import utils.GitConstants;
import utils.TreeBody;

import java.util.HexFormat;

public enum TreeOutputMode {
    FULL("") {
        @Override
        String format(TreeBody.Entry entry) {
            final FileMode mode = FileMode.from(entry.mode());
            return mode.paddedValue() + GitConstants.SPACE + mode.type().value() + GitConstants.SPACE
                    + HexFormat.of().formatHex(entry.sha()) + GitConstants.TAB + entry.name();
        }
    },
    NAME_ONLY("--name-only") {
        @Override
        String format(TreeBody.Entry entry) {
            return entry.name();
        }
    };

    static final String USAGE = "Usage: ls-tree [--name-only] <tree-sha>";

    private final String flag;

    TreeOutputMode(String flag) {
        this.flag = flag;
    }

    static TreeOutputMode fromFlag(String flag) {
        if (NAME_ONLY.flag.equals(flag)) {
            return NAME_ONLY;
        }
        throw new IllegalArgumentException(USAGE);
    }

    public String render(byte[] treeBody) {
        final StringBuilder output = new StringBuilder();
        for (TreeBody.Entry entry : TreeBody.parse(treeBody)) {
            output.append(format(entry)).append(GitConstants.NEWLINE);
        }
        return output.toString();
    }

    abstract String format(TreeBody.Entry entry);
}
