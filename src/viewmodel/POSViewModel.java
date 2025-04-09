package viewmodel;

import com.google.gson.Gson;
import model.Department;
import model.PaymentModel;

import javax.net.ssl.*;
import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;

public class POSViewModel {
    private PaymentModel model;  // Add this line to store PaymentModel
    private List<Department> departments = new ArrayList<>();



    public POSViewModel(PaymentModel model) {  // Ensure this constructor exists
        this.model = model;
        SQLiteHelper.initializeDatabase();

        initDepartments();
    }

    private void initDepartments() {
        departments = SQLiteHelper.getDepartmentsFromDatabase(); // Veritabanından departmanları alıyoruz
    }

    public int getDepartmentsSize() {
        return departments.size();  // Liste boyutunu döndürür
    }

    public List<Department> getDepartments() {
        return departments;
    }

    public void addDepartment(Department dept) {
        departments.add(dept);
    }

    public void increaseCount(Department dept) {
        dept.setDepCount(dept.getDepCount() + 1);
    }

    public void decreaseCount(Department dept) {
        if (dept.getDepCount() > 0)
            dept.setDepCount(dept.getDepCount() - 1);
    }

    public String getPreviewJson(Map<Department, JPanel> previewItems) {
        return new Gson().toJson(previewItems.keySet().stream().map(dept -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", dept.getDepName());
            map.put("price", dept.getDepPrice());
            map.put("count", dept.getDepCount());
            map.put("kdv", dept.getDepKdv());
            return map;
        }).toArray());
    }

    public String processPaymentAndSend(Map<Department, JPanel> previewItems) {
        if (!model.processPayment()) return "Ödeme başarısız!";

        String jsonData = getPreviewJson(previewItems);
        boolean sent = sendDataOverHttps(jsonData);
        return sent ? "Ödeme alındı ve gönderildi!" : "Ödeme alındı ama gönderilemedi!";
    }

    private boolean sendDataOverHttps(String jsonData) {
        try {
            SSLContext sslContext = createSSLContext();
            URL url = new URL("https://192.168.50.91:8443");
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
            conn.setSSLSocketFactory(sslContext.getSocketFactory());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonData.getBytes(StandardCharsets.UTF_8));
            }

            int responseCode = conn.getResponseCode();
            System.out.println("HTTP response code: " + responseCode);
            return responseCode == 200;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private SSLContext createSSLContext() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new FileInputStream("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-keystore.p12"), "123456".toCharArray());

        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, "123456".toCharArray());

        KeyStore trustStore = KeyStore.getInstance("PKCS12");
        trustStore.load(new FileInputStream("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-truststore.p12"), "123456".toCharArray());

        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return sslContext;
    }
}
