// NewLocoSelPane.java

package jmri.jmrit.symbolicprog;

import jmri.jmrit.roster.Roster;
import jmri.jmrit.decoderdefn.DecoderIndexFile;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/** 
 * Provide GUI controls to select a decoder for a new loco and/or copy an existing config
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class NewLocoSelPane extends javax.swing.JPanel  {
			
	public NewLocoSelPane() {
		JLabel last;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		add(last = new JLabel("New locomotive on programming track"));
		last.setBorder(new EmptyBorder(6,0,6,0));
		add(new JLabel("Copy settings from existing locomotive:"));

		JComboBox co = Roster.instance().matchingComboBox(null, null, null, null, null, null);
		co.insertItemAt("<none>",0);
		co.setSelectedIndex(0);
		add(co);
		
			JPanel pane1a = new JPanel();
			pane1a.setLayout(new BoxLayout(pane1a, BoxLayout.X_AXIS));
			pane1a.add(new JLabel("Decoder installed:"));
			JButton iddecoder= new JButton("Identify decoder");
			pane1a.add(iddecoder);
			pane1a.setAlignmentX(JLabel.LEFT_ALIGNMENT);				
		add(pane1a);
		
		DecoderIndexFile.instance();
		String[] decoderLabels = {"<none>", "Lenz LE230", "Digitrax DH142", "Digitrax DH121"};
		add(new JComboBox(decoderLabels));
		JButton go1 = new JButton("Open programmer");
		add(go1);
		setBorder(new EmptyBorder(6,6,6,6));
	}

	// static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NewLocoSelPane.class.getName());

}
