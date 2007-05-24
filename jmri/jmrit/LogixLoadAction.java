// LogixLoadAction.java

package jmri.jmrit;

import jmri.InstanceManager;

import java.awt.event.*;
import java.util.*;

import javax.swing.*;

/**
 * Provide an action to allow Logixs to be loaded disabled when panel file is loaded
 *
 * @author	Dave Duchamp   Copyright (C) 2007
 * @version	$Revision: 1.1 $
 */
public class LogixLoadAction extends AbstractAction {

    public LogixLoadAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

	ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.JmritDebugBundle");
	
    JPanel _who;

    public void actionPerformed(ActionEvent e) {
		// Set option to force Logixs to be loaded disabled
		InstanceManager.logixManagerInstance().setLoadDisabled(true);
		log.error("Requested load Logixs diabled via Debug menu.");
		javax.swing.JOptionPane.showMessageDialog(_who,
				rb.getString("LogixDisabledMessage"), rb.getString("DebugOption"),
						javax.swing.JOptionPane.INFORMATION_MESSAGE);
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LogixLoadAction.class.getName());
}
