package commands;

import commands.catfile.CatFileCommand;
import commands.hashobject.HashObjectCommand;
import commands.init.InitCommand;

import java.util.HashMap;
import java.util.Map;

public final class CommandRegistry {

    private static final Map<String, Command> COMMANDS = new HashMap<>();

    static {
        register("init", new InitCommand());
        register("cat-file", new CatFileCommand());
        register("hash-object", new HashObjectCommand());
    }

    private CommandRegistry() {
    }

    private static void register(String name, Command command) {
        COMMANDS.put(name, command);
    }

    public static Command from(String name) {
        final Command command = COMMANDS.get(name);
        if (command == null) {
            throw new IllegalArgumentException("Unknown command: " + name);
        }
        return command;
    }
}
