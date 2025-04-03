import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.*;
import java.io.*;
import java.security.KeyStore;
import org.json.JSONObject;

public class HttpsClient {
    private static final String KEYSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-keystore.p12";  // İstemci keystore dosyasının yolu
    private static final String KEYSTORE_PASSWORD = "123456";           // Keystore şifresi
    private static final String TRUSTSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-truststore.p12";  // TrustStore dosyasının yolu
    private static final String TRUSTSTORE_PASSWORD = "123456";         // TrustStore şifresi
    private static final String SERVER_HOST = "192.168.1.61";
    private static final int SERVER_PORT = 8443;

    public static void main(String[] args) {
        try {
            // Enable SSL/TLS debugging (Handshake details, etc.)
            System.setProperty("javax.net.debug", "ssl,handshake,verbose");

            // Enable HTTP connection debugging (request/response)
            System.setProperty("java.net.debug", "all");

            // İstemci KeyStore'u yükle
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(KEYSTORE_PATH), KEYSTORE_PASSWORD.toCharArray());

            // KeyManagerFactory ile istemci KeyManager'ı oluştur
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

            // Client TrustStore'u yükle
            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(new FileInputStream(TRUSTSTORE_PATH), TRUSTSTORE_PASSWORD.toCharArray());

            // TrustManagerFactory ile trust manager'ı oluştur
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            // SSLContext oluştur
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            // SSLSocketFactory oluştur
            SSLSocketFactory factory = sslContext.getSocketFactory();

            // Sunucuya bağlan
            SSLSocket socket = (SSLSocket) factory.createSocket(SERVER_HOST, SERVER_PORT);

            // SSL el sıkışmasını başlat
            socket.startHandshake();
            System.out.println("Connected to server!");

            // JSON veri oluştur
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("name", "test");
            jsonObject.put("age", 123456);

            // Sunucuya JSON verisini gönderme
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println(jsonObject.toString());
            System.out.println("Sent to server: " + jsonObject.toString());

            // Sunucudan cevap alma
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String serverResponse = reader.readLine();
            System.out.println("Server response: " + serverResponse);

            // Bağlantıyı kapat
            socket.close();
        } catch (Exception e) {
            System.err.println("Error occurred during connection: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
