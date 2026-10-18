package commands.committree;

import commands.Command;
import utils.ObjectType;
import utils.ObjectWriter;
import utils.Sha1;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

public class CommitTreeCommand implements Command {

    private static final String AUTHOR = "Codecrafters User <user@example.com>";
    private static final String USAGE = "Usage: commit-tree <tree-sha> [-p <parent-sha>] -m <message>";

    private static final String PARENT_FLAG = "-p";
    private static final String MESSAGE_FLAG = "-m";

    private static final String TREE_KEY = "tree";
    private static final String PARENT_KEY = "parent";
    private static final String AUTHOR_KEY = "author";
    private static final String COMMITTER_KEY = "committer";

    private static final String SPACE = " ";
    private static final String LINE_END = "\n";
    private static final DateTimeFormatter TIMEZONE_OFFSET = DateTimeFormatter.ofPattern("xx");

    @Override
    public void execute(String[] args) {
        final ParsedArguments parsed = parseArguments(args);
        try {
            System.out.println(ObjectWriter.write(ObjectType.COMMIT, commit(parsed)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static ParsedArguments parseArguments(String[] args) {
        final Deque<String> tokens = new ArrayDeque<>(Arrays.asList(args));
        tokens.poll();

        final String treeSha = nextValue(tokens, USAGE);
        Sha1.validateHexDigest(treeSha);

        String parentSha = null;
        String message = null;
        while (!tokens.isEmpty()) {
            final String option = tokens.poll();
            switch (option) {
                case PARENT_FLAG -> {
                    parentSha = nextValue(tokens, "Missing value for " + option);
                    Sha1.validateHexDigest(parentSha);
                }
                case MESSAGE_FLAG -> message = nextValue(tokens, "Missing value for " + option);
                default -> throw new IllegalArgumentException("Unknown option: " + option);
            }
        }
        if (message == null) {
            throw new IllegalArgumentException(USAGE);
        }
        return new ParsedArguments(treeSha, parentSha, message);
    }

    private static String nextValue(Deque<String> tokens, String missingMessage) {
        final String value = tokens.poll();
        if (value == null) {
            throw new IllegalArgumentException(missingMessage);
        }
        return value;
    }

    /** Commit body: header lines, a blank line, then the message. Tree and parent are hex SHAs. */
    private static byte[] commit(ParsedArguments parsed) {
        final String signature = AUTHOR + SPACE + timestamp(OffsetDateTime.now());
        final String parent = parsed.parentSha() == null ? "" : header(PARENT_KEY, parsed.parentSha());

        final String headers = header(TREE_KEY, parsed.treeSha())
                + parent
                + header(AUTHOR_KEY, signature)
                + header(COMMITTER_KEY, signature);

        return (headers + LINE_END + parsed.message() + LINE_END).getBytes(StandardCharsets.UTF_8);
    }

    private static String header(String key, String value) {
        return key + SPACE + value + LINE_END;
    }

    private static String timestamp(OffsetDateTime now) {
        return now.toEpochSecond() + SPACE + now.format(TIMEZONE_OFFSET);
    }

    private record ParsedArguments(String treeSha, String parentSha, String message) {
    }
}
