// RosterEntryPane.java

package jmri.jmrit.roster;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/** 
 * Display and edit a RosterEntry.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			
 */
public class RosterEntryPane extends javax.swing.JPanel  {

	JTextField id 				= new JTextField(12);
	JTextField roadName 		= new JTextField(12);
	JTextField roadNumber 		= new JTextField(12);
	JTextField mfg 				= new JTextField(12);
	JLabel filename 		= new JLabel();
	JLabel dccAddress		= new JLabel();
	JLabel decoderModel 	= new JLabel();
	JLabel decoderFamily 	= new JLabel();
	
	public RosterEntryPane(RosterEntry r) {
		id.setText(r.getId());
		filename.setText(r.getFileName());
		dccAddress.setText(r.getDccAddress());
		roadName.setText(r.getRoadName());
		roadNumber.setText(r.getRoadNumber());
		mfg.setText(r.getMfg());
		decoderModel.setText(r.getDecoderModel());
		decoderFamily.setText(r.getDecoderFamily());
		
		// assemble the GUI
		setLayout(new GridLayout(8,2));
		
		add(new JLabel("ID:"));
		add(id);
		
		add(new JLabel("Road Name:"));
		add(roadName);
		
		add(new JLabel("Road Number:"));
		add(roadNumber);
		
		add(new JLabel("Manufacturer:"));
		add(mfg);
		
		add(new JLabel("DCC Address:"));
		add(dccAddress);
		
		add(new JLabel("Decoder Family:"));
		add(decoderFamily);
		
		add(new JLabel("Decoder Model:"));
		add(decoderModel);
		
		add(new JLabel("Filename:"));
		add(filename);
		
	}
		
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntryPane.class.getName());

}
