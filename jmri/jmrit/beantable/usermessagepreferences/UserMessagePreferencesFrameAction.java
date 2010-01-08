// UserMessagePreferencesFrameAction.java

package jmri.jmrit.beantable.usermessagepreferences;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a 
 *       			PreferencesFrame object
 *
 * @author	    Kevin Dickerson    Copyright (C) 2009
 * @version		$Revision: 1.1 $	
 */

public class UserMessagePreferencesFrameAction extends AbstractAction {

	public UserMessagePreferencesFrameAction(String s) { super(s);}
	
    public void actionPerformed(ActionEvent e) {
		UserMessagePreferencesFrame f = new UserMessagePreferencesFrame();
		try {
			f.initComponents();
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			ex.printStackTrace();
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(UserMessagePreferencesFrameAction.class.getName());
}


/* @(#)PreferencesFrameAction.java */
