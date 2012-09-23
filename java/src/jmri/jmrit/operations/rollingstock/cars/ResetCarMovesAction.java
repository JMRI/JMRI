// ResetCarMovesAction.java

package jmri.jmrit.operations.rollingstock.cars;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


/**
 * This routine will reset the move count for all cars in the operation database.
 * 
 * @author Dan Boudreau Copyright (C) 2012
 * @version $Revision: 17977 $
 */


public class ResetCarMovesAction extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.cars.JmritOperationsCarsBundle");
	
	CarManager manager = CarManager.instance();
	
    public ResetCarMovesAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		if (JOptionPane.showConfirmDialog(null,
				rb.getString("carSureResetMoves"), rb.getString("carResetMovesAll"),
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
			log.debug("Reset moves for all cars in roster");
			manager.resetMoves();
		}
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(ResetCarMovesAction.class.getName());
}
