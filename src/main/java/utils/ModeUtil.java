package utils;

public class ModeUtil {

    public static String typeForMode(String mode) {
        return switch (mode) {
            case "40000", "040000" -> "tree";
            case "160000" -> "commit";
            default -> "blob";
        };
    }

    /** Git stores tree modes without the leading zero ("40000"); output always shows six digits. */
    public static String padMode(String mode) {
        return "0".repeat(Math.max(0, 6 - mode.length())) + mode;
    }
}
