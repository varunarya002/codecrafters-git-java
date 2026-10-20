package commands.catfile;

import commands.Command;

import java.io.IOException;

public class CatFileCommand implements Command {

    @Override
    public void execute(String[] args) throws IOException {
        CatFileMode.from(args[1]).execute(args[2]);
    }
}
