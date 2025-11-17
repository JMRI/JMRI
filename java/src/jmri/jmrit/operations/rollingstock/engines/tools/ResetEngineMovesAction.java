package jmri.jmrit.operations.rollingstock.engines.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.gui.EnginesTableFrame;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will reset the move count for all engines in the operation
 * database.
 *
 * @author Dan Boudreau Copyright (C) 2012
 */
public class ResetEngineMovesAction extends AbstractAction {

    EnginesTableFrame _enginesTableFrame;

    public ResetEngineMovesAction(EnginesTableFrame enginesTableFrame) {
        super(Bundle.getMessage("MenuItemResetMoves"));
        _enginesTableFrame = enginesTableFrame;
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureResetMoves"),
                Bundle.getMessage("engineResetMovesAll"), JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
            log.debug("Reset moves for all engines in roster");
            InstanceManager.getDefault(EngineManager.class)
                    .resetMoves(_enginesTableFrame.enginesTableModel.getSelectedEngineList());
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResetEngineMovesAction.class);
}
