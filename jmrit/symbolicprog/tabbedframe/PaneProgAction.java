/** 
 * PaneProgAction.java
 *
 * Description:		Swing action to create and register a 
 *       			SymbolicProg object
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			
 */

package jmri.jmrit.symbolicprog.tabbedframe;

import jmri.jmrit.symbolicprog.KnownLocoSelPane;
import jmri.jmrit.symbolicprog.NewLocoSelPane;
import jmri.jmrit.decoderdefn.*;
import jmri.jmrit.roster.*;

import java.awt.event.*;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.*;
import org.jdom.*;
import org.jdom.input.*;

public class PaneProgAction 			extends AbstractAction {

	public PaneProgAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {

		// create the initial frame that steers
		JFrame f = new JFrame("Tab-Programmer Setup");
		f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));

		// new Loco on programming track
		JLabel last;
		JPanel pane1 = new NewLocoSelPane(){
			protected void startProgrammer(DecoderFile decoderFile, String locoFile, RosterEntry re) {
				JFrame p = new PaneProgFrame(decoderFile, locoFile, re, "Program New Locomotive");
				p.pack();
				p.show();
			}
		};
		
		// Known loco on programming track
		JPanel pane2 = new KnownLocoSelPane(){
			protected void startProgrammer(DecoderFile decoderFile, String locoFile, RosterEntry re) {
				String title = "Program "+re.getId();
				JFrame p = new PaneProgFrame(decoderFile, locoFile, re, title);
				p.pack();
				p.show();
			}
		};
			
		// update roster button
		JPanel pane4 = new JPanel();
			JButton updateRoster;
			pane4.add(updateRoster = new JButton("Update Roster"));
			pane4.setBorder(new EmptyBorder(6,6,6,6));
			pane4.setAlignmentX(JLabel.LEFT_ALIGNMENT);
			updateRoster.setEnabled(false);
			updateRoster.setToolTipText("disable because not yet implemented");
			
		// load primary frame
		f.getContentPane().add(pane1);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		f.getContentPane().add(pane2);
		f.getContentPane().add(new JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		f.getContentPane().add(pane4);
		
		f.pack();	
		f.show();	
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PaneProgAction.class.getName());

}


/* @(#)PanecProgAction.java */
