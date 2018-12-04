package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This routine will reset the move count for all engines in the operation
 * database.
 *
 * @author Dan Boudreau Copyright (C) 2012
 */
public class ResetEngineMovesAction extends AbstractAction {

    EngineManager manager = InstanceManager.getDefault(EngineManager.class);

    public ResetEngineMovesAction(String actionName) {
        super(actionName);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureResetMoves"),
                Bundle.getMessage("engineResetMovesAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            log.debug("Reset moves for all engines in roster");
            manager.resetMoves();
        }
    }

    private final static Logger log = LoggerFactory
            .getLogger(ResetEngineMovesAction.class);
}
