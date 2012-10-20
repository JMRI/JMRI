//PoolTrackAction.java

package jmri.jmrit.operations.locations;

import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.*;

import java.text.MessageFormat;
import java.util.List;
import java.util.ResourceBundle;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;

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

