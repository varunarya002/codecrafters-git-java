package utils;

import java.nio.file.Path;

public class CommandUtil {

    public static Path extractObjectPath(String objectHash) {
        return GitPath.OBJECTS.resolve(objectHash.substring(0, 2), objectHash.substring(2));
    }
}
