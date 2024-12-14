package jmri.jmrit.etcs.dmi.swing;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.jmrit.etcs.ResourceUtil;

/**
 * JPanel containing ERTMS DMI Panel A, Distance to Target Bar.
 * @author Steve Young Copyright (C) 2024
 */
public class DmiPanelA extends JPanel {

    private final JLabel a2Label;
    private final JLabel a4Label;

    private String speedString = "";
    private int distanceToTarget = -10;

    private static final BufferedImage supervisionImage = ResourceUtil.readFile(ResourceUtil.getImageFile("LS_01.bmp"));
    private static final ImageIcon adhesionIcon =  ResourceUtil.getImageIcon("ST_02.bmp");

    public DmiPanelA(@Nonnull DmiPanel mainPanel){
        super();
        setLayout(null);
        setBackground(DmiPanel.BACKGROUND_COLOUR);
        setBounds(0, 15, 54, 300);

        JPanel a1 = getSupervisionImagePanel();
        a2Label = new JLabel();
        // JPanel a2 = new JPanel(); // distance to target, nearest 10m
        JPanel a3 = getDistanceToTargetBarPanel(); // distance to target bar
        JPanel a4 = new JPanel(); // adhesion factor

        a1.setBounds(0, 0, 54, 54);
        a2Label.setBounds(0, 54, 54, 30);
        a3.setBounds(0, 84, 54, 191);
        a4.setBounds(0, 84+191, 54, 25);

        a1.setLayout(null);
        a1.setOpaque(true);

        a2Label.setForeground(DmiPanel.GREY);
        a2Label.setFont(new Font(DmiPanel.FONT_NAME, Font.PLAIN, 14));
        a2Label.setHorizontalAlignment(SwingConstants.RIGHT);

        a4Label = new JLabel();
        a4.add(a4Label);

        // setBg(a2);
        setBg(a3);
        setBg(a4);
        
        add(a1);
        add(a2Label);
        add(a3);
        add(a4);

        DmiPanelA.this.setAdhesionFactorOn(false);
        DmiPanelA.this.setLimitedSupervisionSpeed(-1);
    }

    private JPanel getDistanceToTargetBarPanel(){
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (distanceToTarget < 0 ) {
                    return;
                }
                if (!(g instanceof Graphics2D) ) {
                    throw new IllegalArgumentException("Graphics object passed is not the correct type");
                }
                Graphics2D g2 = (Graphics2D) g;

                RenderingHints  hints =new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHints(hints);
                
                g2.setColor(DmiPanel.GREY);
                drawScale(g2);

                int rectHeight = (int)calculatePositionOnScale(distanceToTarget);
                g2.fillRect(29, 188-rectHeight, 10, rectHeight);
            }
        };
    }

    private static final double LINEAR_SCALE_MAX_DISTANCE = 100.0;
    private static final double LOG_SCALE_MIN_DISTANCE = 100.0;
    private static final double LOG_SCALE_MAX_DISTANCE = 1000.0;
    private static final int TOTAL_PIXELS = 188;
    private static final int FIRST_100M_PIXELS = 33;
    private static final int LOG_SCALE_WIDTH_PIXELS = TOTAL_PIXELS-FIRST_100M_PIXELS;

    // Calculate the position on the scale for a given length in meters
    private static double calculatePositionOnScale(double lengthInMeters) {
        double position;
        if (lengthInMeters <= LINEAR_SCALE_MAX_DISTANCE) {
            // Linear scale for the first 100 meters
            position = (lengthInMeters / LINEAR_SCALE_MAX_DISTANCE) * FIRST_100M_PIXELS;
        } else {
            // Logarithmic scale for lengths beyond 100 meters
            double logScaleFactor = LOG_SCALE_WIDTH_PIXELS / (Math.log(LOG_SCALE_MAX_DISTANCE)
                - Math.log(LOG_SCALE_MIN_DISTANCE));
            position = FIRST_100M_PIXELS + (Math.log(lengthInMeters)
                - Math.log(LOG_SCALE_MIN_DISTANCE)) * logScaleFactor;
        }
        log.debug("at distance {} px: {}",lengthInMeters,position);
        return position;
    }

    private void drawScale(Graphics2D g2){
        g2.drawLine(12, 1, 25, 1); // 1000
        g2.drawLine(12, 2, 25, 2); // 
        g2.drawLine(16, 8, 25, 8); // 900
        g2.drawLine(16, 15, 25, 15); // 800
        g2.drawLine(16, 24, 25, 24); // 700
        g2.drawLine(16, 34, 25, 34); // 600
        g2.drawLine(12, 47, 25, 47); // 500
        g2.drawLine(12, 48, 25, 48); // 
        g2.drawLine(16, 61, 25, 61); // 400
        g2.drawLine(16, 81, 25, 81); // 300
        g2.drawLine(16, 107, 25, 107); // 200
        g2.drawLine(16, 154, 25, 154); // 100
        g2.drawLine(12, 187, 25, 187); // 0
        g2.drawLine(12, 188, 25, 188); // 
    }

    private JPanel getSupervisionImagePanel(){
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (speedString.isEmpty()) {
                    return;
                }
                if (!(g instanceof Graphics2D) ) {
                    throw new IllegalArgumentException("Graphics object passed is not the correct type");
                }
                Graphics2D g2 = (Graphics2D) g;

                RenderingHints  hints =new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setRenderingHints(hints);

                Font font = new Font(DmiPanel.FONT_NAME, Font.PLAIN, 18);
                g2.setFont(font);
                g2.setColor(DmiPanel.GREY);
                
                FontMetrics fm = g2.getFontMetrics();
                int textWidth = fm.stringWidth(speedString);
                int centerX =  ((getWidth()-textWidth) / 2);

                g2.drawImage(supervisionImage, 2, 2, 50, 
                        50, this);
                g2.drawString(speedString, centerX, 33);
            }
        };
    }

    protected void setLimitedSupervisionSpeed(float spd){
        if ( spd < 0 ) {
            speedString="";
        } else {
            speedString = (String.valueOf(Math.round(spd)));
        }
        repaint();
    }

    protected void setAdhesionFactorOn(boolean newVal) {
        a4Label.setIcon(newVal ? adhesionIcon : null);
        a4Label.setToolTipText(newVal ?  Bundle.getMessage("AdhesionFactorOn") : null);
    }

    protected void setDistanceToTarget(float distance) {
        distanceToTarget = Math.round(distance);
        a2Label.setVisible(distanceToTarget >= 0 );
        int nearestTen = ((distanceToTarget + 5) / 10) * 10;
        a2Label.setText(String.valueOf(nearestTen));
        repaint();
    }

    private void setBg(JPanel p){
        p.setBackground(DmiPanel.BACKGROUND_COLOUR);
        p.setBorder(javax.swing.BorderFactory.createLineBorder(Color.black, 1));
    
    }

    protected void advance(int distance) {
        setDistanceToTarget(distanceToTarget - distance);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DmiPanelA.class);

}
