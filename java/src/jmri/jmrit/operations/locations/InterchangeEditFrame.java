// InterchangeEditFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.BorderFactory;

/**
 * Frame for user edit of an interchange track. Adds two panels to TrackEditFram for train/route car drops and pick ups.
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011
 * @version $Revision$
 */

public class InterchangeEditFrame extends TrackEditFrame implements
		java.beans.PropertyChangeListener {

	public InterchangeEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.INTERCHANGE;

		super.initComponents(location, track);

		_toolMenu.add(new IgnoreUsedTrackAction(this));
		_toolMenu.add(new TrackDestinationEditAction(this));
		_toolMenu.add(new ChangeTrackTypeAction(this));
		_toolMenu.add(new ShowCarsByLocationAction(false, location.getName(), trackName));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Interchange", true); // NOI18N

		// override text strings for tracks
		// panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainInterchange")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle
				.getMessage("TypesInterchange")));
		deleteTrackButton.setText(Bundle.getMessage("DeleteInterchange"));
		addTrackButton.setText(Bundle.getMessage("AddInterchange"));
		saveTrackButton.setText(Bundle.getMessage("SaveInterchange"));

		// finish
		pack();
		setVisible(true);
	}

	static Logger log = LoggerFactory
			.getLogger(InterchangeEditFrame.class.getName());
}
