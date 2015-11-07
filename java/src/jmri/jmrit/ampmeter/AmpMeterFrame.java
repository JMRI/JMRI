// AmpMeterFrame.java
package jmri.jmrit.ampmeter;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.Date;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;
import jmri.MultiMeter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Frame providing a simple lcd-based display of track current.
 * <P>
 * A Run/Stop button is built into this, but because I don't like the way it
 * looks, it's not currently displayed in the GUI.
 *
 *
 * @author	Ken Cameron Copyright (C) 2007
 * @author	Mark Underwood Copyright (C) 2007
 * @version	$Revision$
 *
 * This was a direct steal form the LCDClock code by Ken Cameron,
 * which was a direct steal from the Nixie clock code, ver 1.2. 
 * Thank you Bob Jacobsen and Ken Cameron.
 *
 */
public class AmpMeterFrame extends JmriJFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JLabel h1;  // msb of hours
    JLabel h2;
    JLabel m1;  // msb of minutes
    JLabel m2;
    JLabel colon;

    double aspect;
    double iconAspect;

    Timebase clock;

    MultiMeter meter;

    NamedIcon tubes[] = new NamedIcon[10];
    NamedIcon baseTubes[] = new NamedIcon[10];
    NamedIcon colonIcon;
    NamedIcon baseColon;
    //"base" variables used to hold original gifs, other variables used with scaled images

    public AmpMeterFrame() {
        super(Bundle.getMessage("MenuItemAmpMeter"));

	// TODO: Replace this with a data source that polls the command station.
        //clock = InstanceManager.timebaseInstance();
	meter = InstanceManager.getDefault(MultiMeter.class);

        //Load the images (these are now the larger version of the original gifs
	// TODO: At least add a ".", if not use new icons.
        for (int i = 0; i < 10; i++) {
            baseTubes[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
            tubes[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        colonIcon = new NamedIcon("resources/icons/misc/LCD/Lcd_Colonb.GIF", "resources/icons/misc/LCD/Lcd_Colonb.GIF");
        baseColon = new NamedIcon("resources/icons/misc/LCD/Lcd_Colonb.GIF", "resources/icons/misc/LCD/Lcd_Colonb.GIF");
        // set initial size the same as the original gifs
        for (int i = 0; i < 10; i++) {
            Image scaledImage = baseTubes[i].getImage().getScaledInstance(23, 32, Image.SCALE_SMOOTH);
            tubes[i].setImage(scaledImage);
        }
        Image scaledImage = baseColon.getImage().getScaledInstance(12, 32, Image.SCALE_SMOOTH);
        colonIcon.setImage(scaledImage);

        // determine aspect ratio of a single digit graphic
        iconAspect = 24. / 32.;

        // determine the aspect ratio of the 4 digit base graphic plus a half digit for the colon
        // this DOES NOT allow space for the Run/Stop button, if it is
        // enabled.  When the Run/Stop button is enabled, the layout will have to be changed
        aspect = (4.5 * 24.) / 32.;

        // listen for changes to the timebase parameters
	meter.addPropertyChangeListener(this);
        //clock.addPropertyChangeListener(this);

        // init GUI
        m1 = new JLabel(tubes[0]);
        m2 = new JLabel(tubes[0]);
        h1 = new JLabel(tubes[0]);
        h2 = new JLabel(tubes[0]);
        colon = new JLabel(colonIcon);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(h1);
        getContentPane().add(colon);
        getContentPane().add(h2);
        getContentPane().add(m1);
        getContentPane().add(m2);

        getContentPane().add(b = new JButton("Stop"));
        b.addActionListener(new ButtonListener());
        // since Run/Stop button looks crummy, don't display for now
        b.setVisible(false);

        update();
        pack();

        // request callback to update time
	// Again, adding updates.
	java.beans.PropertyChangeListener du_listener = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
	    };
        meter.addDataUpdateListener(du_listener);

        // Add component listener to handle frame resizing event
        this.addComponentListener(
                new ComponentAdapter() {
                    public void componentResized(ComponentEvent e) {
                        scaleImage();
                    }
                });

    }

    // Added method to scale the clock digit images to fit the
    // size of the display window
    public void scaleImage() {
        int iconHeight;
        int iconWidth;
        int frameHeight = this.getContentPane().getSize().height;
        int frameWidth = this.getContentPane().getSize().width;
        if ((double) frameWidth / (double) frameHeight > aspect) {
            iconHeight = frameHeight;
            iconWidth = (int) (iconAspect * iconHeight);
        } else {
            //this DOES NOT allow space for the Run/Stop button, if it is
            //enabled.  When the Run/Stop button is enabled, the layout will have to be changed
            iconWidth = (int) (frameWidth / 4.5);
            iconHeight = (int) (iconWidth / iconAspect);
        }
        for (int i = 0; i < 10; i++) {
            Image scaledImage = baseTubes[i].getImage().getScaledInstance(iconWidth, iconHeight, Image.SCALE_SMOOTH);
            tubes[i].setImage(scaledImage);
        }
        Image scaledImage = baseColon.getImage().getScaledInstance(iconWidth / 2, iconHeight, Image.SCALE_SMOOTH);
        colonIcon.setImage(scaledImage);

//      Ugly hack to force frame to redo the layout.
//      Without this the image is scaled but the label size and position doesn't change.
//      doLayout() doesn't work either
        this.setVisible(false);
        this.remove(b);
        this.getContentPane().add(b);
        this.setVisible(true);
        return;
    }

    @SuppressWarnings("deprecation")
    void update() {
	float val = meter.getCurrent(); // should be a value between 0-99%


	int v1 = (int)(val*10); // first decimal digit
	int v2 = ((int)(val*100)) % 10; // second decimal digit.

	log.debug("Current update: val {} v1 {} v2 {}", val, v1, v2);

        h1.setIcon(tubes[0]);
        h2.setIcon(tubes[v1]);
        m1.setIcon(tubes[v2]);
        m2.setIcon(tubes[0]);
    }

    public void dispose() {
	meter.removePropertyChangeListener(this);
	meter.removeDataUpdateListener(this);
        super.dispose();
    }

    /**
     * Handle a change to clock properties
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
	/*
        boolean now = clock.getRun();
        if (now) {
            b.setText("Stop");
        } else {
            b.setText("Run");
        }
	*/
    }

    JButton b;

    private class ButtonListener implements ActionListener {

        public void actionPerformed(ActionEvent a) {
	    /*
            boolean next = !clock.getRun();
            clock.setRun(next);
            if (next) {
                b.setText("Stop");
            } else {
                b.setText("Run ");
            }
	    */
        }
    }

    static Logger log = LoggerFactory.getLogger(AmpMeterFrame.class.getName());


}
