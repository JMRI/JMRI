//ShowTrainsServingLocationAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;

import javax.swing.*;

/**
 * Action to create the ShowTrainsServingLocationFrame.
 * 
 * @author Daniel Boudreau Copyright (C) 2014
 * @version $Revision: 22219 $
 */
public class ShowTrainsServingLocationAction extends AbstractAction {

	public ShowTrainsServingLocationAction(String title, Location location, Track track) {
		super(title);
		_location = location;
		_track = track;
		setEnabled(_location != null);
	}
	
	Location _location;
	Track _track;
	ShowTrainsServingLocationFrame _frame;

	public void actionPerformed(ActionEvent e) {
		if (_frame != null)
			_frame.dispose();
		_frame = new ShowTrainsServingLocationFrame();
		_frame.initComponents(_location, _track);
	}
}

