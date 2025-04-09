package view;

import javax.swing.*;
import java.awt.*;

public class GradientPanel extends JPanel {
    private Color startColor = Color.WHITE;
    private Color endColor = new Color(0, 0, 139);

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setPaint(new GradientPaint(0, 0, startColor, 0, getHeight(), endColor));
        g2d.fillRect(0, 0, getWidth(), getHeight());
    }
}
