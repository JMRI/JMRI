// KnownLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.roster.Roster;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * Provide GUI controls to select a known loco via the Roster.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class KnownLocoSelPane extends javax.swing.JPanel  {
		
	public KnownLocoSelPane() {
		JLabel last;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		JLabel l2 = new JLabel("Known locomotive on programming track");
		l2.setBorder(new EmptyBorder(6,0,6,0));
		add(l2);
			JPanel pane2a = new JPanel();
			pane2a.setLayout(new BoxLayout(pane2a, BoxLayout.X_AXIS));
			pane2a.add(new JLabel("Select from roster:"));
			JButton idloco = new JButton("Identify locomotive");
			pane2a.add(idloco);
			pane2a.setAlignmentX(JLabel.LEFT_ALIGNMENT);				
		add(pane2a);
			
		JComboBox co = Roster.instance().matchingComboBox(null, null, null, null, null, null, null);
		add(co);
		
		JButton go2 = new JButton("Open programmer");
		add(go2);
		setBorder(new EmptyBorder(6,6,6,6));
	}
	
	// static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(KnownLocoSelPane.class.getName());

}
