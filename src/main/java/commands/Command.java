package commands;

import utils.GitPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public enum Command {
    INIT("init") {
        @Override
        public void execute(String[] args) {
            final File root = new File(GitPath.ROOT.value());
            new File(GitPath.OBJECTS.value()).mkdirs();
            new File(GitPath.REFS.value()).mkdirs();
            final File head = new File(GitPath.HEAD.value());

            try {
                head.createNewFile();
                Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
                System.out.println("Initialized git directory");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    },
    CAT_FILE("cat-file") {
        @Override
        public void execute(String[] args) {
            try {
                CatFileFlag.from(args[1]).execute(args[2]);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    };

    private final String name;

    Command(String name) {
        this.name = name;
    }

    public abstract void execute(String[] args);

    public static Command from(String name) {
        return Arrays.stream(values())
                .filter(cmd -> cmd.name.equals(name))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown command: " + name));
    }
}
