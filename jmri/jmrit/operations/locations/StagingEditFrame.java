// StagingEditFrame.java

package jmri.jmrit.operations.locations;
import java.util.ResourceBundle;


/**
 * Frame for user edit of a location sidings
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.9 $
 */

public class StagingEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	public StagingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.STAGING;
		super.initComponents(location, track);
		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Staging", true);
		
		// override text strings for tracks
		textTrain.setText(rb.getString("TrainStaging"));
		textType.setText(rb.getString("TypesStaging"));
		deleteTrackButton.setText(rb.getString("DeleteStaging"));
		addTrackButton.setText(rb.getString("AddStaging"));
		saveTrackButton.setText(rb.getString("SaveStaging"));
	}
	
	static org.apache.log4j.Category log = org.apache.log4j.Category
			.getInstance(StagingEditFrame.class.getName());
}
