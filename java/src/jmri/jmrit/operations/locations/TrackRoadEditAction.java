//TrackRoadEditAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;

import javax.swing.*;

/**
 * Action to create the TrackRoadEditFrame.
 * 
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class TrackRoadEditAction extends AbstractAction {
	
	private Track _track;
	private Location _location;

	public TrackRoadEditAction(Location location, Track track) {
		super(Bundle.getMessage("MenuItemRoadOptions"));
		_location = location;
		_track = track;
	}

	public void actionPerformed(ActionEvent e) {
		TrackRoadEditFrame tref = new TrackRoadEditFrame();
		tref.initComponents(_location, _track);		
	}
}

