import com.google.gson.Gson;

import javax.net.ssl.*;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.URL;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;


public class POSAppView {
    private static final String KEYSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-keystore.p12";
    private static final String TRUSTSTORE_PATH = "C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\client-truststore.p12";
    private static final char[] STORE_PASSWORD = "123456".toCharArray();
    private static final String SERVER_URL = "https://192.168.1.61:8443";

    private JFrame frame;
    private JPanel panel, topPanel, bottomPanel;
    private JButton payButton;
    private POSViewModel viewModel;
    private Map<Department, JButton> previewButtons = new HashMap<>();

    public POSAppView(POSViewModel viewModel) {
        this.viewModel = viewModel;
        initialize();
    }

    private void initialize() {
        frame = new JFrame("POS Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        panel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setPaint(new GradientPaint(0, 0, new Color(173, 216, 230), getWidth(), 0, new Color(11, 77, 166)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        topPanel = new JPanel(new GridLayout(1, 4));
        topPanel.setBackground(new Color(173, 216, 230));

        Department[] departments = {
                new Department("D1", 1, 18.0, 0),
                new Department("D2", 10, 8.0, 0),
                new Department("D3", 20, 15.0, 0),
                new Department("D4", 30, 20.0, 0)
        };

        for (Department dept : departments) {
            JButton button = new JButton(generateHtml(dept));
            styleDepartmentButton(button);
            button.addActionListener(e -> handleDepartmentClick(dept, button));
            topPanel.add(button);
        }

        bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setBackground(new Color(173, 216, 230));

        payButton = new JButton("Ödeme Al");
        payButton.setPreferredSize(new Dimension(120, 50));
        payButton.addActionListener(e -> {
            String result = viewModel.processPayment();
            JOptionPane.showMessageDialog(frame, result);

            String json = generatePreviewJson();
            sendDataOverHttps(json);
        });

        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        frame.add(payButton, BorderLayout.SOUTH);
        frame.add(panel);
        frame.setVisible(true);
    }

    private void handleDepartmentClick(Department dept, JButton button) {
        dept.setDepCount(dept.getDepCount() + 1);
        button.setText(generateHtml(dept));

        previewButtons.compute(dept, (key, previewButton) -> {
            if (previewButton == null) {
                previewButton = createPreviewButton(dept);
                bottomPanel.add(previewButton);
            } else {
                previewButton.setText(generateHtml(dept));
            }
            bottomPanel.revalidate();
            bottomPanel.repaint();
            return previewButton;
        });
    }

    private JButton createPreviewButton(Department dept) {
        JButton preview = new JButton(generateHtml(dept));
        styleDepartmentButton(preview);

        JButton deleteBtn = new JButton("🗑️");
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.addActionListener(e -> {
            dept.setDepCount(dept.getDepCount() - 1);
            if (dept.getDepCount() <= 1) {
                bottomPanel.remove(preview);
                previewButtons.remove(dept);
            } else {
                preview.setText(generateHtml(dept));
            }
            bottomPanel.revalidate();
            bottomPanel.repaint();
        });

        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deletePanel.setOpaque(false);
        deletePanel.add(deleteBtn);
        preview.setLayout(new BorderLayout());
        preview.add(deletePanel, BorderLayout.NORTH);

        return preview;
    }

    private String generateHtml(Department dept) {
        return String.format("<html><div style='text-align: center;'>"
                        + "<span style='font-size: 10px;'>%s</span><br>"
                        + "<span style='font-size: 8px;'>Price: %.2f</span><br>"
                        + "<span style='font-size: 8px;'>Count: %d</span><br>"
                        + "<span style='font-size: 8px;'>KDV: %.0f%%</span></div></html>",
                dept.getDepName(), dept.getDepPrice(), dept.getDepCount(), dept.getDepKdv());
    }


    private void styleDepartmentButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 100));
        button.setMargin(new Insets(10, 10, 10, 10));
        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.CENTER);
        button.setFont(new Font("Arial", Font.PLAIN, 10));
    }

    private String generatePreviewJson() {
        Gson gson = new Gson();
        StringBuilder jsonBuilder = new StringBuilder("[");
        previewButtons.keySet().forEach(dept -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", dept.getDepName());
            map.put("price", dept.getDepPrice());
            map.put("count", dept.getDepCount());
            map.put("kdv", dept.getDepKdv());
            jsonBuilder.append(gson.toJson(map)).append(",");
        });
        if (jsonBuilder.length() > 1) jsonBuilder.setLength(jsonBuilder.length() - 1);
        jsonBuilder.append("]");
        return jsonBuilder.toString();
    }

    private void sendDataOverHttps(String jsonData) {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");

            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new FileInputStream(KEYSTORE_PATH), STORE_PASSWORD);

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, STORE_PASSWORD);

            KeyStore trustStore = KeyStore.getInstance("PKCS12");
            trustStore.load(new FileInputStream(TRUSTSTORE_PATH), STORE_PASSWORD);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);

            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

            URL url = new URL(SERVER_URL);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setSSLSocketFactory(sslContext.getSocketFactory());
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            try (OutputStream os = connection.getOutputStream()) {
                os.write(jsonData.getBytes("UTF-8"));
            }

            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = reader.readLine()) != null) response.append(line);
                System.out.println("Response: " + response);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

