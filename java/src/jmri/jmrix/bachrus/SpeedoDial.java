package jmri.jmrix.bachrus;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.JPanel;
import jmri.jmrit.catalog.NamedIcon;

/**
 * Creates a JPanel containing an Dial type speedo display.
 *
 * <p>
 * Based on analogue clock frame by Dennis Miller
 *
 * @author Andrew Crosland Copyright (C) 2010
 * @author Dennis Miller Copyright (C) 2015
 *
 */
public class SpeedoDial extends JPanel {

    // GUI member declarations
    float speedAngle = 0.0F;
    int speedDigits = 0;

    // Create a Panel that has a dial drawn on it scaled to the size of the panel
    // Define common variables
    Image logo;
    Image scaledLogo;
    NamedIcon jmriIcon;
    NamedIcon scaledIcon;
    int minuteX[] = {-12, -11, -24, -11, -11, 0, 11, 11, 24, 11, 12};
    int minuteY[] = {-31, -261, -266, -314, -381, -391, -381, -314, -266, -261, -31};
    int scaledMinuteX[] = new int[minuteX.length];
    int scaledMinuteY[] = new int[minuteY.length];
    int rotatedMinuteX[] = new int[minuteX.length];
    int rotatedMinuteY[] = new int[minuteY.length];

    Polygon minuteHand;
    Polygon scaledMinuteHand;
    int minuteHeight;
    float scaleRatio;
    int faceSize;
    int size;
    int logoWidth;
    int logoHeight;

    // centreX, centreY are the coordinates of the centre of the dial
    int centreX;
    int centreY;

    int units = Speed.MPH;

    int baseMphLimit = 80;
    int baseKphLimit = 140;
    int mphLimit = baseMphLimit;
    int mphInc = 40;
    int kphLimit = baseKphLimit;
    int kphInc = 70;
    float priMajorTick;
    float priMinorTick;
    float secTick;
    String priString = "MPH";
    String secString = "KPH";

