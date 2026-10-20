package pack;

/**
 * One object's row in a pack index.
 *
 * @param sha    hex SHA-1 of the resolved object
 * @param crc    CRC-32 of the object's raw bytes as stored in the pack
 * @param offset byte offset of the object's entry in the pack
 */
public record IndexEntry(String sha, long crc, long offset) {
}
