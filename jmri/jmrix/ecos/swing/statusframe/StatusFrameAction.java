// StatusFrameAction.java

package jmri.jmrix.ecos.swing.statusframe;

import jmri.jmrix.ecos.*;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a 
 *       			StatusFrame object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2008
 * @version		$Revision: 1.4 $	
 */
@Deprecated
public class StatusFrameAction extends AbstractAction {

	public StatusFrameAction(String s, EcosSystemConnectionMemo memo ) {
        super(s);
        adaptermemo = memo;
    }

    EcosSystemConnectionMemo adaptermemo;

    public void actionPerformed(ActionEvent e) {
		StatusFrame f = new StatusFrame(adaptermemo);
		try {
			f.initComponents(adaptermemo);
			}
		catch (Exception ex) {
			log.error("Exception: "+ex.toString());
			ex.printStackTrace();
			}
		f.setVisible(true);	
	}
   static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(StatusFrameAction.class.getName());
}


/* @(#)StatusFrameAction.java */
