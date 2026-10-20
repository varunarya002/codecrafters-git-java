package commands.hashobject;

import commands.Command;
import utils.ObjectType;
import utils.ObjectWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HashObjectCommand implements Command {

    private static final String WRITE_FLAG = "-w";

    @Override
    public void execute(String[] args) throws IOException {
        final byte[] content = Files.readAllBytes(Path.of(args[2]));
        final String hash = WRITE_FLAG.equals(args[1])
                ? ObjectWriter.write(ObjectType.BLOB, content)
                : ObjectWriter.hash(ObjectType.BLOB, content);

        System.out.println(hash);
    }
}
