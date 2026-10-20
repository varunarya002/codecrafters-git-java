package utils;

import java.util.Arrays;

public enum ObjectType {
    BLOB("blob"),
    TREE("tree"),
    COMMIT("commit"),
    TAG("tag");

    private final String value;

    ObjectType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static ObjectType from(String value) {
        return Arrays.stream(values())
                .filter(t -> t.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown object type: " + value));
    }
}
