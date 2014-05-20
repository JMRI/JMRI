// InterchangeEditFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.BorderFactory;

/**
 * Frame for user edit of a classification/interchange track. Adds two panels to TrackEditFram for train/route car drops
 * and pick ups.
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011, 2012
 * @version $Revision$
 */

public class InterchangeEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	public InterchangeEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.INTERCHANGE;

		super.initComponents(location, track);

		_toolMenu.add(new IgnoreUsedTrackAction(this));
		_toolMenu.add(new TrackDestinationEditAction(this));
		_toolMenu.add(new ChangeTrackTypeAction(this));
		_toolMenu.add(new ShowTrainsServingLocationAction(Bundle.getMessage("MenuItemShowTrainsTrack"), _location, _track));
		_toolMenu.add(new ShowCarsByLocationAction(false, location.getName(), trackName));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true); // NOI18N

		// override text strings for tracks
		// panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainInterchange")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesInterchange")));
		deleteTrackButton.setText(Bundle.getMessage("DeleteInterchange"));
		addTrackButton.setText(Bundle.getMessage("AddInterchange"));
		saveTrackButton.setText(Bundle.getMessage("SaveInterchange"));

		updateDestinationOption();

		// finish
		pack();
		setVisible(true);
	}

	private void updateDestinationOption() {
		if (_track != null) {
			if (_track.getDestinationOption().equals(Track.INCLUDE_DESTINATIONS)) {
				pDestinationOption.setVisible(true);
				destinationOption.setText(Bundle.getMessage("AcceptOnly") + " "
						+ _track.getDestinationListSize() + " " + Bundle.getMessage("Destinations"));
			} else if (_track.getDestinationOption().equals(Track.EXCLUDE_DESTINATIONS)) {
				pDestinationOption.setVisible(true);
				destinationOption.setText(Bundle.getMessage("Exclude")
						+ " "
						+ (LocationManager.instance().getNumberOfLocations() - _track
								.getDestinationListSize()) + " " + Bundle.getMessage("Destinations"));
			} else {
				destinationOption.setText(Bundle.getMessage("AcceptAll"));
			}
		}
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		super.propertyChange(e);
		if (e.getPropertyName().equals(Track.DESTINATIONS_CHANGED_PROPERTY)
				|| e.getPropertyName().equals(Track.DESTINATION_OPTIONS_CHANGED_PROPERTY)) {
			updateDestinationOption();			
		}
	}

	static Logger log = LoggerFactory.getLogger(InterchangeEditFrame.class.getName());
}
