package transport;

/**
 * What the remote advertises for HEAD.
 *
 * @param headSha    the commit HEAD points to
 * @param headBranch the full ref name HEAD points to (e.g. {@code refs/heads/main}), or {@code null} when HEAD is detached
 */
public record RemoteRefs(String headSha, String headBranch) {
}
