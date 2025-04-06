import org.json.JSONObject;

import javax.net.ssl.*;
import java.io.*;
import java.net.Socket;
import java.security.KeyStore;

public class HttpsClient {
    private static final String KEYSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-keystore.p12";
    private static final String KEYSTORE_PASSWORD = "123456";
    private static final String TRUSTSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-truststore.p12";
    private static final String TRUSTSTORE_PASSWORD = "123456";
    private static final String SERVER_HOST = "192.168.1.61";
    private static final int SERVER_PORT = 8443;

    public static void main(String[] args) {
        try {
            // Debugging (isteğe bağlı aktif edilebilir)
            // System.setProperty("javax.net.debug", "ssl,handshake,verbose");
            // System.setProperty("java.net.debug", "all");

            // SSLContext oluştur
            SSLContext sslContext = createSSLContext();
            SSLSocketFactory factory = sslContext.getSocketFactory();

            // Sunucuya bağlan
            try (SSLSocket socket = (SSLSocket) factory.createSocket(SERVER_HOST, SERVER_PORT)) {
                socket.startHandshake();
                System.out.println("Connected to server!");

                // JSON veri gönder
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("name", "test");
                jsonObject.put("age", 123456);

                try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                    writer.println(jsonObject.toString());
                    System.out.println("Sent to server: " + jsonObject);

                    String response = reader.readLine();
                    System.out.println("Server response: " + response);
                }
            }

        } catch (Exception e) {
            System.err.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static SSLContext createSSLContext() throws Exception {
        // KeyStore yükle
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(keyStoreStream, KEYSTORE_PASSWORD.toCharArray());
        }

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // TrustStore yükle
        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        try (InputStream trustStoreStream = new FileInputStream(TRUSTSTORE_PATH)) {
            trustStore.load(trustStoreStream, TRUSTSTORE_PASSWORD.toCharArray());
        }

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return context;
    }
}
