// InterchangeEditFrame.java

package jmri.jmrit.operations.locations;
import java.util.ResourceBundle;

import javax.swing.*;


/**
 * Frame for user edit of a location sidings
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.9 $
 */

public class InterchangeEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	public InterchangeEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.INTERCHANGE;
		super.initComponents(location, track);
		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Interchanges", true);
		
		// override text strings for tracks
		textTrain.setText(rb.getString("TrainInterchange"));
		textType.setText(rb.getString("TypesInterchange"));
		deleteTrackButton.setText(rb.getString("DeleteInterchange"));
		addTrackButton.setText(rb.getString("AddInterchange"));
		saveTrackButton.setText(rb.getString("SaveInterchange"));
	}
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(InterchangeEditFrame.class.getName());
}
