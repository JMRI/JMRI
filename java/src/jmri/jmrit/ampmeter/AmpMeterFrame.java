package jmri.jmrit.ampmeter;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.MultiMeter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Frame providing a simple lcd-based display of track current.
 * <P>
 * A Run/Stop button is built into this, but because I don't like the way it
 * looks, it's not currently displayed in the GUI.
 *
 *
 * @author Ken Cameron Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2007
 *
 * This was a direct steal form the LCDClock code by Ken Cameron, which was a
 * direct steal from the Nixie clock code, ver 1.2. Thank you Bob Jacobsen and
 * Ken Cameron.
 *
 */
public class AmpMeterFrame extends JmriJFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JLabel d1;  // Decimal 1 (msb)
    JLabel d2;  // Decimal 2
    JLabel d3;  // Decimal 3 (lsb)
    JLabel percent;
    JLabel decimal;

    double aspect;
    double iconAspect;

    private int startWidth;
    private int startHeight;

    MultiMeter meter;

    NamedIcon digits[] = new NamedIcon[10];
    NamedIcon percentIcon;
    NamedIcon decimalIcon;

    public AmpMeterFrame() {
        super(Bundle.getMessage("MenuItemAmpMeter"));

        meter = InstanceManager.getDefault(MultiMeter.class);
    }

    @Override
    public void initComponents() {
        //Load the images (these are now the larger version of the original gifs
        for (int i = 0; i < 10; i++) {
            digits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        percentIcon = new NamedIcon("resources/icons/misc/LCD/percentb.gif", "resources/icons/misc/LCD/percentb.gif");
        decimalIcon = new NamedIcon("resources/icons/misc/LCD/decimalb.gif", "resources/icons/misc/LCD/decimalb.gif");

        // determine aspect ratio of a single digit graphic
        iconAspect = 24. / 32.;

        // determine the aspect ratio of the 4 digit base graphic plus a half digit for the colon
        // this DOES NOT allow space for the Run/Stop button, if it is
        // enabled.  When the Run/Stop button is enabled, the layout will have to be changed
        aspect = (4.5 * 24.) / 32.; // used to be 4.5??

        // listen for changes to the meter parameters
        meter.addPropertyChangeListener(this);

        // init GUI
        d1 = new JLabel(digits[0]);
        d2 = new JLabel(digits[0]);
        d3 = new JLabel(digits[0]);
        percent = new JLabel(percentIcon);
        decimal = new JLabel(decimalIcon);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(d1);
        getContentPane().add(d2);
        getContentPane().add(decimal);
        getContentPane().add(d3);
        getContentPane().add(percent);

        getContentPane().add(b = new JButton(Bundle.getMessage("ButtonStop")));
        b.addActionListener(new ButtonListener());
        // since Run/Stop button looks crummy, don't display for now
        b.setVisible(false);

        meter.enable();

        update();
        pack();

        // request callback to update time
        // Again, adding updates.
        java.beans.PropertyChangeListener du_listener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        };
        meter.addPropertyChangeListener(MultiMeter.CURRENT, du_listener);

        // Add component listener to handle frame resizing event
        this.addComponentListener(
                new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleImage();
            }
        });

        startHeight = this.getContentPane().getSize().height;
        startWidth = this.getContentPane().getSize().width;

    }

    // Added method to scale the clock digit images to fit the
    // size of the display window
    synchronized public void scaleImage() {
        int frameHeight = this.getContentPane().getSize().height;
        int frameWidth = this.getContentPane().getSize().width;

        double hscale = ((double)frameHeight)/((double)startHeight);
        double wscale = ((double)frameWidth)/((double)startWidth);
        double scale = hscale < wscale? hscale:wscale;

        for (int i = 0; i < 10; i++) {
            digits[i].scale(scale,this);
        }
        percentIcon.scale(scale,this);
        decimalIcon.scale(scale,this);
    }

    synchronized void update() {
        float val = meter.getCurrent(); // should be a value between 0-99%

        int v1 = (int) (val * 10); // first decimal digit
        int v2 = ((int) (val * 100)) % 10; // second decimal digit.
        int v3 = ((int) (val * 1000)) % 10; // third decimal digit.

        log.debug("Current update: val {} v1 {} v2 {} v3 {}", val, v1, v2, v3);

        d1.setIcon(digits[v1]);
        d2.setIcon(digits[v2]);
        d3.setIcon(digits[v3]);
    }

    @Override
    public void dispose() {
        meter.disable();
        meter.removePropertyChangeListener(this);
        super.dispose();
    }

    /**
     * Handle a change to clock properties
     */
    @Override
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

    static private class ButtonListener implements ActionListener {

        @Override
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

    private final static Logger log = LoggerFactory.getLogger(AmpMeterFrame.class);
}
