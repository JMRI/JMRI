//TrackLoadEditAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;

import javax.swing.*;

/**
 * Action to create the TrackLoadEditFrame.
 * 
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class TrackLoadEditAction extends AbstractAction {
	
	private Track _track;
	private Location _location;

	public TrackLoadEditAction(Location location, Track track) {
		super(Bundle.getMessage("MenuItemLoadOptions"));
		_location = location;
		_track = track;
	}

	public void actionPerformed(ActionEvent e) {
		TrackLoadEditFrame tlef = new TrackLoadEditFrame();
		tlef.initComponents(_location, _track);		
	}
}

