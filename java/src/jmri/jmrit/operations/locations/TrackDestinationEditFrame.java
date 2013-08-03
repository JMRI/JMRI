// TrackDestinationEditFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

import java.awt.*;
import java.util.List;

import javax.swing.*;

/**
 * Frame for user edit of track roads
 * 
 * @author Dan Boudreau Copyright (C) 2013
 * @version $Revision: 22371 $
 */

public class TrackDestinationEditFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

	Track _track = null;
	
	LocationManager locationManager = LocationManager.instance();

	// panels
	JPanel pControls = new JPanel();
	JPanel panelDestinations = new JPanel();
	JScrollPane paneDestinations = new JScrollPane(panelDestinations);

	// major buttons
	JButton saveTrackButton = new JButton(Bundle.getMessage("SaveTrack"));

	// radio buttons
	JRadioButton destinationsAll = new JRadioButton(Bundle.getMessage("AcceptAll"));
	JRadioButton destinationsInclude = new JRadioButton(Bundle.getMessage("AcceptOnly"));
	JRadioButton destinationsExclude = new JRadioButton(Bundle.getMessage("Exclude"));

	// labels
	JLabel trackName = new JLabel();

	public static final String DISPOSE = "dispose"; // NOI18N

	public TrackDestinationEditFrame() {
		super(Bundle.getMessage("TitleEditTrackDestinations"));
	}

	public void initComponents(Track track) {
		_track = track;

		// property changes

		// the following code sets the frame's initial state
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

		// Set up the panels
		// Layout the panel by rows
		// row 1
		JPanel p1 = new JPanel();
		p1.setLayout(new BoxLayout(p1, BoxLayout.X_AXIS));
		p1.setMaximumSize(new Dimension(2000, 250));

		// row 1a
		JPanel pTrackName = new JPanel();
		pTrackName.setLayout(new GridBagLayout());
		pTrackName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Track")));
		addItem(pTrackName, trackName, 0, 0);

		// row 1b
		JPanel pLocationName = new JPanel();
		pLocationName.setLayout(new GridBagLayout());
		pLocationName.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Location")));
		addItem(pLocationName, new JLabel(_track.getLocation().getName()), 0, 0);

		p1.add(pTrackName);
		p1.add(pLocationName);

		// row 3
		JPanel p3 = new JPanel();
		p3.setLayout(new BoxLayout(p3, BoxLayout.Y_AXIS));
		JScrollPane pane3 = new JScrollPane(p3);
		pane3.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("DestinationTrack")));
		pane3.setMaximumSize(new Dimension(2000, 400));

		JPanel pRadioButtons = new JPanel();
		pRadioButtons.setLayout(new FlowLayout());

		pRadioButtons.add(destinationsAll);
		pRadioButtons.add(destinationsInclude);
		pRadioButtons.add(destinationsExclude);

		p3.add(pRadioButtons);

		// row 4
		panelDestinations.setLayout(new GridBagLayout());
		paneDestinations.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("Destinations")));

		ButtonGroup bGroup = new ButtonGroup();
		bGroup.add(destinationsAll);
		bGroup.add(destinationsInclude);
		bGroup.add(destinationsExclude);

		// row 12
		JPanel panelButtons = new JPanel();
		panelButtons.setLayout(new GridBagLayout());
		panelButtons.setBorder(BorderFactory.createTitledBorder(""));
		panelButtons.setMaximumSize(new Dimension(2000, 200));

		// row 13
		addItem(panelButtons, saveTrackButton, 0, 0);

		getContentPane().add(p1);
		getContentPane().add(pane3);
		getContentPane().add(paneDestinations);
		getContentPane().add(panelButtons);

		// setup buttons
		addButtonAction(saveTrackButton);

		addRadioButtonAction(destinationsAll);
		addRadioButtonAction(destinationsInclude);
		addRadioButtonAction(destinationsExclude);

		// road fields and enable buttons
		if (_track != null) {
			_track.addPropertyChangeListener(this);
			trackName.setText(_track.getName());
			enableButtons(true);
		} else {
			enableButtons(false);
		}
		
		updateDestinations();

		// build menu
		// JMenuBar menuBar = new JMenuBar();
		// _toolMenu = new JMenu(Bundle.getMessage("Tools"));
		// menuBar.add(_toolMenu);
		// setJMenuBar(menuBar);

		setMinimumSize(new Dimension(Control.minPanelWidth, Control.panelHeight));
		pack();
		setVisible(true);
	}

	// Save, Delete, Add
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (_track == null)
			return;
		if (ae.getSource() == saveTrackButton) {
			log.debug("track save button activated");
			OperationsXml.save();
			if (Setup.isCloseWindowOnSaveEnabled())
				dispose();
		}
	}

	protected void enableButtons(boolean enabled) {
		saveTrackButton.setEnabled(enabled);
		destinationsAll.setEnabled(enabled);
		destinationsInclude.setEnabled(enabled);
		destinationsExclude.setEnabled(enabled);
	}

	public void radioButtonActionPerformed(java.awt.event.ActionEvent ae) {
		log.debug("radio button activated");
		if (ae.getSource() == destinationsAll) {
			_track.setDestinationOption(Track.ALL_DESTINATIONS);
		}
		if (ae.getSource() == destinationsInclude) {
			_track.setDestinationOption(Track.INCLUDE_DESTINATIONS);
		}
		if (ae.getSource() == destinationsExclude) {
			_track.setDestinationOption(Track.EXCLUDE_DESTINATIONS);
		}
		updateDestinations();
	}
	
	private void updateDestinations() {
		log.debug("Update destinations");
		panelDestinations.removeAll();
		if (_track != null) {
			destinationsAll.setSelected(_track.getDestinationOption().equals(Track.ALL_DESTINATIONS));
			destinationsInclude.setSelected(_track.getDestinationOption().equals(Track.INCLUDE_DESTINATIONS));
			destinationsExclude.setSelected(_track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS));
		}
		List<String> locIds = locationManager.getLocationsByNameList();
		for (int i = 0; i < locIds.size(); i++) {
			Location loc = locationManager.getLocationById(locIds.get(i));
			JCheckBox cb = new JCheckBox(loc.getName());
			addItemLeft(panelDestinations, cb, 0, i);
			cb.setEnabled(!destinationsAll.isSelected());
			addCheckBoxAction(cb);
			if (destinationsAll.isSelected()) {
				cb.setSelected(true);
			} else if (_track != null && _track.acceptsDestination(loc)) {
				cb.setSelected(true);
			}
		}
		panelDestinations.revalidate();
	}

	public void checkBoxActionPerformed(java.awt.event.ActionEvent ae) {
		JCheckBox b = (JCheckBox) ae.getSource();
		log.debug("checkbox change " + b.getText());
		if (_track == null)
			return;
		Location loc = locationManager.getLocationByName(b.getText());
		if (loc != null) {
			if (b.isSelected() ^ _track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS))
				_track.addDestination(loc);
			else
				_track.deleteDestination(loc);
		}
	}

	public void dispose() {
		if (_track != null)
			_track.removePropertyChangeListener(this);
		super.dispose();
	}

	public void propertyChange(java.beans.PropertyChangeEvent e) {
		if (Control.showProperty && log.isDebugEnabled())
			log.debug("Property change " + e.getPropertyName() + " old: " + e.getOldValue() + " new: "
					+ e.getNewValue()); // NOI18N
	}

	static Logger log = LoggerFactory.getLogger(TrackDestinationEditFrame.class.getName());
}
