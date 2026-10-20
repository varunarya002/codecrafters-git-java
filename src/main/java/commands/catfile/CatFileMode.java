package commands.catfile;

import commands.treeobject.TreeOutputMode;
import utils.CommandUtil;
import utils.GitObject;
import utils.ObjectType;

import java.io.IOException;
import java.util.Arrays;

public enum CatFileMode {
    PRETTY_PRINT("-p") {
        @Override
        public void execute(String objectHash) throws IOException {
            final GitObject object = GitObject.read(CommandUtil.extractObjectPath(objectHash));
            if (object.type() == ObjectType.TREE) {
                System.out.print(TreeOutputMode.FULL.render(object.body()));
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
}
