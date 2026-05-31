package commands.catfile;

import commands.Command;

import java.io.IOException;

public class CatFileCommand implements Command {

    @Override
    public void execute(String[] args) {
        try {
            CatFileMode.from(args[1]).execute(args[2]);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
