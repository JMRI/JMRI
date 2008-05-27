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
 * @author			Bob Jacobsen    Copyright (C) 2006, 2008
 * @version         $Revision: 1.3 $
 */
public class RpsTrackingFrameAction 			extends AbstractAction {

	public RpsTrackingFrameAction(String s) { super(s);}

    public RpsTrackingFrameAction() {
        this("RPS Tracking Display");
    }

    public void actionPerformed(ActionEvent e) {
        jmri.util.JmriJFrame f = new jmri.util.JmriJFrame("RPS Tracking");

        f.addHelpMenu("package.jmri.jmrix.rps.trackingpanel.RpsTrackingFrame", true);

        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        
        // add controls; first, button for handling errors
        JPanel controls = new JPanel();
        controls.setLayout(new BoxLayout(controls, BoxLayout.Y_AXIS));
        showButton = new JCheckBox("Show error points");
        showButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                                showButtonChanged();
                        }
                });
        controls.add(showButton);

        // then alignment control
        panel = new RpsTrackingPanel();
        controls.add(new RpsTrackingControlPane(panel));
        controls.add(new JSeparator());

        panel.setSize(440,240);
        panel.setPreferredSize(new Dimension(440,240));
        panel.setOrigin(-20.,-20.);
        panel.setCoordMax(420.,220.);

        // combine
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, controls, panel);
        f.getContentPane().add(split);
 
        f.pack();        
        f.setVisible(true);
	}
	
	JCheckBox showButton;
	RpsTrackingPanel panel;
	
	void showButtonChanged() {
	    panel.setShowErrors(showButton.isSelected());
	}
}


/* @(#)RpsTrackingFrameAction.java */
