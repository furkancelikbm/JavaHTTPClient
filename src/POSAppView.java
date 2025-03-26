import com.google.gson.Gson;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

public class POSAppView {
    private JFrame frame;
    private JPanel panel;
    private JPanel topPanel;
    private JPanel bottomPanel;
    private JButton payButton;
    private POSViewModel viewModel;
    private Map<Department, JButton> previewButtons; // To store preview buttons for each department

    public POSAppView(POSViewModel viewModel) {
        this.viewModel = viewModel;
        this.previewButtons = new HashMap<>();
        initialize();
    }

    private void initialize() {
        frame = new JFrame("POS Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400); // Increase frame size to allow space for buttons

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                Color color1 = new Color(173, 216, 230);
                Color color2 = new Color(11, 77, 166);
                GradientPaint gradient = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gradient);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(new BorderLayout());

        topPanel = new JPanel();
        topPanel.setLayout(new GridLayout(1, 4));
        topPanel.setBackground(new Color(173, 216, 230));

        // Sample data for departments
        Department[] departments = {
                new Department("D1", 1, 18.0, 0),  // depId, depPrice, depCount, depKdv
                new Department("D2", 10, 8.0, 0),
                new Department("D3", 20, 15.0, 0),
                new Department("D4", 30, 20.0, 0)
        };

        for (int i = 0; i < departments.length; i++) {
            Department dept = departments[i];
            JButton button = new JButton("<html><div style='text-align: center;'>" +
                    "<span style='font-size: 10px;'>" + dept.getDepName() + "</span><br>" +
                    "<span style='font-size: 8px;'>Price: " + dept.getDepPrice() + "</span><br>" +
                    "<span style='font-size: 8px;'>Count: " + dept.getDepCount() + "</span><br>" +
                    "<span style='font-size: 8px;'>KDV: " + dept.getDepKdv() + "%</span></div></html>");
            button.setPreferredSize(new Dimension(100, 100));  // Increase button size
            button.setMargin(new Insets(10, 10, 10, 10));
            button.setVerticalTextPosition(SwingConstants.CENTER);
            button.setHorizontalTextPosition(SwingConstants.CENTER);
            button.setFont(new Font("Arial", Font.PLAIN, 10)); // Set smaller font size for button text
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dept.setDepCount(dept.getDepCount() + 1);
                    updateButtonText(dept, button);

                    if (dept.getDepCount() > 1) {
                        // Create preview button with delete button
                        JButton previewButton = previewButtons.get(dept);
                        if (previewButton == null) {
                            previewButton = createPreviewButton(dept);
                            previewButtons.put(dept, previewButton);
                            bottomPanel.add(previewButton);
                            bottomPanel.revalidate();
                            bottomPanel.repaint();
                        } else {
                            updatePreviewButtonText(dept, previewButton);
                        }
                    }
                }
            });

            topPanel.add(button);
        }

        // Panel for preview buttons (horizontal flow)
        bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(173, 216, 230));
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Horizontal flow for preview buttons

        // Pay button (fixed at the bottom)
        payButton = new JButton("Ödeme Al");
        payButton.setPreferredSize(new Dimension(120, 50)); // Adjust the size to fit
        payButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Call the ViewModel to process the payment
                String resultMessage = viewModel.processPayment();
                JOptionPane.showMessageDialog(frame, resultMessage);

                // Log the preview data as JSON and send it to the server
                String previewDataJson = logPreviewDataAsJson();
                sendPreviewDataToServer(previewDataJson);
            }
        });

        // Add preview buttons and pay button to the panel
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);  // Preview buttons appear above the pay button

        // Add the fixed pay button at the bottom
        frame.add(payButton, BorderLayout.SOUTH);  // This ensures it's fixed at the bottom

        frame.add(panel);
        frame.setVisible(true);
    }

    private void updateButtonText(Department dept, JButton button) {
        button.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 10px;'>" + dept.getDepName() + "</span><br>" +
                "<span style='font-size: 8px;'>Price: " + dept.getDepPrice() + "</span><br>" +
                "<span style='font-size: 8px;'>Count: " + dept.getDepCount() + "</span><br>" +
                "<span style='font-size: 8px;'>KDV: " + dept.getDepKdv() + "%</span></div></html>");
    }

    private JButton createPreviewButton(Department dept) {
        // Create the preview button (not a panel anymore)
        JButton previewButton = new JButton("<html><div style='text-align: center;'>" +
                "<span style='font-size: 10px;'>" + dept.getDepName() + "</span><br>" +
                "<span style='font-size: 8px;'>Price: " + dept.getDepPrice() + "</span><br>" +
                "<span style='font-size: 8px;'>Count: " + dept.getDepCount() + "</span><br>" +
                "<span style='font-size: 8px;'>KDV: " + dept.getDepKdv() + "%</span></div></html>");
        previewButton.setPreferredSize(new Dimension(100, 100)); // Ensure a larger button size
        previewButton.setMargin(new Insets(10, 10, 10, 10));
        previewButton.setVerticalTextPosition(SwingConstants.CENTER);
        previewButton.setHorizontalTextPosition(SwingConstants.CENTER);
        previewButton.setFont(new Font("Arial", Font.PLAIN, 10));

        // Create and add the delete button (use a panel for positioning the delete button)
        JButton deleteButton = new JButton("🗑️");
        deleteButton.setPreferredSize(new Dimension(20, 20));
        deleteButton.setBackground(Color.RED);
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFont(new Font("Arial", Font.PLAIN, 12));
        deleteButton.setBorderPainted(false);
        deleteButton.setFocusPainted(false);
        deleteButton.setContentAreaFilled(false);
        deleteButton.setAlignmentX(Component.RIGHT_ALIGNMENT);

        // ActionListener for deleting preview button
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dept.setDepCount(dept.getDepCount() - 1);  // Decrement count
                updatePreviewButtonText(dept, previewButton);

                if (dept.getDepCount() <= 1) {
                    bottomPanel.remove(previewButton);  // Remove preview button
                    bottomPanel.revalidate();
                    bottomPanel.repaint();
                }
            }
        });

        // Create a panel for delete button
        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deletePanel.add(deleteButton);

        // Add the delete panel to the preview button
        previewButton.setLayout(new BorderLayout());
        previewButton.add(deletePanel, BorderLayout.NORTH);

        return previewButton;  // Return the preview button with delete button
    }


    private void updatePreviewButtonText(Department dept, JButton previewButton) {
        previewButton.setText("<html><div style='text-align: center;'>" +
                "<span style='font-size: 10px;'>" + dept.getDepName() + "</span><br>" +
                "<span style='font-size: 8px;'>Price: " + dept.getDepPrice() + "</span><br>" +
                "<span style='font-size: 8px;'>Count: " + dept.getDepCount() + "</span><br>" +
                "<span style='font-size: 8px;'>KDV: " + dept.getDepKdv() + "%</span></div></html>");
    }

    // Method to log the preview data as JSON
    private String logPreviewDataAsJson() {
        // Create a list of preview data to log
        StringBuilder jsonBuilder = new StringBuilder();
        jsonBuilder.append("[");

        // Loop through each department and its associated preview button to get the data
        Gson gson = new Gson();
        for (Map.Entry<Department, JButton> entry : previewButtons.entrySet()) {
            Department dept = entry.getKey();
            Map<String, Object> deptData = new HashMap<>();
            deptData.put("name", dept.getDepName());
            deptData.put("price", dept.getDepPrice());
            deptData.put("count", dept.getDepCount());
            deptData.put("kdv", dept.getDepKdv());

            // Convert each department data to JSON and append it to the string
            jsonBuilder.append(gson.toJson(deptData));
            jsonBuilder.append(",");
        }

        // Remove the last comma and close the JSON array
        if (jsonBuilder.length() > 1) {
            jsonBuilder.deleteCharAt(jsonBuilder.length() - 1);
        }
        jsonBuilder.append("]");

        // Return the JSON data as a string
        return jsonBuilder.toString();
    }

    private void sendPreviewDataToServer(String previewDataJson) {
        try {
            // Load the custom CA certificate
            FileInputStream fis = new FileInputStream("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\rootCA.crt");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);

            // Create a KeyStore containing the certificate
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);  // Initialize empty KeyStore
            keyStore.setCertificateEntry("rootCA", cert);

            // Create a TrustManager that trusts the loaded certificate
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

            // Create an SSLContext using the TrustManager
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, new java.security.SecureRandom());

            // Set the default SSLContext to the one we just created
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            // Prepare the URL and open connection
            URL url = new URL("https://192.168.50.91:8443");  // Ensure the correct endpoint
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

            // Set request method and headers
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // Enable input and output streams
            connection.setDoOutput(true);

            // Write the JSON data to the output stream
            try (OutputStreamWriter osw = new OutputStreamWriter(connection.getOutputStream(), "UTF-8")) {
                osw.write(previewDataJson);  // Send the preview data as JSON
                osw.flush();
            }

            // Get the response code and print it
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            // Read the response from the server
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                System.out.println("Response: " + response.toString());
            }

            connection.disconnect();
        } catch (Exception e) {
            if (e instanceof IOException) {
                e.printStackTrace();  // Handle IOException specifically
            } else {
                e.printStackTrace();  // Handle other exceptions
            }
        }
    }
}
