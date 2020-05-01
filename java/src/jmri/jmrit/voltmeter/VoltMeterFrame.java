package jmri.jmrit.voltmeter;

import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.MultiMeter;
import jmri.jmrit.catalog.NamedIcon;
import jmri.util.JmriJFrame;

/**
 * Frame providing a simple LCD-based display of track voltage.
 * <p>
 * @author Ken Cameron Copyright (C) 2007
 * @author Mark Underwood Copyright (C) 2007
 * @author Andrew Crosland Copyright (C) 2020
 *
 * Adapted from ampmeter to display voltage.
 */
public class VoltMeterFrame extends JmriJFrame {

    // GUI member declarations
    ArrayList<JLabel> digitIcons;
    JLabel decimal;
    JLabel volt;

    private int displayLength;
    private boolean displayDP;
    private int startWidth;
    private int startHeight;

    MultiMeter meter;

    NamedIcon digits[] = new NamedIcon[10];
    NamedIcon decimalIcon;
    NamedIcon voltIcon;

    JPanel pane1;
    JPanel meterPane;
    
    public VoltMeterFrame() {
        super(Bundle.getMessage("TrackVoltageMeterTitle"));

        meter = InstanceManager.getDefault(MultiMeter.class);
    }

    @Override
    public void initComponents() {
        //Load the images (these are now the larger version of the original gifs
        for (int i = 0; i < 10; i++) {
            digits[i] = new NamedIcon("resources/icons/misc/LCD/Lcd_" + i + "b.GIF", "resources/icons/misc/LCD/Lcd_" + i + "b.GIF");
        }
        decimalIcon = new NamedIcon("resources/icons/misc/LCD/decimalb.gif", "resources/icons/misc/LCD/decimalb.gif");
        voltIcon = new NamedIcon("resources/icons/misc/LCD/voltb.gif", "resources/icons/misc/LCD/voltb.gif");

        // Voltage readings are displayed as 3 digits with one decimal place
        displayLength = 3;
        displayDP = true;
        
        // init GUI
        digitIcons = new ArrayList<JLabel>(displayLength); // 1 decimal place precision.
        for(int i = 0;i<displayLength;i++) {
           digitIcons.add(i,new JLabel(digits[0]));
        }
        decimal = new JLabel(decimalIcon);
        volt = new JLabel(voltIcon);

        buildContents();

        // Initially we want to scale the icons to fit the previously saved window size
        getStartDimensions();
        scaleImage();
        buildContents();
        
        meter.enable();

        update();

        // request callback to update time
        // Again, adding updates.
        java.beans.PropertyChangeListener du_listener = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                update();
            }
        };
        meter.addPropertyChangeListener(MultiMeter.VOLTAGE, du_listener);

        // Add component listener to handle frame resizing event
        this.addComponentListener(
                new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                scaleImage();
            }
        });

    }

    // Added method to scale the clock digit images to fit the
    // size of the display window
    synchronized public void scaleImage() {
        int frameHeight = this.getContentPane().getHeight()
                - meterPane.getInsets().top - meterPane.getInsets().bottom;
        int frameWidth = this.getContentPane().getWidth()
                - meterPane.getInsets().left - meterPane.getInsets().right;

        double hscale = ((double)frameHeight)/((double)startHeight);
        double wscale = ((double)frameWidth)/((double)startWidth);
        double scale = hscale < wscale? hscale:wscale;

        for (int i = 0; i < 10; i++) {
            digits[i].scale(scale,this);
        }
        decimalIcon.scale(scale,this);
        voltIcon.scale(scale,this);

        meterPane.revalidate();
        this.getContentPane().revalidate();
    }

    private void buildContents(){
        // clear the contents
        getContentPane().removeAll();

        pane1 = new JPanel();
        pane1.setLayout(new BoxLayout(pane1, BoxLayout.Y_AXIS));
        
        meterPane = new JPanel();
        meterPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createEtchedBorder(), Bundle.getMessage("TrackVoltage")));

        // build the actual multimeter display.
        meterPane.setLayout(new BoxLayout(meterPane, BoxLayout.X_AXIS));

        for(int i=0;i<digitIcons.size()-1;i++){
            meterPane.add(digitIcons.get(i));
        }
        meterPane.add(decimal);
        meterPane.add(digitIcons.get(digitIcons.size()-1));
        meterPane.add(volt);

        pane1.add(meterPane);
        getContentPane().add(pane1);
        
        getContentPane().setPreferredSize(meterPane.getPreferredSize());
        
        pack();
    }

    /**
     * Update the displayed value.
     * 
     * Assumes an integer value has an extra, non-displayed decimal digit.
     */
    synchronized void update() {
        float val = meter.getVoltage();
        int value = (int)Math.floor(val *10); // keep one decimal place.
        boolean scaleChanged = false;
        // autoscale the array of labels.
        if (displayDP == true) {
            while( (value) > (Math.pow(10,digitIcons.size())-1)) {
               digitIcons.add(0,new JLabel(digits[0]));
               scaleChanged = true;
            }
        } else {
            while( (value) > ((Math.pow(10,digitIcons.size()-1))-1)) {
               digitIcons.add(0,new JLabel(digits[0]));
               scaleChanged = true;
            }
        }

        if (scaleChanged){
            // clear the content pane and rebuild it.
            getStartDimensions();
            scaleImage();
            buildContents();
        }

        for(int i = digitIcons.size()-1; i>=0; i--){
            digitIcons.get(i).setIcon(digits[value%10]);
            value = value / 10;
        }
    }

    /**
     * Get the starting dimensions based on the size required by the display
     */
    void getStartDimensions() {
        startHeight = digits[0].getIconHeight();
        startWidth = digits[0].getIconWidth() * (displayLength + 1);
        if (displayDP) {
            startWidth += decimalIcon.getIconWidth();
        } else {
            startWidth -= digits[0].getIconWidth(); // non-displayed digit
        }
    }
    
    @Override
    public void dispose() {
        meter.disable();
        super.dispose();
    }

}
