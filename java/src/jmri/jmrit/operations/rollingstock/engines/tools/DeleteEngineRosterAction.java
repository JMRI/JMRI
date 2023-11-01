package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will remove all engines from the operation database.
 *
 * @author Dan Boudreau Copyright (C) 2007
 */
public class DeleteEngineRosterAction extends AbstractAction {

    public DeleteEngineRosterAction() {
        super(Bundle.getMessage("MenuItemDelete"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureDelete"),
                Bundle.getMessage("engineDeleteAll"), JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
            log.debug("removing all engines from roster");
            InstanceManager.getDefault(EngineManager.class).deleteAll();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DeleteEngineRosterAction.class);
}
