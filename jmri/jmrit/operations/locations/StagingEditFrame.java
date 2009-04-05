// StagingEditFrame.java

package jmri.jmrit.operations.locations;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;


/**
 * Frame for user edit of a staging track
 * 
 * @author Dan Boudreau Copyright (C) 2008
 * @version $Revision: 1.11 $
 */

public class StagingEditFrame extends TrackEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// labels
	JLabel textLoads = new JLabel(rb.getString("OptionalLoads"));
	
	// check boxes
	JCheckBox swapLoadsCheckBox = new JCheckBox(rb.getString("SwapCarLoads"));
	JCheckBox emptyCheckBox = new JCheckBox(rb.getString("EmptyCarLoads"));
	JCheckBox loadCheckBox = new JCheckBox(rb.getString("LoadCarLoads"));

	JPanel panelLoad = panelOpt1;
	
	public StagingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		_type = Track.STAGING;
		
		// setup the optional panel with schedule stuff
		// guarantee space for textSchError messages
		panelLoad.setLayout(new GridBagLayout());
		addItem(panelLoad, textLoads, 0, 0);
		addItem(panelLoad, swapLoadsCheckBox, 0, 1);
		addItem(panelLoad, emptyCheckBox, 0, 2);
		addItem(panelLoad, loadCheckBox, 0, 3);
		panelLoad.setBorder(border);
		
		super.initComponents(location, track);
		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Staging", true);
		
		// override text strings for tracks
		textTrain.setText(rb.getString("TrainStaging"));
		textType.setText(rb.getString("TypesStaging"));
		deleteTrackButton.setText(rb.getString("DeleteStaging"));
		addTrackButton.setText(rb.getString("AddStaging"));
		saveTrackButton.setText(rb.getString("SaveStaging"));
		
		// setup the check boxes
		if (_track !=null){
			swapLoadsCheckBox.setSelected(_track.isLoadSwapEnabled());
			emptyCheckBox.setSelected(_track.isRemoveLoadsEnabled());
			loadCheckBox.setSelected(_track.isAddLoadsEnabled());
		}
	}
	
	protected void saveTrack (Track track){
		track.enableLoadSwaps(swapLoadsCheckBox.isSelected());
		track.enableRemoveLoads(emptyCheckBox.isSelected());
		track.enableAddLoads(loadCheckBox.isSelected());
		super.saveTrack(track);
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(StagingEditFrame.class.getName());
}
