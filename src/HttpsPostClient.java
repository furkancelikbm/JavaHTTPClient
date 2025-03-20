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
            String keyStorePath = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\mykeystore.jks";
            String trustStorePath = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\mykeystore.jks";
            String keyStorePassword = "123456";
            String trustStorePassword = "123456";

            // KeyStore'u yükle
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (FileInputStream keyStoreStream = new FileInputStream(keyStorePath)) {
                keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
            }

            // TrustStore'u yükle
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (FileInputStream trustStoreStream = new FileInputStream(trustStorePath)) {
                trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
            }

            // KeyManagerFactory'i başlat
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, keyStorePassword.toCharArray());

            // TrustManagerFactory'i başlat
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            // SSLContext'u başlat
            javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

            // HttpsURLConnection'u yapılandır
            URL url = new URL("https://192.168.50.91:8443");
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Accept", "application/json");

            // JSON verisini oluştur
            String jsonInputString = "{\"key1\": \"value1\", \"hahahhahah\": \"value2\"}";

            // Veriyi POST et
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonInputString.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            // Yanıtı al
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Sunucudan gelen cevabı oku
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"))) {
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }

                System.out.println("Response Body: " + response.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
