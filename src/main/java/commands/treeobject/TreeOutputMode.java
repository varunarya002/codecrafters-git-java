package commands.treeobject;

import utils.TreeBody;

import java.util.HexFormat;

public enum TreeOutputMode {
    FULL("") {
        @Override
        String format(TreeBody.Entry entry) {
            final FileMode mode = FileMode.from(entry.mode());
            return mode.paddedValue() + " " + mode.type().value() + " "
                    + HexFormat.of().formatHex(entry.sha()) + "\t" + entry.name();
        }
    },
    NAME_ONLY("--name-only") {
        @Override
        String format(TreeBody.Entry entry) {
            return entry.name();
        }
    };

    private final String flag;

    TreeOutputMode(String flag) {
        this.flag = flag;
    }

    static TreeOutputMode fromFlag(String flag) {
        if (NAME_ONLY.flag.equals(flag)) {
            return NAME_ONLY;
        }
        throw new IllegalArgumentException("Usage: ls-tree [--name-only] <tree-sha>");
    }

    public String render(byte[] treeBody) {
        final StringBuilder output = new StringBuilder();
        for (TreeBody.Entry entry : TreeBody.parse(treeBody)) {
            output.append(format(entry)).append('\n');
        }
        return output.toString();
    }

    abstract String format(TreeBody.Entry entry);
}
