import org.json.JSONObject;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class HttpsClient {

    public static void main(String[] args) throws Exception {
        // Enable SSL/TLS debugging (Handshake details, etc.)
        System.setProperty("javax.net.debug", "ssl,handshake,verbose");

        // Enable HTTP connection debugging (request/response)
        System.setProperty("java.net.debug", "all");

        // Load the custom CA certificate
        FileInputStream fis = new FileInputStream("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\rootCA.crt");
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);

        // Create a KeyStore containing the certificate
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        keyStore.load(null, null);  // Initialize empty KeyStore
        keyStore.setCertificateEntry("rootCA", cert);

        // Create a TrustManager that trusts the loaded certificate
        javax.net.ssl.TrustManagerFactory trustManagerFactory = javax.net.ssl.TrustManagerFactory.getInstance(javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        javax.net.ssl.TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        // Create an SSLContext using the TrustManager
        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext.getInstance("TLS");
        sslContext.init(null, trustManagers, new java.security.SecureRandom());

        // Set the default SSLContext to the one we just created
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

        // Prepare the URL and open connection
        URL url = new URL("https://192.168.50.91:8443");  // Ensure the correct endpoint
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        // Set the HTTP method to POST
        connection.setRequestMethod("POST");

        // Set the request headers
        connection.setRequestProperty("Content-Type", "application/json");  // JSON content type

        // Set connection and read timeouts
        connection.setConnectTimeout(5000);  // Timeout for establishing connection (5 seconds)
        connection.setReadTimeout(5000);     // Timeout for reading the response (5 seconds)

        // Enable input and output streams
        connection.setDoOutput(true);
        connection.setDoInput(true);

        // Create a JSONObject with your data
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", "hahahahahahaha");
        jsonObject.put("age", 33330);

        // Write the JSON data to the output stream
        try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
            osw.write(jsonObject.toString());  // Use jsonObject.toString() here
            osw.flush();
        }

        // Get the response code and print it
        int responseCode = connection.getResponseCode();
        System.out.println("Response Code: " + responseCode);

        // Read the response
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
            String inputLine;
            StringBuilder response = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            System.out.println("Response: " + response.toString());
        }

        // Close connection
        connection.disconnect();
    }
}
