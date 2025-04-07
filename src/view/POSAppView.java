package view;

import model.Department;
import viewmodel.POSViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class POSAppView {
    private JFrame frame;
    private JPanel panel, topPanel, bottomPanel;
    private JButton payButton;
    private POSViewModel viewModel;
    private final Map<Department, JPanel> previewItems = new HashMap<>();

    public POSAppView(POSViewModel viewModel) {
        this.viewModel = viewModel;
        initialize();
    }

    private void initialize() {
        frame = createFrame();
        panel = createPanel();
        topPanel = createTopPanel();
        bottomPanel = createBottomPanel();

        payButton = new JButton("Ödeme Al");
        payButton.setPreferredSize(new Dimension(120, 50));
        payButton.addActionListener(e -> processPayment());

        panel.setLayout(new BorderLayout());
        panel.add(topPanel, BorderLayout.NORTH);
        panel.add(bottomPanel, BorderLayout.CENTER);
        frame.add(panel, BorderLayout.CENTER);  // Add panel with top and bottom panels
        frame.add(payButton, BorderLayout.SOUTH);  // Add payment button at the bottom
        frame.setVisible(true);
    }

    private JFrame createFrame() {
        JFrame frame = new JFrame("POS Uygulaması");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);
        return frame;
    }

    private JPanel createPanel() {
        return new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;

                // Dikey degrade: Beyazdan koyu maviye geçiş
                g2d.setPaint(new GradientPaint(0, 0, Color.WHITE, 0, getHeight(), new Color(0, 0, 139)));
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
    }




    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new GridLayout(1, 4));  // Arrange buttons horizontally
        topPanel.setBackground(new Color(173, 216, 230));

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
        bottomPanel.setBackground(new Color(173, 216, 230));
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

        // Silme butonunu bir ikonla değiştiriyoruz
        ImageIcon deleteIcon = new ImageIcon("C:\\Users\\furka\\IdeaProjects\\DesktopClient\\src\\delete.png");
        deleteIcon = new ImageIcon(deleteIcon.getImage().getScaledInstance(20, 20, Image.SCALE_DEFAULT));  // İkonu boyutlandırma
        JButton deleteBtn = new JButton();
        deleteBtn.setPreferredSize(new Dimension(20, 20));
        deleteBtn.setIcon(deleteIcon);  // İkonu ekliyoruz
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

        bottomPanel.revalidate();  // Görünürlük için yeniden düzenleme
        bottomPanel.repaint();  // Paneli yeniden çizme
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
