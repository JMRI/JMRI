package jmri.jmrit.throttle.utils;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JPanel;

public class TranslucentJPanel extends JPanel {

    private final Color TRANS_COL = new Color(100, 100, 100, 100);

    public TranslucentJPanel() {
        super();
        setOpaque(false);
    }

    @Override
    public void paintComponent(Graphics g) {
        g.setColor(TRANS_COL);
        g.fillRoundRect(0, 0, getSize().width, getSize().height, 10, 10);
        super.paintComponent(g);
    }
}