//PoolTrackAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;

import javax.swing.*;

import java.util.ResourceBundle;

/**
 * Action to create a track pool and place a track in that pool.
 * 
 * @author Daniel Boudreau Copyright (C) 2011
 * @author Gregory Madsen Copyright (C) 2012
 * @version $Revision$
 */
public class PoolTrackAction extends AbstractAction {

	static final ResourceBundle rb = ResourceBundle
			.getBundle("jmri.jmrit.operations.locations.JmritOperationsLocationsBundle");

	private TrackEditFrame _tef;
	private PoolTrackFrame _ptf;

	public PoolTrackAction(TrackEditFrame tef) {
		super(rb.getString("MenuItemPoolTrack"));
		_tef = tef;
	}

	public void actionPerformed(ActionEvent e) {
		if (_ptf != null)
			_ptf.dispose();
		_ptf = new PoolTrackFrame(_tef);
	}
}

