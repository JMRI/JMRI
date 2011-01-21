// StagingEditFrame.java

package jmri.jmrit.operations.locations;
import java.awt.GridBagLayout;
import java.util.ResourceBundle;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;


/**
 * Frame for user edit of a staging track
 * 
 * @author Dan Boudreau Copyright (C) 2008, 2011
 * @version $Revision: 1.17 $
 */

public class StagingEditFrame extends InterchangeEditFrame implements java.beans.PropertyChangeListener {

	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// check boxes
	JCheckBox swapLoadsCheckBox = new JCheckBox(rb.getString("SwapCarLoads"));
	JCheckBox emptyCheckBox = new JCheckBox(rb.getString("EmptyCarLoads"));
	JCheckBox loadCheckBox = new JCheckBox(rb.getString("LoadCarLoads"));

	JPanel panelLoad = panelOpt3;
	
	public StagingEditFrame() {
		super();
	}

	public void initComponents(Location location, Track track) {
		
		// setup the optional panel with staging stuff
		panelLoad.setLayout(new GridBagLayout());
		panelLoad.setBorder(BorderFactory.createTitledBorder(rb.getString("OptionalLoads")));
		addItem(panelLoad, swapLoadsCheckBox, 0, 1);
		addItem(panelLoad, emptyCheckBox, 0, 2);
		addItem(panelLoad, loadCheckBox, 0, 3);
		
		super.initComponents(location, track);
		_type = Track.STAGING;
		
		addHelpMenu("package.jmri.jmrit.operations.Operations_Staging", true);
		
		// override text strings for tracks
		panelTrainDir.setBorder(BorderFactory.createTitledBorder(rb.getString("TrainStaging")));
		paneCheckBoxes.setBorder(BorderFactory.createTitledBorder(rb.getString("TypesStaging")));
		deleteTrackButton.setText(rb.getString("DeleteStaging"));
		addTrackButton.setText(rb.getString("AddStaging"));
		saveTrackButton.setText(rb.getString("SaveStaging"));
		
		// setup the check boxes
		if (_track !=null){
			swapLoadsCheckBox.setSelected(_track.isLoadSwapEnabled());
			emptyCheckBox.setSelected(_track.isRemoveLoadsEnabled());
			loadCheckBox.setSelected(_track.isAddLoadsEnabled());
		}
		// finish
		packFrame();
		setVisible(true);
	}
	
	protected void saveTrack (Track track){
		track.enableLoadSwaps(swapLoadsCheckBox.isSelected());
		track.enableRemoveLoads(emptyCheckBox.isSelected());
		track.enableAddLoads(loadCheckBox.isSelected());
		super.saveTrack(track);
	}
	
	protected void enableButtons(boolean enabled){
		swapLoadsCheckBox.setEnabled(enabled);
		emptyCheckBox.setEnabled(enabled);
		loadCheckBox.setEnabled(enabled);
		super.enableButtons(enabled);
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(StagingEditFrame.class.getName());
}
