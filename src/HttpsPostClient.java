import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Enumeration;

public class HttpsPostClient {
    public static void main(String[] args) {
        try {
            // KeyStore ve TrustStore dosya yolları
            String keyStorePath = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\keystore.jks";
            String trustStorePath = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\truststore.jks";
            String keyStorePassword = "123456";
            String trustStorePassword = "123456";

            System.out.println("Loading KeyStore...");
            // KeyStore'u yükle
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream keyStoreStream = new FileInputStream(keyStorePath)) {
                keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
            }

            System.out.println("Loading TrustStore...");
            // TrustStore'u yükle
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream trustStoreStream = new FileInputStream(trustStorePath)) {
                trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
            }

            System.out.println("Initializing KeyManagerFactory...");
            // KeyManagerFactory'i başlat
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            System.out.println("Initializing TrustManagerFactory...");
            // TrustManagerFactory'i başlat
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            System.out.println("Initializing SSLContext...");
            // SSLContext'u başlat
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

            System.out.println("Opening connection to server...");
            // HttpsURLConnection'u yapılandır
            URL url = new URL("https://192.168.1.61:8443");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("GET"); // Changed to GET request (no POST)
            connection.setDoOutput(false); // Don't send any output
            connection.setRequestProperty("Accept", "application/json");

            // Receiving response...
            System.out.println("Receiving response...");

            // Yanıtı al
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                // Sunucudan gelen cevabı oku
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();

                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    System.out.println("Response Body: " + response.toString());
                }
            } else {
                System.out.println("Error: Response code " + responseCode);
            }
        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
