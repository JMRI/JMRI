// AnalogClock2Display.java

package jmri.jmrit.display;

import java.util.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.catalog.*;
import jmri.jmrit.display.*;
import jmri.util.JmriJFrame;

/**
 * <p>An Analog Clock for displaying in a panel</p>
 *
 * <p>Time code copied in part from code for the Nixie clock by Bob Jacobsen </p>
 *
 * @author  Howard G. Penny - Copyright (C) 2005
 * @version $Revision: 1.1 $
 */
public class AnalogClock2Display extends PositionableJComponent {
    Timebase clock;
    double rate;
    static double minuteAngle;
    static double hourAngle;
    static String amPm;

    // Define common variables
    Image logo;
    Image scaledLogo;
    Image clockFace;
    NamedIcon jmriIcon;
    NamedIcon scaledIcon;
    NamedIcon clockIcon;

    int hourX[] = {
         -12, -11, -25, -10, -10, 0, 10, 10, 25, 11, 12};
    int hourY[] = {
         -31, -163, -170, -211, -276, -285, -276, -211, -170, -163, -31};
    int minuteX[] = {
         -12, -11, -24, -11, -11, 0, 11, 11, 24, 11, 12};
    int minuteY[] = {
         -31, -261, -266, -314, -381, -391, -381, -314, -266, -261, -31};
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

    public AnalogClock2Display(JmriJFrame parentFrame) {
        super((JmriJFrame)parentFrame);
        clock = InstanceManager.timebaseInstance();

        // set time to now
        clock.setTime(new Date());
        rate = (int) clock.getRate();

        // Load the JMRI logo and clock face
        // Icons are the original size version kept for to allow for mulitple resizing
        // and scaled Icons are the version scaled for the panel size
        jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        scaledIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        clockIcon = new NamedIcon("resources/clock2.gif", "resources/clock2.gif");
        logo = jmriIcon.getImage();
        clockFace = clockIcon.getImage();

        // Create an unscaled set of hands to get the original size (height)to use
        // in the scaling calculations
        hourHand = new Polygon(hourX, hourY, 11);
        hourHeight = hourHand.getBounds().getSize().height;
        minuteHand = new Polygon(minuteX, minuteY, 11);
        minuteHeight = minuteHand.getBounds().getSize().height;

        amPm = "AM";

        // request callback to update time
        clock.addMinuteChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });
        // request callback to update changes in properties
        clock.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });
        setSize(clockIcon.getIconHeight()); // set to default size
    }

    ButtonGroup rateButtonGroup = null;
    JMenuItem runMenu = null;

    protected void addToPopup() {
        if (popup != null) {
            popup.insert(new JMenuItem("Fast Clock"), 0);
            JMenu rateMenu = new JMenu("Clock rate");
            rateButtonGroup = new ButtonGroup();
            addRateMenuEntry(rateMenu, 1);
            addRateMenuEntry(rateMenu, 2);
            addRateMenuEntry(rateMenu, 4);
            addRateMenuEntry(rateMenu, 8);
            popup.insert(rateMenu, 1);
            runMenu = new JMenuItem(getRun() ? "Stop" : "Start");
            runMenu.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    setRun(!getRun());
                    update();
                }
            });
            popup.insert(runMenu, 2);
        }
    }

    void addRateMenuEntry(JMenu menu, final int newrate) {
        JRadioButtonMenuItem r = new JRadioButtonMenuItem("" + newrate + ":1");
        r.addActionListener(new ActionListener() {
            final int rate = newrate;
            public void actionPerformed(ActionEvent e) {
                try {
                    clock.setRate(rate);
                }
                catch (Exception t) {}
            }
        });
        rateButtonGroup.add(r);
        if (rate == newrate) {
            r.setSelected(true);
        }
        else {
            r.setSelected(false);
        }
        menu.add(r);
    }

    public void paint(Graphics g) {

        // overridden Paint method to draw the clock
        g.translate(centreX, centreY);

        // Draw the clock face
        g.drawImage(clockFace, -faceSize / 2, -faceSize / 2, faceSize, faceSize, this);

        // Draw the JMRI logo
        g.drawImage(scaledLogo, -logoWidth / 2, -faceSize / 4, logoWidth,
                    logoHeight, this);

        // Draw hour hand rotated to appropriate angle
        // Calculation mimics the AffineTransform class calculations in Graphics2D
        // Grpahics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
        for (int i = 0; i < scaledMinuteX.length; i++) {
            rotatedMinuteX[i] = (int) ( (double) scaledMinuteX[i] *
                                       Math.cos(toRadians(minuteAngle)) -
                                       (double) scaledMinuteY[i] *
                                       Math.sin(toRadians(minuteAngle)));
            rotatedMinuteY[i] = (int) ( (double) scaledMinuteX[i] *
                                       Math.sin(toRadians(minuteAngle)) +
                                       (double) scaledMinuteY[i] *
                                       Math.cos(toRadians(minuteAngle)));
        }
        scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY,
                                       rotatedMinuteX.length);
        for (int i = 0; i < scaledHourX.length; i++) {
            rotatedHourX[i] = (int) ( (double) scaledHourX[i] *
                                     Math.cos(toRadians(hourAngle)) -
                                     (double) scaledHourY[i] *
                                     Math.sin(toRadians(hourAngle)));
            rotatedHourY[i] = (int) ( (double) scaledHourX[i] *
                                     Math.sin(toRadians(hourAngle)) +
                                     (double) scaledHourY[i] *
                                     Math.cos(toRadians(hourAngle)));
        }
        scaledHourHand = new Polygon(rotatedHourX, rotatedHourY,
                                     rotatedHourX.length);

        g.fillPolygon(scaledHourHand);
        g.fillPolygon(scaledMinuteHand);

        // Draw AM/PM indicator in slightly smaller font than hour digits
        int amPmFontSize = (int) ( (double) faceSize * .075);
        if (amPmFontSize < 1) {
            amPmFontSize = 1;
        }
        Font amPmSizedFont = new Font("Serif", Font.BOLD, amPmFontSize);
        g.setFont(amPmSizedFont);
        FontMetrics amPmFontM = g.getFontMetrics(amPmSizedFont);

        g.drawString(amPm, -amPmFontM.stringWidth(amPm) / 2, faceSize / 5);
    }

    // Method to convert degrees to radians
    // Math.toRadians was not available until Java 1.2
    double toRadians(double degrees) {
        return degrees / 180.0 * Math.PI;
    }

    // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
    int dotX(double radius, double angle) {
        int xDist;
        xDist = (int) Math.round(radius * Math.cos(toRadians(angle)));
        return xDist;
    }

    // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
    int dotY(double radius, double angle) {
        int yDist;
        yDist = (int) Math.round(radius * Math.sin(toRadians(angle)));
        return yDist;
    }

    // Method called on resizing event - sets various sizing variables
    // based on the size of the resized panel and scales the logo/hands
    private void scaleFace() {
        panelHeight = this.getSize().height;
        panelWidth = this.getSize().width;
        if (panelHeight > 0 && panelWidth > 0) {
            size = Math.min(panelHeight, panelWidth);
        }
        faceSize = size;
        if (faceSize == 0) {
            faceSize = 1;
        }

        // Had trouble getting the proper sizes when using Images by themselves so
        // use the NamedIcon as a source for the sizes
        int logoScaleWidth = faceSize / 6;
        int logoScaleHeight = (int) ( (float) logoScaleWidth *
                                     (float) jmriIcon.getIconHeight() /
                                     (float) jmriIcon.getIconWidth());
        scaledLogo = logo.getScaledInstance(logoScaleWidth, logoScaleHeight,
                                            Image.SCALE_SMOOTH);
        scaledIcon.setImage(scaledLogo);
        logoWidth = scaledIcon.getIconWidth();
        logoHeight = scaledIcon.getIconHeight();

        scaleRatio = (double) faceSize / 2.7 / (double) minuteHeight;
        for (int i = 0; i < minuteX.length; i++) {
            scaledMinuteX[i] = (int) ( (double) minuteX[i] * scaleRatio);
            scaledMinuteY[i] = (int) ( (double) minuteY[i] * scaleRatio);
            scaledHourX[i] = (int) ( (double) hourX[i] * scaleRatio);
            scaledHourY[i] = (int) ( (double) hourY[i] * scaleRatio);
        }
        scaledHourHand = new Polygon(scaledHourX, scaledHourY,
                                     scaledHourX.length);
        scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY,
                                       scaledMinuteX.length);

        if (panelHeight > 0 && panelWidth > 0) {
            centreX = panelWidth / 2;
            centreY = panelHeight / 2;
        }
        else {
            centreX = centreY = size / 2;
        }
        return;
    }

    public void setSize(int x) {
        size = x;
        setSize(x, x);
        scaleFace();
    }

