package commands.treeobject;

import java.util.HexFormat;

enum TreeOutputMode {
    FULL("") {
        @Override
        String format(String mode, String name, byte[] body, int shaOffset) {
            final String type = switch (mode) {
                case "40000", "040000" -> "tree";
                case "160000" -> "commit";
                default -> "blob";
            };
            return "0".repeat(Math.max(0, 6 - mode.length())) + mode + " " + type + " "
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
