// DeleteEngineRosterAction.java

package jmri.jmrit.operations.rollingstock.engines;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;


/**
 * This routine will remove all engines from the operation database.
 * 
 * @author Dan Boudreau Copyright (C) 2007
 * @version $Revision$
 */


public class DeleteEngineRosterAction extends AbstractAction {
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.rollingstock.engines.JmritOperationsEnginesBundle");
	
	EngineManager manager = EngineManager.instance();
	
	javax.swing.JLabel textLine = new javax.swing.JLabel();
	javax.swing.JLabel lineNumber = new javax.swing.JLabel();
	
    public DeleteEngineRosterAction(String actionName, Component frame) {
        super(actionName);
    }
	
	public void actionPerformed(ActionEvent ae) {
		if (JOptionPane.showConfirmDialog(null,
				rb.getString("engineSureDelete"), rb.getString("engineDeleteAll"),
				JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION){
			log.debug("removing all engines from roster");
			manager.deleteAll();
		}
	}




	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(DeleteEngineRosterAction.class.getName());
}
