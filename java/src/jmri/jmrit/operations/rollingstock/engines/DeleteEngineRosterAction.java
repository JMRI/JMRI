// DeleteEngineRosterAction.java
package jmri.jmrit.operations.rollingstock.engines;

import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will remove all engines from the operation database.
 *
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision$
 */
public class DeleteEngineRosterAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = 361439686459687524L;
    EngineManager manager = EngineManager.instance();

    public DeleteEngineRosterAction(String actionName, Component frame) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureDelete"),
                Bundle.getMessage("engineDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            log.debug("removing all engines from roster");
            manager.deleteAll();
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(DeleteEngineRosterAction.class.getName());
}
