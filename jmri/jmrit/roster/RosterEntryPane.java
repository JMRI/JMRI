// RosterEntryPane.java

package jmri.jmrit.roster;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.sun.java.util.collections.List;

/**
 * Display and edit a RosterEntry.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */
public class RosterEntryPane extends javax.swing.JPanel  {

	JTextField id 				= new JTextField(12);
	JTextField roadName 		= new JTextField(12);
	JTextField roadNumber 		= new JTextField(12);
	JTextField mfg 				= new JTextField(12);
	JTextField model			= new JTextField(12);
	JTextField owner			= new JTextField(12);
	JLabel dccAddress		= new JLabel();
	JTextField comment			= new JTextField(12);
	JLabel filename 		= new JLabel();
	JLabel decoderModel 	= new JLabel();
	JLabel decoderFamily 	= new JLabel();
	JTextField decoderComment 	= new JTextField(12);

	Component pane = null;
	RosterEntry re = null;

	public RosterEntryPane(RosterEntry r) {
		id.setText(r.getId());
		filename.setText(r.getFileName());
		dccAddress.setText(r.getDccAddress());
		roadName.setText(r.getRoadName());
		roadNumber.setText(r.getRoadNumber());
		mfg.setText(r.getMfg());
		owner.setText(r.getOwner());
		model.setText(r.getModel());
		comment.setText(r.getComment());
		decoderModel.setText(r.getDecoderModel());
		decoderFamily.setText(r.getDecoderFamily());
		decoderComment.setText(r.getDecoderComment());

		pane = this;
		re = r;

		// add options
		id.setToolTipText("Identifies this locomotive in the roster");

		dccAddress.setToolTipText("This is filled in automatically by the program");
		decoderModel.setToolTipText("This is filled in automatically by your earlier selections");
		decoderFamily.setToolTipText("This is filled in automatically by your earlier selections");
		filename.setToolTipText("This is filled in automatically by the program");

		id.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (checkDuplicate()) JOptionPane.showMessageDialog(pane, "This ID is a duplicate, please change it");

			}
		});

		// assemble the GUI
		setLayout(new GridLayout(12,2));

		add(new JLabel("ID:"));
		add(id);

		add(new JLabel("Road Name:"));
		add(roadName);

		add(new JLabel("Road Number:"));
		add(roadNumber);

		add(new JLabel("Manufacturer:"));
		add(mfg);

		add(new JLabel("Owner:"));
		add(owner);

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

    /**
     *
     * @return true if the value in the id JTextField
     * is a duplicate of some other RosterEntry in the roster
     */
	boolean checkDuplicate() {
		// check its not a duplicate
		List l = Roster.instance().matchingList(null, null, null, null, null, null, id.getText());
		boolean oops = false;
		for (int i=0; i<l.size(); i++) {
			if (re != (RosterEntry)l.get(i)) oops = true;
		}
		return oops;
	}

    /** Update GUI contents to be consistent with the contents of a RosterEntry object **/
	public void update(RosterEntry r) {
		r.setId(id.getText());
		r.setRoadName(roadName.getText());
		r.setRoadNumber(roadNumber.getText());
		r.setMfg(mfg.getText());
		r.setOwner(owner.getText());
		r.setModel(model.getText());
		r.setDccAddress(dccAddress.getText());
		r.setComment(comment.getText());
		r.setDecoderFamily(decoderFamily.getText());
		r.setDecoderModel(decoderModel.getText());
		r.setDecoderComment(decoderComment.getText());
	}

	public void setDccAddress(String a) { dccAddress.setText(a); }

	public void dispose() {
		if (log.isDebugEnabled()) log.debug("dispose");
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(RosterEntryPane.class.getName());

}
