// NixieClockFrame.java

package jmri.jmrit.nixieclock;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.Date;
import jmri.*;

import jmri.jmrit.display.PositionableLabel;
import jmri.jmrit.catalog.*;

/**
 * Frame providing a simple clock showing Nixie tubes.
 * <P>
 * A Run/Stop button is built into this, but because I 
 * don't like the way it looks, it's not currently 
 * displayed in the GUI.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class NixieClockFrame extends javax.swing.JFrame implements java.beans.PropertyChangeListener {

    // GUI member declarations
    JLabel h1;  // msb of hours
    JLabel h2;
    JLabel m1;  // msb of minutes
    JLabel m2;

    Timebase clock;
    javax.swing.Timer timer;
    static int delay = 2*1000;  // update display every two seconds

    static NamedIcon tubes[] = new NamedIcon[10];

    public NixieClockFrame() {

        clock = InstanceManager.timebaseInstance();

        tubes[0] = new NamedIcon("resources/icons/misc/Nixie/M0.gif", "resources/icons/misc/Nixie/M0.gif");
        tubes[1] = new NamedIcon("resources/icons/misc/Nixie/M1.gif", "resources/icons/misc/Nixie/M1.gif");
        tubes[2] = new NamedIcon("resources/icons/misc/Nixie/M2.gif", "resources/icons/misc/Nixie/M2.gif");
        tubes[3] = new NamedIcon("resources/icons/misc/Nixie/M3.gif", "resources/icons/misc/Nixie/M3.gif");
        tubes[4] = new NamedIcon("resources/icons/misc/Nixie/M4.gif", "resources/icons/misc/Nixie/M4.gif");
        tubes[5] = new NamedIcon("resources/icons/misc/Nixie/M5.gif", "resources/icons/misc/Nixie/M5.gif");
        tubes[6] = new NamedIcon("resources/icons/misc/Nixie/M6.gif", "resources/icons/misc/Nixie/M6.gif");
        tubes[7] = new NamedIcon("resources/icons/misc/Nixie/M7.gif", "resources/icons/misc/Nixie/M7.gif");
        tubes[8] = new NamedIcon("resources/icons/misc/Nixie/M8.gif", "resources/icons/misc/Nixie/M8.gif");
        tubes[9] = new NamedIcon("resources/icons/misc/Nixie/M9.gif", "resources/icons/misc/Nixie/M9.gif");

        // set time to now
        clock.setTime(new Date());
        try { clock.setRate(4.); } catch (Exception e) {}
        
        // listen for changes to the timebase parameters
        clock.addPropertyChangeListener(this);
        
        // init GUI
        m1 = new JLabel(tubes[0]);
        m2 = new JLabel(tubes[0]);
        h1 = new JLabel(tubes[0]);
        h2 = new JLabel(tubes[0]);

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
        getContentPane().add(h1);
        getContentPane().add(h2);
        getContentPane().add(new JLabel(":"));  // need a better way to do this!
        getContentPane().add(m1);
        getContentPane().add(m2);

        getContentPane().add(b = new JButton("Stop")); 
        b.addActionListener( new ButtonListener());
        // since Run/Stop button looks crummy, don't display for now
        b.setVisible(false);

        update();
        pack();

        // start timer
         if (timer==null) {
            timer = new javax.swing.Timer(delay, new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent e) {
                        update();
                    }
                });
        }
        timer.stop();
        timer.setInitialDelay(delay);
        timer.setRepeats(true);
        timer.start();
    }

    void update() {
        Date now = clock.getTime();
        int hours = now.getHours();
        int minutes = now.getMinutes();

        h1.setIcon(tubes[hours/10]);
        h2.setIcon(tubes[hours-(hours/10)*10]);
        m1.setIcon(tubes[minutes/10]);
        m2.setIcon(tubes[minutes-(minutes/10)*10]);
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
		if (now) b.setText("Stop");
		else b.setText("Run");
    }

    JButton b;

	private class ButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent a) {
			boolean next = !clock.getRun();
			clock.setRun(next);
			if (next) b.setText("Stop");
			else b.setText("Run ");
		}
	}
}
