import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

public class HttpsClient {
    private static final String KEYSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-keystore.p12";
    private static final String TRUSTSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-truststore.p12";
    private static final char[] STORE_PASSWORD = "123456".toCharArray();
    private static final String SERVER_HOST = "192.168.1.61";
    private static final int SERVER_PORT = 8443;

    public static void main(String[] args) {
        try {
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory socketFactory = sslContext.getSocketFactory();

            try (SSLSocket socket = (SSLSocket) socketFactory.createSocket(SERVER_HOST, SERVER_PORT)) {
                socket.startHandshake();
                System.out.println("✅ SSL Handshake completed. Connected to server.");

                JSONObject data = new JSONObject()
                        .put("name", "test")
                        .put("age", 123456);

                sendJson(socket, data);
            }
        } catch (Exception e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = loadKeyStore(KEYSTORE_PATH, STORE_PASSWORD);
        KeyStore trustStore = loadKeyStore(TRUSTSTORE_PATH, STORE_PASSWORD);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, STORE_PASSWORD);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }

    private static KeyStore loadKeyStore(String path, char[] password) throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream stream = new FileInputStream(path)) {
            keyStore.load(stream, password);
        }
        return keyStore;
    }

    private static void sendJson(Socket socket, JSONObject jsonObject) {
        try (
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))
        ) {
            writer.println(jsonObject.toString());
            System.out.println("📤 Sent to server: " + jsonObject);

            String response = reader.readLine();
            System.out.println("📥 Server response: " + response);

        } catch (IOException e) {
            System.err.println("❌ Error sending/receiving JSON: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