    public SpeedoDial() {
        super();

        // Load the JMRI logo and pointer for the dial
        // Icons are the original size version kept for to allow for mulitple resizing
        // and scaled Icons are the version scaled for the panel size
        jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        scaledIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        logo = jmriIcon.getImage();

        // Create an unscaled pointer to get the original size (height)to use
        // in the scaling calculations
        minuteHand = new Polygon(minuteX, minuteY, 11);
        minuteHeight = minuteHand.getBounds().getSize().height;

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
        super.paint(g);
        if (!(g instanceof Graphics2D) ) {
              throw new IllegalArgumentException("Graphics object passed is not the correct type");
        }

        Graphics2D g2 = (Graphics2D) g;

        // overridden Paint method to draw the speedo dial
        g2.translate(centreX, centreY);

        // Draw the dial outline scaled to the panel size with a dot in the middle and
        // center ring for the pointer
        g2.setColor(Color.white);
        g2.fillOval(-faceSize / 2, -faceSize / 2, faceSize, faceSize);
        g2.setColor(Color.black);
        g2.drawOval(-faceSize / 2, -faceSize / 2, faceSize, faceSize);

        int dotSize = faceSize / 40;
        g2.fillOval(-dotSize * 2, -dotSize * 2, 4 * dotSize, 4 * dotSize);

        // Draw the JMRI logo
        g2.drawImage(scaledLogo, -logoWidth / 2, -faceSize / 4, logoWidth, logoHeight, this);

        // Currently selected units are plotted every 10 units with major and minor
        // tick marks around the outer edge of the dial
        // Other units are plotted in a differrent color, smaller font with dots
        // in an inner ring
        // Scaled font size for primary units
        int fontSize = faceSize / 10;
        if (fontSize < 1) {
            fontSize = 1;
        }
        Font sizedFont = new Font("Serif", Font.PLAIN, fontSize);
        g2.setFont(sizedFont);
        FontMetrics fontM = g2.getFontMetrics(sizedFont);

        // Draw the speed markers for the primary units
        int dashSize = size / 60;
        setTicks();
        // i is degrees clockwise from the X axis
        // Add minor tick marks
        for (float i = 150; i < 391; i = i + priMinorTick) {
            g2.drawLine(dotX((float) faceSize / 2, i), dotY((float) faceSize / 2, i),
                    dotX((float) faceSize / 2 - dashSize, i), dotY((float) faceSize / 2 - dashSize, i));
        }
        // Add major tick marks and digits
        int j = 0;
        for (float i = 150; i < 391; i = i + priMajorTick) {
            g2.drawLine(dotX((float) faceSize / 2, i), dotY((float) faceSize / 2, i),
                    dotX((float) faceSize / 2 - 3 * dashSize, i), dotY((float) faceSize / 2 - 3 * dashSize, i));
            String speed = Integer.toString(10 * j);
            int xOffset = fontM.stringWidth(speed);
            int yOffset = fontM.getHeight();
            // offset by 210 degrees to start in lower left quadrant and work clockwise
            g2.drawString(speed, dotX((float) faceSize / 2 - 6 * dashSize, j * priMajorTick - 210) - xOffset / 2,
                    dotY((float) faceSize / 2 - 6 * dashSize, j * priMajorTick - 210) + yOffset / 4);
            j++;
        }

        // Add dots and digits for secondary units
        // First make a smaller font
        fontSize = faceSize / 15;
        if (fontSize < 1) {
            fontSize = 1;
        }
        sizedFont = new Font("Serif", Font.PLAIN, fontSize);
        g2.setFont(sizedFont);
        fontM = g2.getFontMetrics(sizedFont);
        g2.setColor(Color.green);
        j = 0;
        for (float i = 150; i < 391; i = i + secTick) {
            g2.fillOval(dotX((float) faceSize / 2 - 10 * dashSize, i), dotY((float) faceSize / 2 - 10 * dashSize, i),
                    5, 5);
            if (((j & 1) == 0) || (units == Speed.KPH)) {
                // kph are plotted every 20 when secondary, mph every 10
                String speed = Integer.toString(10 * j);
                int xOffset = fontM.stringWidth(speed);
                int yOffset = fontM.getHeight();
                // offset by 210 degrees to start in lower left quadrant and work clockwise
                g2.drawString(speed, dotX((float) faceSize / 2 - 13 * dashSize, j * secTick - 210) - xOffset / 2,
                        dotY((float) faceSize / 2 - 13 * dashSize, j * secTick - 210) + yOffset / 4);
            }
            j++;
        }
        // Draw secondary units string
        g2.drawString(secString, dotX((float) faceSize / 2 - 5 * dashSize, 45) - fontM.stringWidth(secString) / 2,
                dotY((float) faceSize / 2 - 5 * dashSize, 45) + fontM.getHeight() / 4);
        g2.setColor(Color.black);

        // Draw pointer rotated to appropriate angle
        // Calculation mimics the AffineTransform class calculations in Graphics2D
        // Graphics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
        double speedAngleRadians = Math.toRadians(speedAngle);
        for (int i = 0; i < scaledMinuteX.length; i++) {
            rotatedMinuteX[i] = (int) (scaledMinuteX[i] * Math.cos(speedAngleRadians)
                    - scaledMinuteY[i] * Math.sin(speedAngleRadians));
            rotatedMinuteY[i] = (int) (scaledMinuteX[i] * Math.sin(speedAngleRadians)
                    + scaledMinuteY[i] * Math.cos(speedAngleRadians));
        }
        scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY, rotatedMinuteX.length);
        g2.fillPolygon(scaledMinuteHand);

