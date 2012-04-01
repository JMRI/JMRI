//ChangeTrackTypeAction.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.ResourceBundle;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;


/**
 * Action to change the type of track.  Track types are Spurs, Yards, Interchanges and
 * Staging.
 * @author Daniel Boudreau Copyright (C) 2010
 * @version     $Revision$
 */
public class ChangeTrackTypeAction extends AbstractAction {
		
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	private TrackEditFrame _tef;
	
	public ChangeTrackTypeAction(TrackEditFrame tef){
		super(rb.getString("MenuItemChangeTrackType"));
		_tef = tef;
	}
	
	 public void actionPerformed(ActionEvent e) {
		new ChangeTrackFrame(_tef);
	 }
	
}

class ChangeTrackFrame extends OperationsFrame{
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");
	
	// radio buttons
    JRadioButton sidingRadioButton = new JRadioButton(rb.getString("Siding"));
    JRadioButton yardRadioButton = new JRadioButton(rb.getString("Yard"));
    JRadioButton interchangeRadioButton = new JRadioButton(rb.getString("Interchange"));
    ButtonGroup group = new ButtonGroup();
    
    // major buttons
    JButton saveButton = new JButton(rb.getString("Save"));
    
    private TrackEditFrame _tef;
    String _trackType ="";
	
	public ChangeTrackFrame(TrackEditFrame tef){
		super();
		
		// the following code sets the frame's initial state
	    getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
	    
	    _tef = tef;
	    if (_tef._track == null){
	    	log.debug("track is null, change track not possible");
	    	return;
	    }
	    String trackName = _tef._track.getName();
		
		// load the panel
	   	// row 1a
    	JPanel p1 = new JPanel();
    	p1.setLayout(new GridBagLayout());
    	p1.setBorder(BorderFactory.createTitledBorder(MessageFormat.format(rb.getString("TrackType"),new Object[]{trackName})));
    	addItem(p1, sidingRadioButton, 0, 0);
    	addItem(p1, yardRadioButton, 1, 0);
    	addItem(p1, interchangeRadioButton, 2, 0);
    	addItem(p1, saveButton, 1, 1);
    	
    	// group and set current track type
    	_trackType = tef._track.getLocType();
    	group.add(sidingRadioButton);
    	group.add(yardRadioButton);
    	group.add(interchangeRadioButton);
    	
    	sidingRadioButton.setSelected(_trackType.equals(Track.SIDING));
    	yardRadioButton.setSelected(_trackType.equals(Track.YARD));
    	interchangeRadioButton.setSelected(_trackType.equals(Track.INTERCHANGE));
    	
    	// Can not change staging tracks!
    	saveButton.setEnabled(!_trackType.equals(Track.STAGING));
    	
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
			if (sidingRadioButton.isSelected() && !_trackType.equals(Track.SIDING)){
				changeTrack(Track.SIDING);
			} else if (yardRadioButton.isSelected() && !_trackType.equals(Track.YARD)){
				changeTrack(Track.YARD);
			} else if (interchangeRadioButton.isSelected() && !_trackType.equals(Track.INTERCHANGE)){
				changeTrack(Track.INTERCHANGE);
			}
		}		
	}
	
	private void changeTrack(String type){
		log.debug("change track to "+type);
		_tef._track.setLocType(type);
		OperationsXml.save();
		_tef.dispose();
		dispose();
	}
	
	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ChangeTrackFrame.class.getName());
}
