package commands.clone;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Hits the network, so it only runs when GIT_LIVE_TESTS is set. */
class CloneCommandLiveTest {

    @TempDir
    Path tmp;

    @Test
    void clonesPublicRepository() throws Exception {
        Assumptions.assumeTrue(System.getenv("GIT_LIVE_TESTS") != null, "set GIT_LIVE_TESTS to run");
        final Path target = tmp.resolve("hello");

        new CloneCommand().execute(new String[]{"clone", "https://github.com/octocat/Hello-World.git", target.toString()});

        assertTrue(Files.readString(target.resolve("README")).contains("Hello World"));
        assertTrue(Files.exists(target.resolve(".git/HEAD")));
    }
}
