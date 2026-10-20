import commands.CommandRegistry;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Main {
    public static void main(String[] args) {
        System.err.println("Logs from your program will appear here!");
        try {
            CommandRegistry.from(args[0]).execute(args);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
