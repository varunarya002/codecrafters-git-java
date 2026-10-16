package commands.treeobject;

import utils.ModeUtil;

import java.util.HexFormat;

enum TreeOutputMode {
    FULL("") {
        @Override
        String format(String mode, String name, byte[] body, int shaOffset) {
            return ModeUtil.padMode(mode) + " " + ModeUtil.typeForMode(mode) + " "
                    + HexFormat.of().formatHex(body, shaOffset, shaOffset + 20) + "\t" + name;
        }
    },
    NAME_ONLY("--name-only") {
        @Override
        String format(String mode, String name, byte[] body, int shaOffset) {
            return name;
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

    abstract String format(String mode, String name, byte[] body, int shaOffset);
}
