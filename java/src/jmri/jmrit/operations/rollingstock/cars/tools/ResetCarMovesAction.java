package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;

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
        if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("carSureResetMoves"),
                Bundle.getMessage("carResetMovesAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            log.debug("Reset moves for all cars in roster");
            InstanceManager.getDefault(CarManager.class).resetMoves();
        }
    }

    private final static Logger log = LoggerFactory.getLogger(ResetCarMovesAction.class);
}
