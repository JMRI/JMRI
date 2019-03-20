package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

/**
 * Action to create the TrackDestinationEditFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrackDestinationEditAction extends AbstractAction {

    private TrackEditFrame _frame;
    private TrackDestinationEditFrame tdef = null;

    public TrackDestinationEditAction(TrackEditFrame frame) {
        super(Bundle.getMessage("MenuItemDestinations"));
        _frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (tdef != null) {
            tdef.dispose();
        }
        tdef = new TrackDestinationEditFrame();
        tdef.initComponents(_frame._track);
    }
}
