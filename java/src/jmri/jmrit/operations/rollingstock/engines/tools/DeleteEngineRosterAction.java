package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will remove all engines from the operation database.
 *
 * @author Dan Boudreau Copyright (C) 2007
 */
public class DeleteEngineRosterAction extends AbstractAction {

    EnginesTableFrame _enginesTableFrame;

    public DeleteEngineRosterAction(EnginesTableFrame enginesTableFrame) {
        super(Bundle.getMessage("MenuItemDelete"));
        // delete all cars on a track or location
        if (enginesTableFrame.enginesTableModel.trackName != null) {
            String actionName = Bundle.getMessage("MenuDeleteEnginesTrack",
                    enginesTableFrame.enginesTableModel.trackName);
            putValue(NAME, actionName);
        } else if (enginesTableFrame.enginesTableModel.locationName != null) {
            String actionName = Bundle.getMessage("MenuDeleteEnginesLocation",
                    enginesTableFrame.enginesTableModel.locationName);
            putValue(NAME, actionName);
        }
        _enginesTableFrame = enginesTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (_enginesTableFrame.enginesTableModel.trackName == null &&
                _enginesTableFrame.enginesTableModel.locationName == null) {
            if (JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureDelete"),
                    Bundle.getMessage("engineDeleteAll"),
                    JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
                log.debug("removing all engines from roster");
                InstanceManager.getDefault(EngineManager.class).deleteAll();
            }
        } else {
            // delete all cars on track or location
            String message = Bundle.getMessage("engineDeleteEnginesTrack",
                    _enginesTableFrame.enginesTableModel.trackName);
            if (_enginesTableFrame.enginesTableModel.trackName == null) {
                message = Bundle.getMessage("engineDeleteEnginesLocation",
                        _enginesTableFrame.enginesTableModel.locationName);
            }
            if (JmriJOptionPane.showConfirmDialog(null, message,
                    Bundle.getMessage("engineDeleteAll"),
                    JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
                for (Engine engine : _enginesTableFrame.enginesTableModel.getSelectedEngineList()) {
                    InstanceManager.getDefault(EngineManager.class).deregister(engine);
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteEngineRosterAction.class);
}
