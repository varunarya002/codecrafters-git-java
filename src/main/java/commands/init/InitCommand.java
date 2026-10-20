package commands.init;

import commands.Command;
import utils.GitConstants;
import utils.GitPath;
import utils.RefWriter;

import java.io.File;
import java.io.IOException;

public class InitCommand implements Command {

    @Override
    public void execute(String[] args) throws IOException {
        final File root = new File(GitPath.ROOT.value());
        new File(GitPath.OBJECTS.value()).mkdirs();
        new File(GitPath.REFS.value()).mkdirs();

        RefWriter.writeSymbolic(GitPath.HEAD.resolve(), GitPath.HEADS.refName(GitConstants.DEFAULT_BRANCH));
        System.out.println("Initialized git directory in " + root.getCanonicalPath() + "/");
    }
}
