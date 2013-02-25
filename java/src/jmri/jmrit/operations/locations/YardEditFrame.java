// YardEditFrame.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TrainYard")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("TypesYard")));
		deleteTrackButton.setText(Bundle.getMessage("DeleteYard"));
		addTrackButton.setText(Bundle.getMessage("AddYard"));
		saveTrackButton.setText(Bundle.getMessage("SaveYard"));
		// finish
		dropPanel.setVisible(false); // don't show drop and pick up panel
		pickupPanel.setVisible(false);
		pack();
		setVisible(true);
	}

	static Logger log = LoggerFactory.getLogger(YardEditFrame.class
			.getName());
}
