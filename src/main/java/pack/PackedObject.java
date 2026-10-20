package pack;

import utils.ObjectType;

/** A fully resolved object (deltas applied) read from a packfile. */
public record PackedObject(String sha, ObjectType type, byte[] body) {
}
