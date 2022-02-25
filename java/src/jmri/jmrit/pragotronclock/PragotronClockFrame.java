package jmri.jmrit.pragotronclock;

import java.awt.Color;
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

/**
 * Frame providing a simple clock showing Pragotron clock.
 * <p>
 * A Run/Stop button is built into this, but because I don't like the way it
 * looks, it's not currently displayed in the GUI.
 *
 * @author Petr Sidlo Copyright (C) 2019
 *
 * Based on Nixie clock by Bob Jacobsen.
 */
public class PragotronClockFrame extends JmriJFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JLabel h24;  // hours
    JLabel m1;  // msb of minutes
    JLabel m2;
    JLabel colon;

    double aspect;
    double iconAspect10;
    double iconAspectDot;
    double iconAspect24;

    Timebase clock;

    NamedIcon[] foldingSheets10 = new NamedIcon[10];
    NamedIcon[] baseFoldingSheets10 = new NamedIcon[10];
    NamedIcon[] foldingSheets24 = new NamedIcon[24];
    NamedIcon[] baseFoldingSheets24 = new NamedIcon[24];
    NamedIcon colonIcon;
    NamedIcon baseColon;
    //"base" variables used to hold original gifs, other variables used with scaled images

    public PragotronClockFrame() {
        super(Bundle.getMessage("MenuItemPragotronClock"));

        this.getContentPane().setBackground(new Color(0x3D3D3D));    // set background to black

        clock = InstanceManager.getDefault(jmri.Timebase.class);

        //Load the images (these are now the larger version of the original gifs
        for (int i = 0; i < 10; i++) {
            baseFoldingSheets10[i] = new NamedIcon("resources/icons/misc/Pragotron/M" + i + ".png", "resources/icons/misc/Pragotron/M" + i + ".png");
            foldingSheets10[i] = new NamedIcon("resources/icons/misc/Pragotron/M" + i + ".png", "resources/icons/misc/Pragotron/M" + i + ".png");
        }
        for (int i = 0; i < 24; i++) {
            baseFoldingSheets24[i] = new NamedIcon("resources/icons/misc/Pragotron/H" + i + ".png", "resources/icons/misc/Pragotron/H" + i + ".png");
            foldingSheets24[i] = new NamedIcon("resources/icons/misc/Pragotron/H" + i + ".png", "resources/icons/misc/Pragotron/H" + i + ".png");
        }
        colonIcon = new NamedIcon("resources/icons/misc/Pragotron/dot.png", "resources/icons/misc/Pragotron/dot.png");
        baseColon = new NamedIcon("resources/icons/misc/Pragotron/dot.png", "resources/icons/misc/Pragotron/dot.png");
        // set initial size the same as the original gifs
        for (int i = 0; i < 10; i++) {
            Image scaledImage = baseFoldingSheets10[i].getImage().getScaledInstance(32, 48, Image.SCALE_SMOOTH);  // 152 / 192
            foldingSheets10[i].setImage(scaledImage);
        }
        for (int i = 0; i < 24; i++) {
            Image scaledImage = baseFoldingSheets24[i].getImage().getScaledInstance(80, 48, Image.SCALE_SMOOTH);  // 320 / 192
            foldingSheets24[i].setImage(scaledImage);
        }
        Image scaledImage = baseColon.getImage().getScaledInstance(10, 48, Image.SCALE_SMOOTH);  // 40 / 192
        colonIcon.setImage(scaledImage);

        // determine aspect ratio of a single digit graphic
        iconAspect10 = 152.0 / 192.0;       // 152 : 192
        iconAspect24 = 320.0 / 192.0;       // 320 : 192
        iconAspectDot = 40.0 / 192.0;       // 40 : 192

        // determine the aspect ratio of the 1 hour digit, dot and 2 minutes digit
        // this DOES NOT allow space for the Run/Stop button, if it is
        // enabled.  When the Run/Stop button is enabled, the layout will have to be changed
        if (!clock.getShowStopButton()) {
            aspect = (320.0 + 40.0 + 2 * 152.0) / 192.0; // pick up clock prefs choice: no button
        } else {
            aspect = (320.0 + 40.0 + 2 * 152.0 + 152.0) / 192.0; // pick up clock prefs choice: add 20. for a stop/start button
        }

        // listen for changes to the Timebase parameters
        clock.addPropertyChangeListener(this);

        // init GUI
        m1 = new JLabel(foldingSheets10[0]);
        m2 = new JLabel(foldingSheets10[0]);
        h24 = new JLabel(foldingSheets24[0]);
        colon = new JLabel(colonIcon);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(h24);
        getContentPane().add(colon);
        getContentPane().add(m1);
        getContentPane().add(m2);

        getContentPane().add(b = new JButton(Bundle.getMessage("ButtonPauseClock")));
        b.addActionListener(new ButtonListener());
        // since Run/Stop button looks crummy, user may turn it on in clock prefs
        b.setVisible(clock.getShowStopButton()); // pick up clock prefs choice
        updateButtonText();
        update();
        pack();

        // request callback to update time
        clock.addMinuteChangeListener((java.beans.PropertyChangeEvent e) -> {
            update();
        });

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
    public void scaleImage() {
        int iconHeight10;
        int iconWidth10;
        int iconHeight24;
        int iconWidth24;
        int iconHeightDot;
        int iconWidthDot;
        int frameHeight = this.getContentPane().getSize().height;
        int frameWidth = this.getContentPane().getSize().width;
        if ((double) frameWidth / (double) frameHeight > aspect) {
            iconHeight10 = frameHeight;
            iconWidth10 = (int) (iconAspect10 * iconHeight10);
            iconHeight24 = frameHeight;
            iconWidth24 = (int) (iconAspect24 * iconHeight24);
            iconHeightDot = frameHeight;
            iconWidthDot = (int) (iconAspectDot * iconHeightDot);
        } else {
            // this DOES NOT allow space for the Run/Stop button, if it is enabled.
            // When the Run/Stop button is enabled, the layout will change accordingly.
            iconWidth10 = (int) (frameWidth / 664.0 * 152.0);
            iconHeight10 = (int) (iconWidth10 / iconAspect10);
            iconWidth24 = (int) (frameWidth / 664.0 * 320.0);
            iconHeight24 = (int) (iconWidth24 / iconAspect24);
            iconWidthDot = (int) (frameWidth / 664.0 * 40.0);
            iconHeightDot = (int) (iconWidthDot / iconAspectDot);
        }
        for (int i = 0; i < 10; i++) {
            Image scaledImage = baseFoldingSheets10[i].getImage().getScaledInstance(iconWidth10, iconHeight10, Image.SCALE_SMOOTH);
            foldingSheets10[i].setImage(scaledImage);
        }
        for (int i = 0; i < 24; i++) {
            Image scaledImage = baseFoldingSheets24[i].getImage().getScaledInstance(iconWidth24, iconHeight24, Image.SCALE_SMOOTH);
            foldingSheets24[i].setImage(scaledImage);
        }
        Image scaledImage = baseColon.getImage().getScaledInstance(iconWidthDot , iconHeightDot, Image.SCALE_SMOOTH);
        colonIcon.setImage(scaledImage);

        // update the images on screen
        this.getContentPane().revalidate();
    }

    @SuppressWarnings("deprecation") // Date.getHours, getMinutes, getSeconds
    void update() {
        Date now = clock.getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();

        h24.setIcon(foldingSheets24[hours]);
        m1.setIcon(foldingSheets10[minutes / 10]);
        m2.setIcon(foldingSheets10[minutes - (minutes / 10) * 10]);
    }

    /**
     * Handle a change to clock properties.
     * @param e unused.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        updateButtonText();
    }

    /**
     * Update clock button text.
     */
    private void updateButtonText(){
        b.setText( Bundle.getMessage( clock.getRun() ? "ButtonPauseClock" : "ButtonRunClock") );
    }

    JButton b;

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent a) {
            clock.setRun(!clock.getRun());
            updateButtonText();
        }
    }

}
