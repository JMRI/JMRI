package jmri.jmrit.analogclock;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;

/**
 * Creates a JFrame containing an analog clockface and hands.
 * <p>
 * Time code copied from code for the Nixie clock by Bob Jacobsen
 *
 * @author Dennis Miller Copyright (C) 2004
 */
public class AnalogClockFrame extends JmriJFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    Timebase clock;
    double minuteAngle;
    double hourAngle;
    String amPm;

    public AnalogClockFrame() {
        super(Bundle.getMessage("MenuItemAnalogClock"));

        clock = InstanceManager.getDefault(jmri.Timebase.class);

        // listen for changes to the Timebase parameters
        clock.addPropertyChangeListener(this);

        // init GUI
        setPreferredSize(new java.awt.Dimension(200, 200));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel analogClockPanel = new ClockPanel();
        analogClockPanel.setOpaque(true);
        getContentPane().add(analogClockPanel);

        JPanel buttonPanel = new JPanel();
        // Need to put a Box Layout on the panel to ensure the run/stop button is centered
        // Without it, the button does not center properly
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(b = new JButton(Bundle.getMessage("ButtonPauseClock")));
        if (!clock.getRun()) {
            b.setText(Bundle.getMessage("ButtonRunClock"));
        }
        b.addActionListener(new ButtonListener());
        b.setOpaque(true);
        b.setVisible(true);
        getContentPane().add(buttonPanel);
        // since Run/Stop button is not to evryones taste, user may turn it on in clock prefs
        buttonPanel.setVisible(clock.getShowStopButton()); // pick up clock prefs choice
        // get ready to display
        pack();
        update();  // set proper time

        // request callback to update time
        clock.addMinuteChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });

    }

    public class ClockPanel extends JPanel {

        // Create a Panel that has clockface drawn on it scaled to the size of the panel
        // Define common variables
        Image logo;
        Image scaledLogo;
        NamedIcon jmriIcon;
        NamedIcon scaledIcon;
        int hourX[] = {-12, -11, -25, -10, -10, 0, 10, 10, 25, 11, 12};
        int hourY[] = {-31, -163, -170, -211, -276, -285, -276, -211, -170, -163, -31};
        int minuteX[] = {-12, -11, -24, -11, -11, 0, 11, 11, 24, 11, 12};
        int minuteY[] = {-31, -261, -266, -314, -381, -391, -381, -314, -266, -261, -31};
        int scaledHourX[] = new int[hourX.length];
        int scaledHourY[] = new int[hourY.length];
        int scaledMinuteX[] = new int[minuteX.length];
        int scaledMinuteY[] = new int[minuteY.length];
        int rotatedHourX[] = new int[hourX.length];
        int rotatedHourY[] = new int[hourY.length];
        int rotatedMinuteX[] = new int[minuteX.length];
        int rotatedMinuteY[] = new int[minuteY.length];

        Polygon hourHand;
        Polygon scaledHourHand;
        Polygon minuteHand;
        Polygon scaledMinuteHand;
        int minuteHeight;
        int hourHeight;
        double scaleRatio;
        int faceSize;
        int panelWidth;
        int panelHeight;
        int size;
        int logoWidth;
        int logoHeight;

        // centreX, centreY are the coordinates of the centre of the clock
        int centreX;
        int centreY;

        public ClockPanel() {
            // Load the JMRI logo and hands to put on the clock
            // Icons are the original size version kept for to allow for mulitple resizing
            // and scaled Icons are the version scaled for the panel size
            jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
            scaledIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
            logo = jmriIcon.getImage();

            // Create an unscaled set of hands to get the original size (height)to use
            // in the scaling calculations
            hourHand = new Polygon(hourX, hourY, 11);
            hourHeight = hourHand.getBounds().getSize().height;
            minuteHand = new Polygon(minuteX, minuteY, 11);
            minuteHeight = minuteHand.getBounds().getSize().height;

            amPm = "AM";

            // Add component listener to handle frame resizing event
            this.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    scaleFace();
                }
            });

        }

        @Override
        public void paint(Graphics g) {

            // overridden Paint method to draw the clock
            g.translate(centreX, centreY);

            // Draw the clockface outline scaled to the panel size with a dot in the middle and
            // rings for the hands
            g.setColor(Color.white);
            g.fillOval(-faceSize / 2, -faceSize / 2, faceSize, faceSize);
            g.setColor(Color.black);
            g.drawOval(-faceSize / 2, -faceSize / 2, faceSize, faceSize);

            int dotSize = faceSize / 40;
            g.fillOval(-dotSize * 2, -dotSize * 2, 4 * dotSize, 4 * dotSize);
            g.setColor(Color.white);
            g.fillOval(-dotSize, -dotSize, 2 * dotSize, 2 * dotSize);
            g.setColor(Color.black);
            g.fillOval(-dotSize / 2, -dotSize / 2, dotSize, dotSize);

            // Draw the JMRI logo
            g.drawImage(scaledLogo, -logoWidth / 2, -faceSize / 4, logoWidth, logoHeight, this);

            // Draw the hour and minute markers
            int dashSize = size / 60;
            for (int i = 0; i < 360; i = i + 6) {
                g.drawLine(dotX(faceSize / 2., i), dotY(faceSize / 2., i), dotX(faceSize / 2. - dashSize, i), dotY(faceSize / 2. - dashSize, i));
            }
            for (int i = 0; i < 360; i = i + 30) {
                g.drawLine(dotX(faceSize / 2., i), dotY(faceSize / 2., i), dotX(faceSize / 2. - 3 * dashSize, i), dotY(faceSize / 2. - 3 * dashSize, i));
            }

            // Add the hour digits, with the fontsize scaled to the clock size
            int fontSize = faceSize / 10;
            if (fontSize < 1) {
                fontSize = 1;
            }
            Font sizedFont = new Font("Serif", Font.PLAIN, fontSize);
            g.setFont(sizedFont);
            FontMetrics fontM = g.getFontMetrics(sizedFont);

            for (int i = 0; i < 12; i++) {
                String hour = Integer.toString(i + 1);
                int xOffset = fontM.stringWidth(hour);
                int yOffset = fontM.getHeight();
                g.drawString(Integer.toString(i + 1), dotX(faceSize / 2 - 6 * dashSize, i * 30 - 60) - xOffset / 2, dotY(faceSize / 2 - 6 * dashSize, i * 30 - 60) + yOffset / 4);
            }

            // Draw hour hand rotated to appropriate angle
            // Calculation mimics the AffineTransform class calculations in Graphics2D
            // Graphics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
            double minuteAngleRad = Math.toRadians(minuteAngle);
            for (int i = 0; i < scaledMinuteX.length; i++) {
                rotatedMinuteX[i] = (int) (scaledMinuteX[i] * Math.cos(minuteAngleRad) - scaledMinuteY[i] * Math.sin(minuteAngleRad));
                rotatedMinuteY[i] = (int) (scaledMinuteX[i] * Math.sin(minuteAngleRad) + scaledMinuteY[i] * Math.cos(minuteAngleRad));
            }
            scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY, rotatedMinuteX.length);

            double hourAngleRad = Math.toRadians(hourAngle);
            for (int i = 0; i < scaledHourX.length; i++) {
                rotatedHourX[i] = (int) (scaledHourX[i] * Math.cos(hourAngleRad) - scaledHourY[i] * Math.sin(hourAngleRad));
                rotatedHourY[i] = (int) (scaledHourX[i] * Math.sin(hourAngleRad) + scaledHourY[i] * Math.cos(hourAngleRad));
            }
            scaledHourHand = new Polygon(rotatedHourX, rotatedHourY, rotatedHourX.length);

            g.fillPolygon(scaledHourHand);
            g.fillPolygon(scaledMinuteHand);

            // Draw AM/PM indicator in slightly smaller font than hour digits
            int amPmFontSize = (int) (fontSize * .75);
            if (amPmFontSize < 1) {
                amPmFontSize = 1;
            }
            Font amPmSizedFont = new Font("Serif", Font.PLAIN, amPmFontSize);
            g.setFont(amPmSizedFont);
            FontMetrics amPmFontM = g.getFontMetrics(amPmSizedFont);

            g.drawString(amPm, -amPmFontM.stringWidth(amPm) / 2, faceSize / 5);
        }

        // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
        int dotX(double radius, double angle) {
            int xDist;
            xDist = (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
            return xDist;
        }

        // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
        int dotY(double radius, double angle) {
            int yDist;
            yDist = (int) Math.round(radius * Math.sin(Math.toRadians(angle)));
            return yDist;
        }

        // Method called on resizing event - sets various sizing variables
        // based on the size of the resized panel and scales the logo/hands
        public void scaleFace() {
            int panelHeight = this.getSize().height;
            int panelWidth = this.getSize().width;
            size = Math.min(panelHeight, panelWidth);
            faceSize = (int) (size * .97);
            if (faceSize == 0) {
                faceSize = 1;
            }

            // Had trouble getting the proper sizes when using Images by themselves so
            // use the NamedIcon as a source for the sizes
            int logoScaleWidth = faceSize / 6;
            int logoScaleHeight = (int) ((float) logoScaleWidth * (float) jmriIcon.getIconHeight() / jmriIcon.getIconWidth());
            scaledLogo = logo.getScaledInstance(logoScaleWidth, logoScaleHeight, Image.SCALE_SMOOTH);
            scaledIcon.setImage(scaledLogo);
            logoWidth = scaledIcon.getIconWidth();
            logoHeight = scaledIcon.getIconHeight();

            scaleRatio = faceSize / 2.7 / minuteHeight;
            for (int i = 0; i < minuteX.length; i++) {
                scaledMinuteX[i] = (int) (minuteX[i] * scaleRatio);
                scaledMinuteY[i] = (int) (minuteY[i] * scaleRatio);
                scaledHourX[i] = (int) (hourX[i] * scaleRatio);
                scaledHourY[i] = (int) (hourY[i] * scaleRatio);
            }
            scaledHourHand = new Polygon(scaledHourX, scaledHourY, scaledHourX.length);
            scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY, scaledMinuteX.length);

            centreX = panelWidth / 2;
            centreY = panelHeight / 2;

            return;
        }
    }

    @SuppressWarnings("deprecation")
    void update() {
        Date now = clock.getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();
        minuteAngle = minutes * 6.;
        hourAngle = hours * 30. + 30. * minuteAngle / 360.;
        if (hours < 12) {
            amPm = "AM ";
        } else {
            amPm = "PM ";
        }
        if (hours == 12 && minutes == 0) {
            amPm = "Noon";
        }
        if (hours == 0 && minutes == 0) {
            amPm = "Midnight";
        }
        amPm = amPm + " " + (int) clock.userGetRate() + ":1";
        repaint();
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    /**
     * Handle a change to clock properties
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        boolean now = clock.getRun();
        if (now) {
            b.setText(Bundle.getMessage("ButtonPauseClock"));
        } else {
            b.setText(Bundle.getMessage("ButtonRunClock"));
        }
    }

    JButton b;

    private class ButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent a) {
            boolean next = !clock.getRun();
            clock.setRun(next);
            if (next) {
                b.setText(Bundle.getMessage("ButtonPauseClock"));
            } else {
                b.setText(Bundle.getMessage("ButtonRunClock"));
            }
        }
    }

}
