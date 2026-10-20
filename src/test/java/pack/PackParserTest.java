package pack;

import org.junit.jupiter.api.Test;
import utils.ObjectType;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PackParserTest {

    /** Packfile made by git from a 5-commit history: 21 base objects and 4 OFS/REF deltas. */
    private static final String FIXTURE = "/delta-fixture.pack";
    private static final String HEAD_COMMIT = "3fb39753a09ac724197435b49f995006beb16840";

    @Test
    void resolvesDeltasIntoEveryObject() throws IOException {
        final byte[] pack = fixture();
        final PackParser.Result result = PackParser.parse(pack);

        assertEquals(25, result.objects().size());
        assertEquals(25, result.index().size());
        assertEquals(HexFormat.of().formatHex(pack, pack.length - 20, pack.length), result.checksum());
        assertEquals(ObjectType.COMMIT, result.objects().get(HEAD_COMMIT).type());
    }

    @Test
    void writesVersionTwoIndexWithFanoutAndTrailers() throws IOException {
        final byte[] pack = fixture();
        final PackParser.Result result = PackParser.parse(pack);
        final byte[] index = PackIndexWriter.write(result.index(), result.checksum());

        assertArrayEquals(new byte[]{(byte) 0xff, 't', 'O', 'c', 0, 0, 0, 2}, Arrays.copyOf(index, 8));
        // Fanout's last slot holds the total object count.
        assertEquals(25, java.nio.ByteBuffer.wrap(index, 8 + 255 * 4, 4).getInt());
        // header + fanout + shas + crcs + offsets + pack checksum + index checksum
        assertEquals(8 + 1024 + 25 * 20 + 25 * 4 + 25 * 4 + 20 + 20, index.length);
    }

    @Test
    void rejectsPackWithBadChecksum() throws IOException {
        final byte[] pack = fixture();
        pack[100] ^= 0x01;
        assertThrows(IOException.class, () -> PackParser.parse(pack));
    }

    private static byte[] fixture() throws IOException {
        try (InputStream in = PackParserTest.class.getResourceAsStream(FIXTURE)) {
            assertTrue(in != null, "missing test resource " + FIXTURE);
            return in.readAllBytes();
        }
    }
}