        // Draw primary units indicator in slightly smaller font than speed digits
        int unitsFontSize = (int) ((float) faceSize / 10 * .75);
        if (unitsFontSize < 1) {
            unitsFontSize = 1;
        }
        Font unitsSizedFont = new Font("Serif", Font.PLAIN, unitsFontSize);
        g2.setFont(unitsSizedFont);
        FontMetrics unitsFontM = g2.getFontMetrics(unitsSizedFont);
//        g2.drawString(unitsString, -amPmFontM.stringWidth(unitsString)/2, faceSize/5 );
        g2.drawString(priString, dotX((float) faceSize / 2 - 5 * dashSize, -225) - unitsFontM.stringWidth(priString) / 2,
                dotY((float) faceSize / 2 - 5 * dashSize, -225) + unitsFontM.getHeight() / 4);

        // Show numeric speed
        String speedString = Integer.toString(speedDigits);
        int digitsFontSize = (int) (fontSize * 1.5);
        Font digitsSizedFont = new Font("Serif", Font.PLAIN, digitsFontSize);
        g2.setFont(digitsSizedFont);
        FontMetrics digitsFontM = g2.getFontMetrics(digitsSizedFont);

        // draw a box around the digital speed
        int pad = (int) (digitsFontSize * 0.2);
        int h = (int) (digitsFontM.getAscent() * 0.8);
        int w = digitsFontM.stringWidth("999");
        if (pad < 2) {
            pad = 2;
        }
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(-w / 2 - pad, 2 * faceSize / 5 - h - pad, w + pad * 2, h + pad * 2);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(-w / 2 - pad, 2 * faceSize / 5 - h - pad, w + pad * 2, h + pad * 2);

        g2.setColor(Color.BLACK);
        g2.drawString(speedString, -digitsFontM.stringWidth(speedString) / 2, 2 * faceSize / 5);
    }

    // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
    int dotX(float radius, float angle) {
        int xDist;
        xDist = (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
        return xDist;
    }

    // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
    int dotY(float radius, float angle) {
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

        scaleRatio = faceSize / 2.7F / minuteHeight;
        for (int i = 0; i < minuteX.length; i++) {
            scaledMinuteX[i] = (int) (minuteX[i] * scaleRatio);
            scaledMinuteY[i] = (int) (minuteY[i] * scaleRatio);
        }
        scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY, scaledMinuteX.length);

        centreX = panelWidth / 2;
        centreY = panelHeight / 2;

        return;
    }

    void update(float speed) {
        // hand rotation starts at 12 o'clock position so offset it by 120 degrees
        // scale by the angle between major tick marks divided by 10
        if (units == Speed.MPH) {
            if (Speed.kphToMph(speed) > mphLimit) {
                mphLimit += mphInc;
                kphLimit += kphInc;
            }
            setTicks();
            speedDigits = Math.round(Speed.kphToMph(speed));
            speedAngle = -120 + Speed.kphToMph(speed * priMajorTick / 10);
        } else {
            if (speed > kphLimit) {
                mphLimit += mphInc;
                kphLimit += kphInc;
            }
            setTicks();
            speedDigits = Math.round(speed);
            speedAngle = -120 + speed * priMajorTick / 10;
        }
        repaint();
    }

    void setTicks() {
        if (units == Speed.MPH) {
            priMajorTick = 240 / ((float) mphLimit / 10);
            priMinorTick = priMajorTick / 5;
            secTick = 240 / (Speed.mphToKph(mphLimit) / 10);
        } else {
            priMajorTick = 240 / ((float) kphLimit / 10);
            priMinorTick = priMajorTick / 5;
            secTick = 240 / (Speed.kphToMph(kphLimit) / 10);
        }
    }

    void setUnitsMph() {
        units = Speed.MPH;
        priString = "MPH";
        secString = "KPH";
    }

    void setUnitsKph() {
        units = Speed.KPH;
        priString = "KPH";
        secString = "MPH";
    }

    public void reset() {
        mphLimit = baseMphLimit;
        kphLimit = baseKphLimit;
        update(0.0f);
    }
}
