package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.setup.Setup;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Track tool to enable the display of track moves.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2014
 */
@API(status = MAINTAINED)
public class ShowTrackMovesAction extends AbstractAction {

    public ShowTrackMovesAction() {
        super(Bundle.getMessage("MenuItemShowTrackMoves"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // toggle
        Setup.setShowTrackMovesEnabled(!Setup.isShowTrackMovesEnabled());
    }
}


