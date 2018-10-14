package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

/**
 * Action to create the TrackRoadEditFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrackRoadEditAction extends AbstractAction {

    private TrackEditFrame _frame;
    private TrackRoadEditFrame tref = null;

    public TrackRoadEditAction(TrackEditFrame frame) {
        super(Bundle.getMessage("MenuItemRoadOptions"));
        _frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (tref != null) {
            tref.dispose();
        }
        tref = new TrackRoadEditFrame();
        tref.initComponents(_frame._location, _frame._track);
    }
}
