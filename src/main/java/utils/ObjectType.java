package utils;

public enum ObjectType {
    BLOB("blob"),
    TREE("tree"),
    COMMIT("commit");

    private final String value;

    ObjectType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}