/* This needs to be updated if resizing becomes an option
    public void resize() {
        int panelHeight = this.getSize().height;
        int panelWidth = this.getSize().width;
        size = Math.min(panelHeight, panelWidth);
        scaleFace();
    }
*/

    public void update() {
        Date now = clock.getTime();
        if (runMenu != null) {
            runMenu.setText(getRun() ? "Stop" : "Start");
        }
        int hours = now.getHours();
        int minutes = now.getMinutes();
        minuteAngle = (double) minutes * 6.;
        hourAngle = (double) hours * 30. + 30. * minuteAngle / 360.;
        if (hours < 12) {
            amPm = "AM " + (int) clock.getRate() + ":1";
        }
        else {
            amPm = "PM " + (int) clock.getRate() + ":1";
        }
        if (hours == 12 && minutes == 0) {
            amPm = "Noon";
        }
        if (hours == 0 && minutes == 0) {
            amPm = "Midnight";
        }
        repaint();
    }

    private Integer displayLevel;
    public void setDisplayLevel(Integer l) {
        displayLevel = l;
    }

    public void setDisplayLevel(int l) {
        setDisplayLevel(new Integer(l));
    }

    public Integer getDisplayLevel() {
        return displayLevel;
    }

    public boolean getRun() {
        return clock.getRun();
    }

    public void setRun(boolean next) {
        clock.setRun(next);
    }

    void cleanup() {
        ((PanelEditor)_parentFrame).clockAddEnable(true);
    }

    public void dispose() {
        rateButtonGroup = null;
        runMenu = null;
        super.dispose();
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.
        getInstance(AnalogClock2Display.class.getName());
}
