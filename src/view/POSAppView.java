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
        panel = new GradientPanel(); // Artık özel panel kullanılıyor
        topPanel = createTopPanel();
        bottomPanel = createBottomPanel();

        payButton = new JButton("Ödeme Al");
        payButton.setPreferredSize(new Dimension(120, 50));
        payButton.addActionListener(e -> processPayment());

        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.CENTER);
        frame.add(payButton, BorderLayout.SOUTH);
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("POS Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        return frame;
    }


    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(1, 4));
        topPanel.setOpaque(false); // Arka planı degrade göster
        for (Department dept : viewModel.getDepartments()) {
            JButton button = new JButton(generateHtml(dept));
            styleDepartmentButton(button);
            button.addActionListener(e -> handleDepartmentClick(dept));
            topPanel.add(button);
        }
        return topPanel;
    }

    private JPanel createBottomPanel() {
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        bottomPanel.setOpaque(false); // degrade arkaya uyumlu
        return bottomPanel;
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

        ImageIcon deleteIcon = new ImageIcon("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\delete.png");
        deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));
        JButton deleteBtn = new JButton();
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.setIcon(deleteIcon);
        deleteBtn.setBorderPainted(false);
        deleteBtn.setContentAreaFilled(false);
        deleteBtn.addActionListener(e -> removeItem(dept, itemPanel, previewBtn));

        JPanel deletePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        deletePanel.setOpaque(false);
        deletePanel.add(deleteBtn);

        itemPanel.add(previewBtn, BorderLayout.CENTER);
        itemPanel.add(deletePanel, BorderLayout.NORTH);

        bottomPanel.add(itemPanel);
        previewItems.put(dept, itemPanel);

        bottomPanel.revalidate();
        bottomPanel.repaint();
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

    private void processPayment() {
        String result = viewModel.processPaymentAndSend(previewItems);
        JOptionPane.showMessageDialog(frame, result);
    }
}
