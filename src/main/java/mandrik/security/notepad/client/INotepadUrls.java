package mandrik.security.notepad.client;

public interface INotepadUrls {
    final String SERVER_URL = "http://localhost:8090/";
    final String GENERATE_KEY_URL = "key/generate";
    final String DOWNLOAD_URL = "download";
    final String UPLOAD_RSA_URL = "upload/public-rsa-key";
    final String GET_SESSION_KEY_URL = "session-key/get";
}
