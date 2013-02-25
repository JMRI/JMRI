// DeleteCarRosterAction.java

package jmri.jmrit.operations.rollingstock.cars;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.Component;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

/**
 * This routine will remove all cars from the operation database.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision$
 */

public class DeleteCarRosterAction extends AbstractAction {

	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();

	public DeleteCarRosterAction(String actionName, Component frame) {
		super(actionName);
	}

	public void actionPerformed(ActionEvent ae) {
		if (JOptionPane.showConfirmDialog(null, Bundle.getMessage("carSureDelete"),
				Bundle.getMessage("carDeleteAll"), JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
			log.debug("removing all cars from roster");
			CarManager.instance().deleteAll();
		}
	}

	static Logger log = LoggerFactory
			.getLogger(DeleteCarRosterAction.class.getName());
}
