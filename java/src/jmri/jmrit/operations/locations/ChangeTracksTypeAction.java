//ChangeTracksTypeAction.java

package jmri.jmrit.operations.locations;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;


/**
 * Action to change all of tracks at a location to the same type of track. Track
 * types are Spurs, Yards, Interchanges and Staging.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @version $Revision: 18559 $
 */
public class ChangeTracksTypeAction extends AbstractAction {
			
	private LocationEditFrame _lef;
	
	public ChangeTracksTypeAction(LocationEditFrame lef){
		super(Bundle.getMessage("MenuItemChangeTrackType"));
		_lef = lef;
	}
	
	 public void actionPerformed(ActionEvent e) {
		new ChangeTracksFrame(_lef);
	 }
	
}

class ChangeTracksFrame extends OperationsFrame{
		
	// radio buttons
    JRadioButton spurRadioButton = new JRadioButton(Bundle.getMessage("Spur"));
    JRadioButton yardRadioButton = new JRadioButton(Bundle.getMessage("Yard"));
    JRadioButton interchangeRadioButton = new JRadioButton(Bundle.getMessage("Interchange"));
    JRadioButton stagingRadioButton = new JRadioButton(Bundle.getMessage("Staging")); 
    
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("Save"));
    
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
    	p1.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(Bundle.getMessage("TrackType"),new Object[]{_location.getName()})));
    	addItem(p1, spurRadioButton, 0, 0);
    	addItem(p1, yardRadioButton, 1, 0);
    	addItem(p1, interchangeRadioButton, 2, 0);
    	addItem(p1, stagingRadioButton, 3, 0);
    	addItem(p1, saveButton, 2, 1);
    	
    	// group and set current track type
    	ButtonGroup group = new ButtonGroup();
    	group.add(spurRadioButton);
    	group.add(yardRadioButton);
    	group.add(interchangeRadioButton);
    	group.add(stagingRadioButton);
    	
    	// button action
    	addButtonAction(saveButton);
    	
    	getContentPane().add(p1);
    	setTitle(Bundle.getMessage("MenuItemChangeTrackType"));
    	pack();
    	setMinimumSize(new Dimension(Control.smallPanelWidth, Control.tinyPanelHeight));
    	setVisible(true); 	
	}
	
	public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
		if (ae.getSource() == saveButton){	
			// check to see if button has changed
			if (spurRadioButton.isSelected()){
				changeTracks(Track.SPUR);
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
			track.setTrackType(type);
		}
		if (type.equals(Track.STAGING))
			_location.setLocationOps(Location.STAGING);
		else
			_location.setLocationOps(Location.NORMAL);
		OperationsXml.save();
		_lef.dispose();
		dispose();
	}
	
	static Logger log = LoggerFactory.getLogger(ChangeTracksFrame.class.getName());
}
