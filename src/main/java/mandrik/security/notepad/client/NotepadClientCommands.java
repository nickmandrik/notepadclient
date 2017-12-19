package mandrik.security.notepad.client;

import mandrik.security.notepad.client.rest.StatefullRestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;

import mandrik.security.notepad.service.crypto.FileCipher;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.util.*;
import java.util.stream.Collectors;

public class NotepadClientCommands implements INotepadUrls {
    private RestTemplate restTemplate;
    private int KEY_SIZE = 2048;
    private List<String> cookies;
    private KeyPair keyPair;
    private byte[] sessionKey;
    private String fileName;

    public NotepadClientCommands() {
        restTemplate = new RestTemplate();
    }

    public void requestToGenerateSessionKey() {
        final String url = SERVER_URL + GENERATE_KEY_URL;

        ResponseEntity<String> result = restTemplate.getForEntity(url, String.class);
        cookies = result.getHeaders().get("Set-Cookie");
        System.out.println(result.getBody());
    }

    public KeyPair generateRSAKeyPair() {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(KEY_SIZE);
            keyPair = kpg.generateKeyPair();
            System.out.println("generated RSA key pair. Public key: " + keyPair.getPublic());
        } catch (NoSuchAlgorithmException e) {
            System.out.println(e.getMessage());
        }
        return keyPair;
    }

    public void sendPublicRSAKey(PublicKey key) {
        final String url = SERVER_URL + UPLOAD_RSA_URL;

        String publicK = Base64.encodeBase64String(key.getEncoded());
        Map result = sendRequest(HttpMethod.POST, url,  mapOf("key", publicK));

        System.out.println(result.get("result"));
    }

    public byte[] getEncodedSessionKey() throws NotepadServerException {
        final String url = SERVER_URL + GET_SESSION_KEY_URL;

        Map result = sendRequest(HttpMethod.GET, url, null);
        if(result.containsKey("result") && result.get("result").equals("error")) {
            throw new NotepadServerException("error");
        }
        String encodedSessionKeyString = (String) result.get("sessionKey");
        return Base64.decodeBase64(encodedSessionKeyString);
    }

    public byte[] decryptSessionKey(byte[] encodedSessionKey) throws NotepadServerException {
        byte[] original;
        try {
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            original = cipher.doFinal(encodedSessionKey);
            sessionKey = original;
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException
                 | NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new NotepadServerException("error");
        }

        return original;
    }

    public void downloadFile(String fileName) {
        String url = SERVER_URL + DOWNLOAD_URL;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie",cookies.stream().collect(Collectors.joining(";")));
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_OCTET_STREAM));
        HttpEntity<Map> entity = new HttpEntity<>(mapOf("fileName", fileName), headers);

        ResponseEntity<byte[]> response = restTemplate.exchange(url, HttpMethod.POST, entity, byte[].class);

        if (response.getStatusCode() == HttpStatus.OK) {
            try {
                Files.write(Paths.get("storage/temp.txt"), response.getBody());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.fileName = fileName;
    }

    public void decryptFile() {
        FileCipher fileCipher = new FileCipher( "storage/temp.txt", "D:/" + fileName,
                Base64.encodeBase64String(sessionKey), false);

        try {
            fileCipher.cryptFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map sendRequest(HttpMethod method, String url, Map params) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Cookie",cookies.stream().collect(Collectors.joining(";")));
        HttpEntity<Map> entity = new HttpEntity<>(params, headers);
        return restTemplate.exchange(url, method ,entity, Map.class).getBody();
    }

    public static Map mapOf(Object... args){
        Map result = new HashMap();
        for ( int i = 0; i < args.length - 1; i+=2){
            result.put(args[i], args[i + 1]);
        }
        return result;
    }

    public static Byte[] toByteObjectArray(byte[] bytes) {
        Byte[] byteArray = new Byte[bytes.length];

        for(int i = 0; i < bytes.length; i++) {
            byteArray[i] = bytes[i];
        }
        return byteArray;
    }
}
