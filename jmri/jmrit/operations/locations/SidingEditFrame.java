// SidingEditFrame.java

package jmri.jmrit.operations.locations;
import java.util.ResourceBundle;


/**
 * Frame for user edit of a location sidings
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.9 $
 */

public class SidingEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	public SidingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.SIDING;
		super.initComponents(location, track);
		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Sidings", true);
		
		// override text strings for tracks
		textTrain.setText(rb.getString("TrainSiding"));
		textType.setText(rb.getString("TypesSiding"));
		deleteTrackButton.setText(rb.getString("DeleteSiding"));
		addTrackButton.setText(rb.getString("AddSiding"));
		saveTrackButton.setText(rb.getString("SaveSiding"));
	}

	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(SidingEditFrame.class.getName());
}
