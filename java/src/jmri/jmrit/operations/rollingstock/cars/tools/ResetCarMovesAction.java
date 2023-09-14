package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.util.swing.JmriJOptionPane;

/**
 * This routine will reset the move count for all cars in the operation
 * database.
 *
 * @author Dan Boudreau Copyright (C) 2012
 */
public class ResetCarMovesAction extends AbstractAction {

    public ResetCarMovesAction() {
        super(Bundle.getMessage("MenuItemResetMoves"));
    }

    @Override
    public void actionPerformed(ActionEvent ae) {
        if (JmriJOptionPane.showConfirmDialog(null, Bundle.getMessage("carSureResetMoves"),
                Bundle.getMessage("carResetMovesAll"), JmriJOptionPane.OK_CANCEL_OPTION) == JmriJOptionPane.OK_OPTION) {
            log.debug("Reset moves for all cars in roster");
            InstanceManager.getDefault(CarManager.class).resetMoves();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ResetCarMovesAction.class);
}
