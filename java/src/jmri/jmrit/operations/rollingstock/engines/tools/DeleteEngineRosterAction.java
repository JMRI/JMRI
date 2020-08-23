package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;

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
        if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureDelete"),
                Bundle.getMessage("engineDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            log.debug("removing all engines from roster");
            InstanceManager.getDefault(EngineManager.class).deleteAll();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DeleteEngineRosterAction.class);
}
