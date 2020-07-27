package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;

/**
 * This routine will reset the move count for all engines in the operation
 * database.
 *
 * @author Dan Boudreau Copyright (C) 2012
 */
public class ResetEngineMovesAction extends AbstractAction {

    public ResetEngineMovesAction() {
        super(Bundle.getMessage("MenuItemResetMoves"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureResetMoves"),
                Bundle.getMessage("engineResetMovesAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            log.debug("Reset moves for all engines in roster");
            InstanceManager.getDefault(EngineManager.class).resetMoves();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ResetEngineMovesAction.class);
}
