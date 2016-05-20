// ResetEngineMovesAction.java

package jmri.jmrit.operations.rollingstock.engines;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * This routine will reset the move count for all engines in the operation database.
 * 
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */

public class ResetEngineMovesAction extends AbstractAction {

	EngineManager manager = EngineManager.instance();

	public ResetEngineMovesAction(String actionName, Component frame) {
		super(actionName);
	}

	public void actionPerformed(ActionEvent ae) {
		if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("engineSureResetMoves"),
				Bundle.getMessage("engineResetMovesAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			log.debug("Reset moves for all engines in roster");
			manager.resetMoves();
		}
	}

	static Logger log = LoggerFactory
			.getLogger(ResetEngineMovesAction.class.getName());
}
