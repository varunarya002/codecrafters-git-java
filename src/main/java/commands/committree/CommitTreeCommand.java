package commands.committree;

import commands.Command;
import utils.CommitHeader;
import utils.GitConstants;
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
    private static final String MISSING_VALUE = "Missing value for ";

    private static final String PARENT_FLAG = "-p";
    private static final String MESSAGE_FLAG = "-m";

    private static final DateTimeFormatter TIMEZONE_OFFSET = DateTimeFormatter.ofPattern("xx");

    @Override
    public void execute(String[] args) throws IOException {
        final ParsedArguments parsed = parseArguments(args);
        System.out.println(ObjectWriter.write(ObjectType.COMMIT, commit(parsed)));
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
                    parentSha = nextValue(tokens, MISSING_VALUE + option);
                    Sha1.validateHexDigest(parentSha);
                }
                case MESSAGE_FLAG -> message = nextValue(tokens, MISSING_VALUE + option);
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
        final String signature = AUTHOR + GitConstants.SPACE + timestamp(OffsetDateTime.now());
        final String parent = parsed.parentSha() == null ? "" : CommitHeader.PARENT.line(parsed.parentSha());

        final String headers = CommitHeader.TREE.line(parsed.treeSha())
                + parent
                + CommitHeader.AUTHOR.line(signature)
                + CommitHeader.COMMITTER.line(signature);

        return (headers + GitConstants.NEWLINE + parsed.message() + GitConstants.NEWLINE)
                .getBytes(StandardCharsets.UTF_8);
    }

    private static String timestamp(OffsetDateTime now) {
        return now.toEpochSecond() + GitConstants.SPACE + now.format(TIMEZONE_OFFSET);
    }

    private record ParsedArguments(String treeSha, String parentSha, String message) {
    }
}
