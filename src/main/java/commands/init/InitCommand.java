package commands.init;

import commands.Command;
import utils.GitPath;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class InitCommand implements Command {

    @Override
    public void execute(String[] args) {
        final File root = new File(GitPath.ROOT.value());
        new File(GitPath.OBJECTS.value()).mkdirs();
        new File(GitPath.REFS.value()).mkdirs();
        final File head = new File(GitPath.HEAD.value());

        try {
            head.createNewFile();
            Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
            System.out.println("Initialized git directory in " + root.getCanonicalPath() + "/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
