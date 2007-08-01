// AlignmentPanelAction.java

package jmri.jmrix.rps.reversealign;

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
public class AlignmentPanelAction 			extends AbstractAction {

	public AlignmentPanelAction(String s) { super(s);}

    public AlignmentPanelAction() {
        this("RPS Alignment Tool");
    }

    public void actionPerformed(ActionEvent e) {
        JFrame f = new jmri.util.JmriJFrame("RPS Alignment");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
                        
        panel = new AlignmentPanel();
        panel.initComponents();
        f.getContentPane().add(panel);
        f.pack();
        f.setVisible(true);
	}
	
	AlignmentPanel panel;
	
}


/* @(#)AlignmentPanelAction.java */
