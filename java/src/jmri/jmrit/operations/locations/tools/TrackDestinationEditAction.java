package jmri.jmrit.operations.locations.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.locations.gui.TrackEditFrame;

/**
 * Action to create the TrackDestinationEditFrame.
 *
 * @author Daniel Boudreau Copyright (C) 2013
 * 
 */
public class TrackDestinationEditAction extends AbstractAction {

    private TrackEditFrame _tef = null;
    private TrackDestinationEditFrame tdef = null;

    public TrackDestinationEditAction(TrackEditFrame tef) {
        super(Bundle.getMessage("MenuItemDestinations"));
       _tef = tef;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (tdef != null) {
            tdef.dispose();
        }
        tdef = new TrackDestinationEditFrame();
        tdef.initComponents(_tef);
    }
}
