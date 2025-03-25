import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;

public class HttpsClient {
    public static void main(String[] args) {
        try {
            // KeyStore'u başlat (CA sertifikalarını içerecek)
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            // CA sertifikasını yükleyin
            try (InputStream caInput = new FileInputStream("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\rootCA.crt")) {
                keyStore.load(null, null); // Boş KeyStore oluştur
                // Burada sadece CA sertifikasını yükleyin
                keyStore.setCertificateEntry("rootCA", java.security.cert.CertificateFactory.getInstance("X.509").generateCertificate(caInput));
            }

            // TrustManagerFactory ile CA sertifikasını doğrulama
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // SSLContext'i başlat
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), new java.security.SecureRandom());

            // HTTPS bağlantısı kurma
            URL url = new URL("https://192.168.1.61:8443");  // Sunucu IP adresi ve portu
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("GET");

            // İsteği gönder ve sunucudan gelen yanıtı oku
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Yanıtı yazdır
            System.out.println("Response: " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
