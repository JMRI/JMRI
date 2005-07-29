// ConsistToolAction.java

package jmri.jmrit.consisttool;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * ConsistToolFrame object
 *
 * @author              Paul Bender Copyright (C) 2003
 * @version             $Revision: 1.4 $
 */
 public class ConsistToolAction extends AbstractAction {

    public ConsistToolAction(String s) {
	super(s);

	// disable ourself if there is no consist manager available
        if (jmri.InstanceManager.consistManagerInstance()==null) {
            setEnabled(false);
        }

    }

    public ConsistToolAction() { this("Consist Tool");}

    public void actionPerformed(ActionEvent e) {

		ConsistToolFrame f = new ConsistToolFrame();
		f.show();

	}
   static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ConsistToolAction.class.getName());
}


/* @(#)ConsistToolAction.java */
