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
	JTextField model			= new JTextField(12);
	JLabel dccAddress		= new JLabel();
	JTextField comment			= new JTextField(12);
	JLabel filename 		= new JLabel();
	JLabel decoderModel 	= new JLabel();
	JLabel decoderFamily 	= new JLabel();
	JTextField decoderComment 	= new JTextField(12);
	
	public RosterEntryPane(RosterEntry r) {
		id.setText(r.getId());
		filename.setText(r.getFileName());
		dccAddress.setText(r.getDccAddress());
		roadName.setText(r.getRoadName());
		roadNumber.setText(r.getRoadNumber());
		mfg.setText(r.getMfg());
		model.setText(r.getModel());
		comment.setText(r.getComment());
		decoderModel.setText(r.getDecoderModel());
		decoderFamily.setText(r.getDecoderFamily());
		decoderComment.setText(r.getDecoderComment());
		
		// add options
		id.setToolTipText("Identifies this locomotive in the roster");
		
		dccAddress.setToolTipText("This is filled in automatically by the program");
		decoderModel.setToolTipText("This is filled in automatically by your earlier selections");
		decoderFamily.setToolTipText("This is filled in automatically by your earlier selections");
		filename.setToolTipText("This is filled in automatically by the program");
		
		// assemble the GUI
		setLayout(new GridLayout(11,2));
		
		add(new JLabel("ID:"));
		add(id);
		
		add(new JLabel("Road Name:"));
		add(roadName);
		
		add(new JLabel("Road Number:"));
		add(roadNumber);
		
		add(new JLabel("Manufacturer:"));
		add(mfg);
		
		add(new JLabel("Model:"));
		add(model);
		
		add(new JLabel("DCC Address:"));
		add(dccAddress);
		
		add(new JLabel("Comment:"));
		add(comment);
		
		add(new JLabel("Decoder Family:"));
		add(decoderFamily);
		
		add(new JLabel("Decoder Model:"));
		add(decoderModel);
		
		add(new JLabel("Decoder Comment:"));
		add(decoderComment);
		
		add(new JLabel("Filename:"));
		add(filename);
		
	}
		
	public void update(RosterEntry r) {
		r.setId(id.getText());
		r.setRoadName(roadName.getText());
		r.setRoadNumber(roadNumber.getText());
		r.setMfg(mfg.getText());
		r.setModel(model.getText());
		r.setDccAddress(dccAddress.getText());
		r.setComment(comment.getText());
		r.setDecoderFamily(decoderFamily.getText());
		r.setDecoderModel(decoderModel.getText());
		r.setDecoderComment(decoderComment.getText());
	}
	
	public void setDccAddress(String a) { dccAddress.setText(a); }
	
	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntryPane.class.getName());

}
