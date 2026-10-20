package transport;

/** URL paths and keywords of git's HTTP transfer protocols. */
final class Protocol {

    static final String URL_SEPARATOR = "/";
    static final String HEAD = "HEAD";

    // Smart HTTP
    static final String UPLOAD_PACK = "git-upload-pack";
    static final String SERVICE_ANNOUNCEMENT = "# service=" + UPLOAD_PACK;
    static final String INFO_REFS = URL_SEPARATOR + "info/refs";
    static final String SMART_INFO_REFS = INFO_REFS + "?service=" + UPLOAD_PACK;
    static final String UPLOAD_PACK_PATH = URL_SEPARATOR + UPLOAD_PACK;
    static final String UPLOAD_PACK_REQUEST_TYPE = "application/x-" + UPLOAD_PACK + "-request";
    static final String SYMREF_HEAD = "symref=" + HEAD + ":";
    static final String WANT = "want";
    static final String DONE = "done";

    // Dumb HTTP
    static final String HEAD_PATH = URL_SEPARATOR + HEAD;
    static final String PACKS_LIST = URL_SEPARATOR + "objects/info/packs";
    static final String PACK_DIR = URL_SEPARATOR + "objects/pack/";
    static final String PACK_LINE_PREFIX = "P ";

    private Protocol() {
    }
}
