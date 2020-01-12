package jmri.jmrit.display;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Date;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An Analog Clock for displaying in a panel.
 * <p>
 * Time code copied in part from code for the Nixie clock by Bob Jacobsen
 *
 * @author Howard G. Penny - Copyright (C) 2005
 */
public class AnalogClock2Display extends PositionableJComponent implements LinkingObject {

    Timebase clock;
    double rate;
    double minuteAngle;
    double hourAngle;
    String amPm;
    Color color = Color.black;

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

    String _url;

    public AnalogClock2Display(Editor editor) {
        super(editor);
        clock = InstanceManager.getDefault(jmri.Timebase.class);

        rate = (int) clock.userGetRate();

        init();
    }

    public AnalogClock2Display(Editor editor, String url) {
        this(editor);
        _url = url;
    }

    @Override
    public Positionable deepClone() {
        AnalogClock2Display pos;
        if (_url == null || _url.trim().length() == 0) {
            pos = new AnalogClock2Display(_editor);
        } else {
            pos = new AnalogClock2Display(_editor, _url);
        }
        return finishClone(pos);
    }

    protected Positionable finishClone(AnalogClock2Display pos) {
        pos.setScale(getScale());
        return super.finishClone(pos);
    }

    final void init() {
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
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });
        // request callback to update changes in properties
        clock.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        });
        setSize(clockIcon.getIconHeight()); // set to default size
    }

    ButtonGroup colorButtonGroup = null;
    ButtonGroup rateButtonGroup = null;
    JMenuItem runMenu = null;

    public int getFaceWidth() {
        return faceSize;
    }

    public int getFaceHeight() {
        return faceSize;
    }

    @Override
    public boolean setScaleMenu(JPopupMenu popup) {

        popup.add(new JMenuItem(Bundle.getMessage("FastClock")));
        JMenu rateMenu = new JMenu("Clock rate");
        rateButtonGroup = new ButtonGroup();
        addRateMenuEntry(rateMenu, 1);
        addRateMenuEntry(rateMenu, 2);
        addRateMenuEntry(rateMenu, 4);
        addRateMenuEntry(rateMenu, 8);
        popup.add(rateMenu);
        runMenu = new JMenuItem(getRun() ? "Stop" : "Start");
        runMenu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setRun(!getRun());
                update();
            }
        });
        popup.add(runMenu);
        popup.add(CoordinateEdit.getScaleEditAction(this));
        popup.addSeparator();
        JMenuItem colorMenuItem = new JMenuItem(Bundle.getMessage("Color"));
        colorMenuItem.addActionListener((ActionEvent event) -> {
            Color desiredColor = JmriColorChooser.showDialog(this,
                                 Bundle.getMessage("DefaultTextColor", ""),
                                 color);
            if (desiredColor!=null && !color.equals(desiredColor)) {
               setColor(desiredColor);
           }
        });
        popup.add(colorMenuItem);

        return true;
    }

    @Override
    public String getNameString() {
        return "Clock";
    }

    @Override
    public void setScale(double scale) {
        if (scale == 1.0) {
            init();
            return;
        }
        AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
        int w = (int) Math.ceil(scale * clockIcon.getIconWidth());
        int h = (int) Math.ceil(scale * clockIcon.getIconHeight());
        clockIcon.transformImage(w, h, t, null);
        w = (int) Math.ceil(scale * scaledIcon.getIconWidth());
        h = (int) Math.ceil(scale * scaledIcon.getIconHeight());
        scaledIcon.transformImage(w, h, t, null);
        w = (int) Math.ceil(scale * jmriIcon.getIconWidth());
        h = (int) Math.ceil(scale * jmriIcon.getIconHeight());
        jmriIcon.transformImage(w, h, t, null);
        setSize(clockIcon.getIconHeight());
        scale *= getScale();
        super.setScale(scale);
    }

    @SuppressFBWarnings(value="FE_FLOATING_POINT_EQUALITY", justification="fixed number of possible values")
    void addRateMenuEntry(JMenu menu, final int newrate) {
        JRadioButtonMenuItem button = new JRadioButtonMenuItem("" + newrate + ":1");
        button.addActionListener(new ActionListener() {
            final int r = newrate;

            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    clock.userSetRate(r);
                    rate = r;
                } catch (TimebaseRateException t) {
                    log.error("TimebaseRateException for rate= " + r + ". " + t);
                }
            }
        });
        rateButtonGroup.add(button);

        // next line is the FE_FLOATING_POINT_EQUALITY annotated above
        if (rate == newrate) {
            button.setSelected(true);
        } else {
            button.setSelected(false);
        }
        menu.add(button);
    }

    public Color getColor() {
        return this.color;
    }

    public void setColor(Color color) {
        this.color = color;
        update();
        JmriColorChooser.addRecentColor(color);
    }

    @Override
    public void paint(Graphics g) {
        // overridden Paint method to draw the clock
        g.setColor(color);
        g.translate(centreX, centreY);

        // Draw the clock face
        g.drawImage(clockFace, -faceSize / 2, -faceSize / 2, faceSize, faceSize, this);

        // Draw the JMRI logo
        g.drawImage(scaledLogo, -logoWidth / 2, -faceSize / 4, logoWidth,
                logoHeight, this);

        // Draw hour hand rotated to appropriate angle
        // Calculation mimics the AffineTransform class calculations in Graphics2D
        // Grpahics2D and AffineTransform not used to maintain compatabilty with Java 1.1.8
        double minuteAngleRadians = Math.toRadians(minuteAngle);
        for (int i = 0; i < scaledMinuteX.length; i++) {
            rotatedMinuteX[i] = (int) (scaledMinuteX[i] * Math.cos(minuteAngleRadians)
                    - scaledMinuteY[i] * Math.sin(minuteAngleRadians));
            rotatedMinuteY[i] = (int) (scaledMinuteX[i] * Math.sin(minuteAngleRadians)
                    + scaledMinuteY[i] * Math.cos(minuteAngleRadians));
        }
        scaledMinuteHand = new Polygon(rotatedMinuteX, rotatedMinuteY, rotatedMinuteX.length);
        double hourAngleRadians = Math.toRadians(hourAngle);
        for (int i = 0; i < scaledHourX.length; i++) {
            rotatedHourX[i] = (int) (scaledHourX[i] * Math.cos(hourAngleRadians)
                    - scaledHourY[i] * Math.sin(hourAngleRadians));
            rotatedHourY[i] = (int) (scaledHourX[i] * Math.sin(hourAngleRadians)
                    + scaledHourY[i] * Math.cos(hourAngleRadians));
        }
        scaledHourHand = new Polygon(rotatedHourX, rotatedHourY,
                rotatedHourX.length);

        g.fillPolygon(scaledHourHand);
        g.fillPolygon(scaledMinuteHand);

        // Draw AM/PM indicator in slightly smaller font than hour digits
        int amPmFontSize = (int) (faceSize * .075);
        if (amPmFontSize < 1) {
            amPmFontSize = 1;
        }
        Font amPmSizedFont = new Font("Serif", Font.BOLD, amPmFontSize);
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
        int logoScaleHeight = (int) ((float) logoScaleWidth
                * (float) jmriIcon.getIconHeight()
                / jmriIcon.getIconWidth());
        scaledLogo = logo.getScaledInstance(logoScaleWidth, logoScaleHeight,
                Image.SCALE_SMOOTH);
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
        scaledHourHand = new Polygon(scaledHourX, scaledHourY,
                scaledHourX.length);
        scaledMinuteHand = new Polygon(scaledMinuteX, scaledMinuteY,
                scaledMinuteX.length);

        if (panelHeight > 0 && panelWidth > 0) {
            centreX = panelWidth / 2;
            centreY = panelHeight / 2;
        } else {
            centreX = centreY = size / 2;
        }
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
    @SuppressWarnings("deprecation")
    public void update() {
        Date now = clock.getTime();
        if (runMenu != null) {
            runMenu.setText(getRun() ? "Stop" : "Start");
        }
        int hours = now.getHours();
        int minutes = now.getMinutes();
        minuteAngle = minutes * 6.;
        hourAngle = hours * 30. + 30. * minuteAngle / 360.;
        if (hours < 12) {
            amPm = "AM " + (int) clock.userGetRate() + ":1";
        } else {
            amPm = "PM " + (int) clock.userGetRate() + ":1";
        }
        if (hours == 12 && minutes == 0) {
            amPm = "Noon";
        }
        if (hours == 0 && minutes == 0) {
            amPm = "Midnight";
        }
        repaint();
    }

    public boolean getRun() {
        return clock.getRun();
    }

    public void setRun(boolean next) {
        clock.setRun(next);
    }

    @Override
    void cleanup() {
    }

    public void dispose() {
        rateButtonGroup = null;
        runMenu = null;
    }

    @Override
    public String getURL() {
        return _url;
    }

    @Override
    public void setULRL(String u) {
        _url = u;
    }

    @Override
    public boolean setLinkMenu(JPopupMenu popup) {
        if (_url == null || _url.trim().length() == 0) {
            return false;
        }
        popup.add(CoordinateEdit.getLinkEditAction(this, "EditLink"));
        return true;
    }

    @Override
    public void doMouseClicked(MouseEvent event) {
        log.debug("click to " + _url);
        if (_url == null || _url.trim().length() == 0) {
            return;
        }
        try {
            if (_url.startsWith("frame:")) {
                // locate JmriJFrame and push to front
                String frame = _url.substring(6);
                final jmri.util.JmriJFrame jframe = jmri.util.JmriJFrame.getFrame(frame);
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        jframe.toFront();
                        jframe.repaint();
                    }
                });
            } else {
                jmri.util.ExternalLinkContentViewerUI.activateURL(new java.net.URL(_url));
            }
        } catch (IOException t) {
            log.error("Error handling link", t);
        } catch (URISyntaxException t) {
            log.error("Error handling link", t);
        }
        super.doMouseClicked(event);
    }

    private static final Logger log = LoggerFactory.getLogger(AnalogClock2Display.class);
}
