// YardEditFrame.java

package jmri.jmrit.operations.locations;

import javax.swing.BorderFactory;

/**
 * Frame for user edit of a yard
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision$
 */

public class YardEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	public YardEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.YARD;
		super.initComponents(location, track);

		_toolMenu.add(new ChangeTrackTypeAction(this));
		addHelpMenu("package.jmri.jmrit.operations.Operations_Yards", true); // NOI18N

		// override text strings for tracks
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getString("TrainYard")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getString("TypesYard")));
		deleteTrackButton.setText(Bundle.getString("DeleteYard"));
		addTrackButton.setText(Bundle.getString("AddYard"));
		saveTrackButton.setText(Bundle.getString("SaveYard"));
		// finish
		dropPanel.setVisible(false); // don't show drop and pick up panel
		pickupPanel.setVisible(false);
		packFrame();
		setVisible(true);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(YardEditFrame.class
			.getName());
}
