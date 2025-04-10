package view;

import model.Department;
import viewmodel.POSViewModel;
import repository.SQLiteHelper;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

import static repository.SQLiteHelper.removeDepartmentFromDatabase;

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

        payButton = new JButton("\uD83D\uDCB3 Ödeme Al");
        payButton.setPreferredSize(new Dimension(120, 50));
        payButton.addActionListener(e -> processPayment());

        JButton departmentManagementButton = new JButton("➕ Departman Ekle");
        departmentManagementButton.setPreferredSize(new Dimension(160, 50));
        departmentManagementButton.addActionListener(e -> openDepartmentManagement());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.add(payButton);
        buttonPanel.add(departmentManagementButton);

        JButton deleteDepartmentButton = new JButton("❌ Departman Sil");
        deleteDepartmentButton.setPreferredSize(new Dimension(160, 50));
        deleteDepartmentButton.addActionListener(e -> deleteDepartment());

        buttonPanel.add(deleteDepartmentButton); // Ekledim

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

        ImageIcon originalIcon = new ImageIcon("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\delete.png"); // PNG dosyanın yolu
        Image scaledImage = originalIcon.getImage().getScaledInstance(20 , 20, Image.SCALE_SMOOTH);
        ImageIcon deleteIcon = new ImageIcon(scaledImage);

        JButton deleteBtn = new JButton(deleteIcon);
        deleteBtn.setPreferredSize(new Dimension(20, 20)); // Butonun boyutu
        deleteBtn.setToolTipText("Delete");
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
        JTextField countField = new JTextField("1"); // Set default value to 1

        // Disable the count field to prevent user input
        countField.setEditable(false);

        Object[] message = {
                "Departman Adı:", nameField,
                "Fiyat:", priceField,
                "KDV (%):", kdvField,
        };

        int option = JOptionPane.showConfirmDialog(frame, message, "Departman Ekle", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                String depName = nameField.getText();
                double depPrice = Double.parseDouble(priceField.getText());
                double depKdv = Double.parseDouble(kdvField.getText());
                int depCount = 1; // Always set to 1

                // Create the Department object
                Department dept = new Department(depName, depKdv, depPrice, depCount);

                // Save to database
                SQLiteHelper.addDepartmentToDatabase(dept);

                // Add to ViewModel
                viewModel.addDepartment(dept);

                // Update the UI
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

    private void deleteDepartment() {
        java.util.List<Department> departments = viewModel.getDepartments();
        if (departments.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Silinecek departman yok!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Department[] depArray = departments.toArray(new Department[0]);
        Department selected = (Department) JOptionPane.showInputDialog(
                frame,
                "Silinecek Departmanı Seçin:",
                "Departman Sil",
                JOptionPane.PLAIN_MESSAGE,
                null,
                depArray,
                depArray[0]
        );

        if (selected != null) {
            // Veritabanından sil
            removeDepartmentFromDatabase(selected);

            // ViewModel'den çıkar
            viewModel.removeDepartment(selected);

            // UI'yi güncelle
            updateDepartmentsUI();

            JOptionPane.showMessageDialog(frame, "Departman silindi: " + selected.getDepName());
        }
    }
}
