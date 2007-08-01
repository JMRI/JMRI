// RpsTrackingFrameAction.java

package jmri.jmrix.rps.trackingpanel;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Swing action to create and register a
 *       			RpsTrackingFrame object
 *
 * @author			Bob Jacobsen    Copyright (C) 2006
 * @version         $Revision: 1.1 $
 */
public class RpsTrackingFrameAction 			extends AbstractAction {

	public RpsTrackingFrameAction(String s) { super(s);}

    public RpsTrackingFrameAction() {
        this("RPS Tracking Display");
    }

    public void actionPerformed(ActionEvent e) {
        JFrame f = new jmri.util.JmriJFrame("RPS Tracking");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        
        // add button for handling errors
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        controls.add(new JLabel("Show error points: "));
        showButton = new JCheckBox();
        showButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                                showButtonChanged();
                        }
                });
        controls.add(showButton);
        controls.add(new JSeparator());
        f.getContentPane().add(controls);
           
        panel = new RpsTrackingPanel();
        panel.setSize(440,240);
        panel.setOrigin(-20.,-20.);
        panel.setCoordMax(420.,220.);
        f.getContentPane().add(panel);
        
        f.setSize(400,600);
        f.setVisible(true);
	}
	
	JCheckBox showButton;
	RpsTrackingPanel panel;
	
	void showButtonChanged() {
	    panel.setShowErrors(showButton.isSelected());
	}
}


/* @(#)RpsTrackingFrameAction.java */
