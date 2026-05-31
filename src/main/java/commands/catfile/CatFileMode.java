package commands.catfile;

import utils.CommandUtil;
import utils.ZlibDecompressor;

import java.io.IOException;
import java.util.Arrays;

public enum CatFileMode {
    PRETTY_PRINT("-p") {
        @Override
        public void execute(String objectHash) throws IOException {
            String content = ZlibDecompressor.getContent(CommandUtil.extractObjectPath(objectHash));
            System.out.print(content);
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
