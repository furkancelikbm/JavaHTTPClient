package view;

import model.Department;
import viewmodel.POSViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class POSAppView {
    private JFrame frame;
    private GradientPanel panel;
    private JPanel topPanel, bottomPanel;
    private JButton payButton;
    private POSViewModel viewModel;
    private final Map<Department, JPanel> previewItems = new HashMap<>();

    public POSAppView(POSViewModel viewModel) {
        this.viewModel = viewModel;
        initialize();
    }

    private void initialize() {
        frame = createFrame();
        panel = new GradientPanel();
        topPanel = createTopPanel();
        bottomPanel = createBottomPanel();

        payButton = new JButton("Ödeme Al");
        payButton.setPreferredSize(new Dimension(120, 50));
        payButton.addActionListener(e -> processPayment());

        JButton departmentManagementButton = new JButton("Departman Yönetimi");
        departmentManagementButton.setPreferredSize(new Dimension(160, 50));
        departmentManagementButton.addActionListener(e -> openDepartmentManagement());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(payButton);
        buttonPanel.add(departmentManagementButton);

        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(buttonPanel, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("POS Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        return frame;
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setOpaque(false);
        for (Department dept : viewModel.getDepartments()) {
            JButton button = new JButton(generateHtmlButton(dept));
            styleDepartmentButton(button);
            button.addActionListener(e -> handleDepartmentClick(dept));
            panel.add(button);
        }
        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        panel.setOpaque(false);
        return panel;
    }

    private void handleDepartmentClick(Department dept) {
        viewModel.increaseCount(dept);
        updatePreview(dept);
    }

    private void updatePreview(Department dept) {
        if (previewItems.containsKey(dept)) {
            JButton mainBtn = (JButton) previewItems.get(dept).getComponent(0);
            mainBtn.setText(generateHtml(dept));
        } else {
            addPreviewItem(dept);
        }
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private void addPreviewItem(Department dept) {
        JPanel itemPanel = new JPanel(new BorderLayout());
        itemPanel.setOpaque(false);

        JButton previewBtn = new JButton(generateHtml(dept));
        styleDepartmentButton(previewBtn);

        JButton deleteBtn = new JButton("X");
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.addActionListener(e -> removeItem(dept, itemPanel, previewBtn));

        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deletePanel.setOpaque(false);
        deletePanel.add(deleteBtn);

        itemPanel.add(previewBtn, BorderLayout.CENTER);
        itemPanel.add(deletePanel, BorderLayout.NORTH);

        bottomPanel.add(itemPanel);
        previewItems.put(dept, itemPanel);
    }

    private void removeItem(Department dept, JPanel itemPanel, JButton previewBtn) {
        viewModel.decreaseCount(dept);
        if (dept.getDepCount() <= 0) {
            bottomPanel.remove(itemPanel);
            previewItems.remove(dept);
        } else {
            previewBtn.setText(generateHtml(dept));
        }
        bottomPanel.revalidate();
        bottomPanel.repaint();
    }

    private void processPayment() {
        String result = viewModel.processPaymentAndSend(previewItems);
        JOptionPane.showMessageDialog(frame, result);
    }

    private void openDepartmentManagement() {
        JTextField nameField = new JTextField();
        JTextField priceField = new JTextField();
        JTextField kdvField = new JTextField();
        JTextField countField = new JTextField();

        Object[] message = {
                "Departman Adı:", nameField,
                "Fiyat:", priceField,
                "KDV (%):", kdvField,
                "Adet:", countField
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Departman Ekle", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String depName = nameField.getText();
                double depPrice = Double.parseDouble(priceField.getText());
                double depKdv = Double.parseDouble(kdvField.getText());
                int depCount = Integer.parseInt(countField.getText());

                Department dept = new Department(depName, depKdv, depPrice, depCount);

                // 💾 Veritabanına kaydet
                viewmodel.SQLiteHelper.addDepartmentToDatabase(dept);

                // ➕ ViewModel'e de ekle
                viewModel.addDepartment(dept);

                // 🖼️ UI'yi güncelle
                updateDepartmentsUI();

            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(frame, "Geçersiz sayı girdiniz!", "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void updateDepartmentsUI() {
        topPanel.removeAll();
        for (Department dept : viewModel.getDepartments()) {
            JButton button = new JButton(generateHtmlButton(dept));
            styleDepartmentButton(button);
            button.addActionListener(e -> handleDepartmentClick(dept));
            topPanel.add(button);
        }
        topPanel.revalidate();
        topPanel.repaint();
    }

    private String generateHtml(Department dept) {
        return String.format("<html><div style='text-align: center;'>"
                        + "<span style='font-size: 10px;'>%s</span><br>"
                        + "<span style='font-size: 8px;'>Price: %.2f</span><br>"
                        + "<span style='font-size: 8px;'>Count: %d</span><br>"
                        + "<span style='font-size: 8px;'>KDV: %.0f%%</span></div></html>",
                dept.getDepName(), dept.getDepPrice(), dept.getDepCount(), dept.getDepKdv());
    }

    private String generateHtmlButton(Department dept) {
        return String.format("<html><div style='text-align: center;'>"
                        + "<span style='font-size: 10px;'>%s</span><br>"
                        + "<span style='font-size: 8px;'>Price: %.2f</span><br>"
                        + "<span style='font-size: 8px;'>KDV: %.0f%%</span></div></html>",
                dept.getDepName(), dept.getDepPrice(), dept.getDepKdv());
    }

    private void styleDepartmentButton(JButton button) {
        button.setPreferredSize(new Dimension(100, 100));
        button.setFont(new Font("Arial", Font.PLAIN, 10));
        button.setMargin(new Insets(5, 5, 5, 5));
    }
}
