// AnalogClockFrame.java

package jmri.jmrit.analogclock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import jmri.*;
import java.awt.geom.AffineTransform;

import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.catalog.*;

/**
 * <p>Creates a JFrame containing an analog clockface and hands</p>
 *
 * <p> Time code copied from code for the Nixie clock by Bob Jacobsen</p>
 * @author                     Dennis Miller Copyright (C) 2004
 * @version                    $Revision: 1.1 $
 */

public class AnalogClockFrame extends javax.swing.JFrame implements java.beans.PropertyChangeListener {

      // GUI member declarations

      Timebase clock;
      javax.swing.Timer timer;
      static int delay = 2*1000;  // update display every two seconds
      static double minuteAngle;
      static double hourAngle;



public AnalogClockFrame() {


        clock = InstanceManager.timebaseInstance();

        // set time to now
        clock.setTime(new Date());
        try { clock.setRate(4.); } catch (Exception e) {}

        // listen for changes to the timebase parameters
        clock.addPropertyChangeListener(this);

        // init GUI
        setSize(200,200);
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        JPanel analogClockPanel = new clockPanel();
        analogClockPanel.setOpaque(true);
        getContentPane().add(analogClockPanel);

        JPanel buttonPanel = new JPanel();
	// Need to put a Box Layout on the panel to ensure the run/stop button is centered
	// Without it, the button does not center properly
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(b = new JButton("Pause"));
        b.addActionListener( new ButtonListener());
        b.setOpaque(true);
        b.setVisible(true);
        getContentPane().add(buttonPanel);
        update();

        // request callback to update time
        clock.addMinuteChangeListener( new java.beans.PropertyChangeListener() {
                    public void propertyChange(java.beans.PropertyChangeEvent e) {
                       update();
                    }
                  });


    }

public class clockPanel extends JPanel {
      // Create a Panel that has clockface drawn on it scaled to the size of the panel
      // Define common variables
      Image logo;
      Image scaledLogo;
      NamedIcon jmriIcon;
      NamedIcon scaledIcon;
      Image minute;
      Image scaledMinute;
      NamedIcon minuteIcon;
      NamedIcon scaledMinuteIcon;
      Image hour;
      Image scaledHour;
      NamedIcon hourIcon;
      NamedIcon scaledHourIcon;
      int faceSize;
      int panelWidth;
      int panelHeight;
      int size;
      int logoWidth;
      int logoHeight;
      int hourWidth;
      int hourHeight;
      int hourCentreX;
      int hourCentreY;
      int minuteWidth;
      int minuteHeight;
      int minuteCentreX;
      int minuteCentreY;
      // centreX, centreY are the coordinates of the centre of the clock
      int centreX;
      int centreY;


public clockPanel() {
	// Load the JMRI logo and hands to put on the clock
	// Icons are the original size version kept for to allow for mulitple resizing
	// and scaled Icons are the version scaled for the panel size
	jmriIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        scaledIcon = new NamedIcon("resources/logo.gif", "resources/logo.gif");
        logo = jmriIcon.getImage();

	hourIcon = new NamedIcon("resources/icons/misc/Analog/Hour.png", "resources/icons/misc/Analog/Hour.png");
	scaledHourIcon = new NamedIcon("resources/icons/misc/Analog/Hour.png", "resources/icons/misc/Analog/Hour.png");
	hour = hourIcon.getImage();

        minuteIcon = new NamedIcon("resources/icons/misc/Analog/Minute.png", "resources/icons/misc/Analog/Minute.png");
	scaledMinuteIcon = new NamedIcon("resources/icons/misc/Analog/Minute.png", "resources/icons/misc/Analog/Minute.png");
	minute = minuteIcon.getImage();


        // Add component listener to handle frame resizing event
        this.addComponentListener(new ComponentAdapter() {
          public void componentResized(ComponentEvent e) {
            scaleFace();
            }});

	}

public void paint(Graphics g){

     // overridden Paint method to draw the clock
     AffineTransform at = new AffineTransform();
     Graphics2D g2 = (Graphics2D) g;
     AffineTransform saveXform = g2.getTransform();

     // Draw the clockface outline scaled to the panel size with a dot in the middle
     g2.setColor(Color.white);
     g2.fillOval(centreX-faceSize/2, centreY-faceSize/2, faceSize, faceSize);
     g2.setColor(Color.black);
     g2.drawOval(centreX-faceSize/2, centreY-faceSize/2, faceSize, faceSize);
     int dotSize = faceSize/40;
     g2.fillOval(centreX-dotSize/2, centreY-dotSize/2, dotSize, dotSize);

     // Draw the JMRI logo
     g2.drawImage(scaledLogo, centreX-logoWidth/2, centreY-faceSize/4, logoWidth, logoHeight, this);

     // Draw the hour and minute markers
     int dashSize = size/60;
     for (int i = 0; i < 360; i = i + 6) {
       g2.drawLine(centreX + dotX(faceSize/2, i), centreY + dotY(faceSize/2, i), centreX + dotX(faceSize/2 - dashSize, i), centreY + dotY(faceSize/2 - dashSize, i));
     }
     for (int i = 0; i < 360; i = i + 30) {
       g2.drawLine(centreX + dotX(faceSize/2, i), centreY + dotY(faceSize/2, i), centreX + dotX(faceSize/2 - 3 * dashSize, i), centreY + dotY(faceSize/2 - 3 * dashSize, i));
     }

     // Add the hour digits, with the fontsize scaled to the clock size
     int fontSize = faceSize/10;
     if (fontSize < 1) {fontSize=1;}
     Font sizedFont = new Font("Serif", Font.PLAIN, fontSize);
     g2.setFont(sizedFont);
     FontMetrics fontM = g2.getFontMetrics(sizedFont);

     for (int i = 0; i < 12; i++) {
       String hour = Integer.toString(i+1);
       int xOffset = fontM.stringWidth(hour);
       int yOffset = fontM.getHeight();
       g2.drawString(Integer.toString(i+1), centreX + dotX(faceSize/2-6*dashSize,i*30-60) - xOffset/2, centreY + dotY(faceSize/2-6*dashSize,i*30-60) + yOffset/4);
     }

     // Draw hour hand rotated to appropriate angle
     // "centre" point of hour hand is at 41, 256 in unscaled image
     // Use a translated rotated affine transform to rotate about the centre point of the
     // clock rather than the anchor point of the gifs
     at.setToRotation(Math.toRadians(hourAngle), (double) centreX, (double) centreY);
     g2.setTransform(at);
     g2.drawImage(scaledHour, centreX-hourWidth/2, centreY-hourCentreY, hourWidth, hourHeight, this);


     // Draw minute hand rotated to approriate angle
     // "centre" point of minute hand is at 41, 351 in unscaled image
     at.setToRotation(Math.toRadians(minuteAngle), (double) centreX, (double) centreY);
     g2.setTransform(at);
     g2.drawImage(scaledMinute, centreX-minuteWidth/2, centreY-minuteCentreY, minuteWidth, minuteHeight, this);

     // restore original transfrom
     g2.setTransform(saveXform);
   }

