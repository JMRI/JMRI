//ChangeTracksTypeAction.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;


/**
 * Action to change all of tracks at a location to the same type of track. Track
 * types are Spurs, Yards, Interchanges and Staging.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 18559 $
 */
public class ChangeTracksTypeAction extends AbstractAction {
		
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	private LocationEditFrame _lef;
	
	public ChangeTracksTypeAction(LocationEditFrame lef){
		super(rb.getString("MenuItemChangeTrackType"));
		_lef = lef;
	}
	
	 public void actionPerformed(ActionEvent e) {
		new ChangeTracksFrame(_lef);
	 }
	
}

class ChangeTracksFrame extends OperationsFrame{
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// radio buttons
    JRadioButton sidingRadioButton = new JRadioButton(rb.getString("Siding"));
    JRadioButton yardRadioButton = new JRadioButton(rb.getString("Yard"));
    JRadioButton interchangeRadioButton = new JRadioButton(rb.getString("Interchange"));
    JRadioButton stagingRadioButton = new JRadioButton(rb.getString("Staging")); 
    
    // major buttons
    JButton saveButton = new JButton(rb.getString("Save"));
    
    private LocationEditFrame _lef;
    private Location _location;
	
	public ChangeTracksFrame(LocationEditFrame lef){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
	    _lef = lef;
	    if (_lef._location == null){
	    	log.debug("location is null, change location track types not possible");
	    	return;
	    }
	    _location = _lef._location;
		
		// load the panel
	   	// row 1a
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
    	p1.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(rb.getString("TrackType"),new Object[]{_location.getName()})));
    	addItem(p1, sidingRadioButton, 0, 0);
    	addItem(p1, yardRadioButton, 1, 0);
    	addItem(p1, interchangeRadioButton, 2, 0);
    	addItem(p1, stagingRadioButton, 3, 0);
    	addItem(p1, saveButton, 2, 1);
    	
    	// group and set current track type
    	ButtonGroup group = new ButtonGroup();
    	group.add(sidingRadioButton);
    	group.add(yardRadioButton);
    	group.add(interchangeRadioButton);
    	group.add(stagingRadioButton);
    	
    	// button action
    	addButtonAction(saveButton);
    	
    	getContentPane().add(p1);
    	setTitle(rb.getString("MenuItemChangeTrackType"));
    	pack();
    	if (getWidth() < 250)
    		setSize(getWidth()+100, getHeight());
    	setVisible(true); 	
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){	
			// check to see if button has changed
			if (sidingRadioButton.isSelected()){
				changeTracks(Track.SIDING);
			} else if (yardRadioButton.isSelected()){
				changeTracks(Track.YARD);
			} else if (interchangeRadioButton.isSelected()){
				changeTracks(Track.INTERCHANGE);
			} else if (stagingRadioButton.isSelected()){
				changeTracks(Track.STAGING);
			}
		}		
	}
	
	private void changeTracks(String type){
		log.debug("change tracks to "+type);
		List<String> ids = _location.getTrackIdsByNameList(null);
		for (int i=0; i<ids.size(); i++){
			Track track = _location.getTrackById(ids.get(i));
			track.setLocType(type);
		}
		if (type.equals(Track.STAGING))
			_location.setLocationOps(Location.STAGING);
		else
			_location.setLocationOps(Location.NORMAL);
		OperationsXml.save();
		_lef.dispose();
		dispose();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ChangeTracksFrame.class.getName());
}
