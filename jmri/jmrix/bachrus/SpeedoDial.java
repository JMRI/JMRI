// SpeedoDial.java

package jmri.jmrix.bachrus;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import jmri.jmrit.catalog.*;

/**
 * Creates a JPanel containing an Dial type speedo display.
 *
 * <p> Based on analogue clock frame by Dennis Miller
 *
 * @author                     Andrew Crosland Copyright (C) 2010
 * @version                    $Revision: 1.1 $
 */
public class SpeedoDial extends JPanel {

    // GUI member declarations
    double speedAngle = 0.0;
    int speedDigits = 0;
    
    // Create a Panel that has clockface drawn on it scaled to the size of the panel
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

    enum speedUnits {MPH, KPH};
    speedUnits units = speedUnits.MPH;
        
    public SpeedoDial() {
        super();

        // Load the JMRI logo and hands to put on the clock
        // Icons are the original size version kept for to allow for mulitple resizing
        // and scaled Icons are the version scaled for the panel size
        jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        scaledIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        logo = jmriIcon.getImage();

        // Create an unscaled set of hands to get the original size (height)to use
        // in the scaling calculations
        minuteHand = new Polygon(minuteX, minuteY, 11);
        minuteHeight = minuteHand.getBounds().getSize().height;

        // Add component listener to handle frame resizing event
        this.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                scaleFace();
            }});

        setPreferredSize(new java.awt.Dimension(300,300));
    }
        
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D g2 = (Graphics2D)g;

        // overridden Paint method to draw the speedo dial

        g2.translate(centreX, centreY);

        // Draw the clockface outline scaled to the panel size with a dot in the middle and
        // rings for the hands
        g2.setColor(Color.white);
        g2.fillOval(-faceSize/2, -faceSize/2, faceSize, faceSize);
        g2.setColor(Color.black);
        g2.drawOval(-faceSize/2, -faceSize/2, faceSize, faceSize);

        int dotSize = faceSize/40;
        g2.fillOval(-dotSize*2, -dotSize*2, 4*dotSize, 4*dotSize);
        g2.setColor(Color.white);
        g2.fillOval(-dotSize, -dotSize, 2*dotSize, 2*dotSize);
        g2.setColor(Color.black);
        g2.fillOval(-dotSize/2, -dotSize/2, dotSize, dotSize);

        // Draw the JMRI logo
        g2.drawImage(scaledLogo, -logoWidth/2, -faceSize/4, logoWidth, logoHeight, this);

        // Draw the hour and minute markers
        int dashSize = size/60;
        // i is degrees clockwise from the X axis
        for (int i = 150; i < 390; i = i + 6) {
            g2.drawLine(dotX(faceSize/2, i), dotY(faceSize/2, i),
                       dotX(faceSize/2 - dashSize, i), dotY(faceSize/2 - dashSize, i));
        }
        for (int i = 150; i < 390; i = i + 30) {
            g2.drawLine(dotX(faceSize/2, i), dotY(faceSize/2, i),
                       dotX(faceSize/2 - 3 * dashSize, i), dotY(faceSize/2 - 3 * dashSize, i));
        }

        // Add the hour digits, with the fontsize scaled to the clock size
        int fontSize = faceSize/10;
        if (fontSize < 1) fontSize=1;
        Font sizedFont = new Font("Serif", Font.PLAIN, fontSize);
        g2.setFont(sizedFont);
        FontMetrics fontM = g2.getFontMetrics(sizedFont);

        for (int i = 0; i < 9; i++) {
            String hour = Integer.toString(10*i);
            int xOffset = fontM.stringWidth(hour);
            int yOffset = fontM.getHeight();
            // offset by 210 degrees to start in lower left quadrant and work clockwise
            g2.drawString(hour, dotX(faceSize/2-6*dashSize,i*30-210) - xOffset/2,
                               dotY(faceSize/2-6*dashSize,i*30-210) + yOffset/4);
        }

        // Draw minute hand rotated to appropriate angle
        // Calculation mimics the AffineTransform class calculations in Graphics2D
        // Graphics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
        for (int i = 0; i < scaledMinuteX.length; i++) {
            rotatedMinuteX[i] = (int) (scaledMinuteX[i]*Math.cos(toRadians(speedAngle))
                                    - scaledMinuteY[i]*Math.sin(toRadians(speedAngle)));
            rotatedMinuteY[i] = (int) (scaledMinuteX[i]*Math.sin(toRadians(speedAngle))
                                    + scaledMinuteY[i]*Math.cos(toRadians(speedAngle)));
        }
        scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY, rotatedMinuteX.length);
        g2.fillPolygon(scaledMinuteHand);

        // Draw units indicator in slightly smaller font than hour digits
        String unitsString = (units == speedUnits.MPH) ? "MPH" : "KPH";
        int unitsFontSize = (int) (fontSize*.75);
        if (unitsFontSize < 1) unitsFontSize = 1;
        Font unitsSizedFont = new Font("Serif", Font.PLAIN, unitsFontSize);
        g2.setFont(unitsSizedFont);
        FontMetrics amPmFontM = g2.getFontMetrics(unitsSizedFont);
        g2.drawString(unitsString, -amPmFontM.stringWidth(unitsString)/2, faceSize/5 );
        
        // Show numeric speed
        String speedString = Integer.toString(speedDigits);
        int digitsFontSize = (int) (fontSize*1.5);
        Font digitsSizedFont = new Font("Serif", Font.PLAIN, digitsFontSize);
        g2.setFont(digitsSizedFont);
        FontMetrics digitsFontM = g2.getFontMetrics(digitsSizedFont);
        
        // draw a box around the digital speed
        int pad = (int)(digitsFontSize*0.2);
        int h = (int)(digitsFontM.getAscent()*0.8);
        int w = digitsFontM.stringWidth("999");
        if (pad < 2) { pad = 2; }
        g2.setColor(Color.LIGHT_GRAY);
        g2.fillRect(-w/2-pad, 2*faceSize/5-h-pad, w+pad*2, h+pad*2);
        g2.setColor(Color.DARK_GRAY);
        g2.drawRect(-w/2-pad, 2*faceSize/5-h-pad, w+pad*2, h+pad*2);

        g2.setColor(Color.BLACK);
        g2.drawString(speedString, -digitsFontM.stringWidth(speedString)/2, 2*faceSize/5 );
    }

    // Method to convert degrees to radians
    // Math.toRadians was not available until Java 1.2
    double toRadians(double degrees) {
        return degrees/180.0*Math.PI;
    }

    // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
    int dotX (double radius, double angle) {
        int xDist;
        xDist = (int) Math.round(radius * Math.cos(toRadians(angle)));
        return xDist;
    }

    // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
    int dotY (double radius, double angle) {
        int yDist;
        yDist = (int) Math.round(radius * Math.sin(toRadians(angle)));
        return yDist;
    }

    // Method called on resizing event - sets various sizing variables
    // based on the size of the resized panel and scales the logo/hands
    public void scaleFace() {
        int panelHeight = this.getSize().height;
        int panelWidth = this.getSize().width;
        size = Math.min(panelHeight, panelWidth);
        faceSize = (int) (size * .97);
        if (faceSize == 0){faceSize=1;}

        // Had trouble getting the proper sizes when using Images by themselves so
        // use the NamedIcon as a source for the sizes
        int logoScaleWidth = faceSize/6;
        int logoScaleHeight = (int) ((float)logoScaleWidth * (float)jmriIcon.getIconHeight()/jmriIcon.getIconWidth());
        scaledLogo = logo.getScaledInstance(logoScaleWidth, logoScaleHeight, Image.SCALE_SMOOTH);
        scaledIcon.setImage(scaledLogo);
        logoWidth = scaledIcon.getIconWidth();
        logoHeight = scaledIcon.getIconHeight();

        scaleRatio=faceSize/2.7/minuteHeight;
        for (int i = 0; i < minuteX.length; i++) {
            scaledMinuteX[i] =(int) (minuteX[i]*scaleRatio);
            scaledMinuteY[i] = (int) (minuteY[i]*scaleRatio);
        }
        scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY, scaledMinuteX.length);

        centreX = panelWidth/2;
        centreY = panelHeight/2;

        return ;
    }
    
    @SuppressWarnings("deprecation")
    void update(float speed) {
        // hand rotation starts at 12 o'clock position so offset it here
        // scale by 3 so 10xph is 30 degrees
        speedDigits = Math.round(speed);
        speedAngle = speed*3-120;
        repaint();
    }

    void update() {
        repaint();
    }

    void setUnitsMph() { units = speedUnits.MPH; }
    void setUnitsKph() { units = speedUnits.KPH; }
}

