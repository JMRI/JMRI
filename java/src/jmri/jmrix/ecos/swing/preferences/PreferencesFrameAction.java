// PreferencesFrameAction.java

package jmri.jmrix.ecos.swing.preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.jmrix.ecos.EcosSystemConnectionMemo;

/**
 * Swing action to create and register a 
 *       			PreferencesFrame object
 *
 * @author	    Kevin Dickerson    Copyright (C) 2009
 * @version		$Revision$	
 */

public class PreferencesFrameAction extends AbstractAction {

    public PreferencesFrameAction(String s, EcosSystemConnectionMemo memo) {
        super(s);
        adaptermemo = memo;
    }

    EcosSystemConnectionMemo adaptermemo;

    public void actionPerformed(ActionEvent e) {
		PreferencesFrame f = new PreferencesFrame();
		try {
			f.initComponents(adaptermemo);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			ex.printStackTrace();
			}
		f.setVisible(true);	
	}
   static Logger log = LoggerFactory.getLogger(PreferencesFrameAction.class.getName());
}


/* @(#)PreferencesFrameAction.java */
