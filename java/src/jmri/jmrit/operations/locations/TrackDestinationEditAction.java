//TrackDestinationEditAction.java

package jmri.jmrit.operations.locations;

import java.awt.event.*;

import javax.swing.*;

/**
 * Action to create the TrackDestinationEditFrame.
 * 
 * @author Daniel Boudreau Copyright (C) 2013
 * @version $Revision: 22219 $
 */
public class TrackDestinationEditAction extends AbstractAction {
	
	private TrackEditFrame _frame;
	private TrackDestinationEditFrame tdef = null;

	public TrackDestinationEditAction(TrackEditFrame frame) {
		super(Bundle.getMessage("MenuItemDestinations"));
		_frame = frame;
	}

	public void actionPerformed(ActionEvent e) {
		if (tdef != null)
			tdef.dispose();
		tdef = new TrackDestinationEditFrame();
		tdef.initComponents(_frame._track);		
	}
}

