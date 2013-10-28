// TrackCopyFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.setup.Control;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.text.MessageFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Frame for copying a track for operations.
 * 
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 17977 $
 */
public class TrackCopyFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	// text field
	JTextField trackNameTextField = new javax.swing.JTextField(Control.max_len_string_track_name);

	// major buttons
	JButton copyButton = new javax.swing.JButton(Bundle.getMessage("Copy"));

	// combo boxes
	JComboBox locationBox = LocationManager.instance().getComboBox();
	JComboBox trackBox = new JComboBox();

	Location _location;	// Copy the track to this location

	public TrackCopyFrame(Location location) {
		_location = location;

		// general GUI config

		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels

		// Layout the panel by rows
		// row 1
		JPanel pName = new JPanel();
		pName.setLayout(new GridBagLayout());
		pName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrackName")));
		addItem(pName, trackNameTextField, 0, 0);

		// row 2
		JPanel pCopy = new JPanel();
		pCopy.setLayout(new GridBagLayout());
		pCopy.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("SelectTrackToCopy")));
		addItem(pCopy, locationBox, 0, 0);
		addItem(pCopy, trackBox, 1, 0);

		// row 4
		JPanel pButton = new JPanel();
		pButton.add(copyButton);

		getContentPane().add(pName);
		getContentPane().add(pCopy);
		getContentPane().add(pButton);

		addComboBoxAction(locationBox);

		// get notified if combo box gets modified
		LocationManager.instance().addPropertyChangeListener(this);

		// add help menu to window
		addHelpMenu("package.jmri.jmrit.operations.Operations_Tracks", true); // NOI18N

		pack();
		setMinimumSize(new Dimension(Control.mediumPanelWidth, Control.smallPanelHeight));

		if (_location != null)
			setTitle(MessageFormat.format(Bundle.getMessage("TitleCopyTrack"), new Object[] { _location.getName() }));

		// setup buttons
		addButtonAction(copyButton);
	}

	// location combo box
	public void comboBoxActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == locationBox) {
			updateTrackComboBox();
		}
	}

	protected void updateTrackComboBox() {
		log.debug("update location track combobox");
		if (locationBox.getSelectedItem() == null || locationBox.getSelectedItem().equals("")) {
			trackBox.removeAllItems();
		} else {
			log.debug("Copy Track Frame sees location: " + locationBox.getSelectedItem());
			Location l = (Location) locationBox.getSelectedItem();
			l.updateComboBox(trackBox);
		}
	}

	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == copyButton) {
			log.debug("copy track button activated");
			if (!checkName())
				return;
			if (trackBox.getSelectedItem() != null && !trackBox.getSelectedItem().equals("") && _location != null) {
				Track track = (Track) trackBox.getSelectedItem();
				track.copyTrack(trackNameTextField.getText(), _location);
			} else {
				JOptionPane.showMessageDialog(this, Bundle.getMessage("SelectLocationAndTrack"), Bundle
						.getMessage("SelectTrackToCopy"), JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	protected void updateComboBoxes() {
		LocationManager.instance().updateComboBox(locationBox);
	}

	private static final int MAX_NAME_LENGTH = Control.max_len_string_track_name;

	/**
	 * 
	 * @return true if name entered and isn't too long
	 */
	private boolean checkName() {
		if (trackNameTextField.getText().trim().equals("")) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("MustEnterName"), MessageFormat.format(Bundle
					.getMessage("CanNotTrack"), new Object[] { Bundle.getMessage("add") }), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if (trackNameTextField.getText().length() > MAX_NAME_LENGTH) {
			JOptionPane.showMessageDialog(this, MessageFormat.format(Bundle.getMessage("TrackNameLengthMax"),
					new Object[] { Integer.toString(MAX_NAME_LENGTH + 1) }), MessageFormat.format(Bundle
					.getMessage("CanNotTrack"), new Object[] { Bundle.getMessage("add") }), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		// check to see if track already exists
		if (_location == null)
			return false;
		Track check = _location.getTrackByName(trackNameTextField.getText(), null);
		if (check != null) {
			JOptionPane.showMessageDialog(this, Bundle.getMessage("TrackAlreadyExists"), MessageFormat.format(Bundle
					.getMessage("CanNotTrack"), new Object[] { Bundle.getMessage("add") }), JOptionPane.ERROR_MESSAGE);
			return false;
		}
		return true;
	}

	public void dispose() {
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("PropertyChange (" + e.getPropertyName() + ") new (" + e.getNewValue() + ")");
		if (e.getPropertyName().equals(LocationManager.LISTLENGTH_CHANGED_PROPERTY))
			updateComboBoxes();
	}

	static Logger log = LoggerFactory.getLogger(TrackCopyFrame.class.getName());
}