   // Method to provide the cartesian x coordinate given a radius and angle (in degrees)
   int dotX (double radius, double angle) {
     int xDist;
     xDist = (int) Math.round(radius * Math.cos(Math.toRadians(angle)));
     return xDist;
   }

   // Method to provide the cartesian y coordinate given a radius and angle (in degrees)
   int dotY (double radius, double angle) {
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
     if (faceSize == 0){faceSize=1;}

     // Had trouble getting the proper sizes when using Images by themselves so
     // use the NamedIcon as a source for the sizes
     int logoScaleWidth = faceSize/6;
     int logoScaleHeight = (int) ((float)logoScaleWidth * (float)jmriIcon.getIconHeight()/(float)jmriIcon.getIconWidth());
     scaledLogo = logo.getScaledInstance(logoScaleWidth, logoScaleHeight, Image.SCALE_SMOOTH);
     scaledIcon.setImage(scaledLogo);
     logoWidth = scaledIcon.getIconWidth();
     logoHeight = scaledIcon.getIconHeight();

     int minuteScaleHeight = (int) ((float)faceSize/2.2);
     int minuteScaleWidth = (int) ((float)minuteScaleHeight * (float)minuteIcon.getIconWidth()/(float)minuteIcon.getIconHeight());
     scaledMinute = minute.getScaledInstance(minuteScaleWidth, minuteScaleHeight, Image.SCALE_SMOOTH);
     scaledMinuteIcon.setImage(scaledMinute);
     minuteWidth = scaledMinuteIcon.getIconWidth();
     minuteHeight = scaledMinuteIcon.getIconHeight();
     // pivot point of minute hand is at 41, 351 in unscaled image size of 83, 392
     minuteCentreX = (int) (41./83.*(float)minuteWidth);
     minuteCentreY = (int) (351./392.*(float)minuteHeight);

     int hourScaleHeight = minuteScaleHeight*hourIcon.getIconHeight()/minuteIcon.getIconHeight();
     int hourScaleWidth = (int) ((float)hourScaleHeight * (float)hourIcon.getIconWidth()/(float)hourIcon.getIconHeight());
     scaledHour = hour.getScaledInstance(hourScaleWidth, hourScaleHeight, Image.SCALE_SMOOTH);
     scaledHourIcon.setImage(scaledHour);
     hourWidth = scaledHourIcon.getIconWidth();
     hourHeight = scaledHourIcon.getIconHeight();
     // pivot point of hour hand is at 41, 256 in unscaled image size of 83, 296
     hourCentreX = (int)(41./83.*(float)hourWidth);
     hourCentreY = (int)(256./296.*(float)hourHeight);

     centreX = panelWidth/2;
     centreY = panelHeight/2;

     return ;
   }
}

   void update() {
       Date now = clock.getTime();
       int hours = now.getHours();
       int minutes = now.getMinutes();
       minuteAngle = (double) minutes*6.;
       hourAngle = (double) hours*30. + 30.*minuteAngle/360.;
       repaint();
   }


   // Close the window when the close box is clicked
   void thisWindowClosing(java.awt.event.WindowEvent e) {
       setVisible(false);
       timer.stop();
       dispose();
   }

   /**
    * Handle a change to clock properties
    */
   public void propertyChange(java.beans.PropertyChangeEvent e) {
               boolean now = clock.getRun();
               if (now) b.setText("Pause");
               else b.setText("Run");
   }

   JButton b;

       private class ButtonListener implements ActionListener {
               public void actionPerformed(ActionEvent a) {
                       boolean next = !clock.getRun();
                       clock.setRun(next);
                       if (next) b.setText("Pause");
                       else b.setText("Run ");
               }
       }

}