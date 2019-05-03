package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrit.operations.locations.TrackEditFrame;

/**
 * Action to create the TrackLoadEditFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrackLoadEditAction extends AbstractAction {

    private TrackEditFrame _frame;
    private TrackLoadEditFrame tlef = null;

    public TrackLoadEditAction(TrackEditFrame frame) {
        super(Bundle.getMessage("MenuItemLoadOptions"));
        _frame = frame;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (tlef != null) {
            tlef.dispose();
        }
        tlef = new TrackLoadEditFrame();
        tlef.initComponents(_frame._location, _frame._track);
    }
}
