// DecoderProConfigAction.java

/** 
 * 
 *
 * Description:		Swing action to create DecoderProConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: DecoderProgConfigAction.java,v 1.2 2001-12-05 23:31:15 jacobsen Exp $
 */

package jmri.apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class DecoderProConfigAction 			extends AbstractAction {

	public DecoderProConfigAction(String s) { 
		super(s);
		// see if the file can be read
		try {
			DecoderProConfigFile file = new DecoderProConfigFile();
			file.readFile(DecoderProConfigFile.defaultConfigFilename());
			log.debug("configuration file located and read");
			frame = new DecoderProConfigFrame("Preferences");
			frame.configure(file);
			log.debug("configuration complete");
		} catch (Exception e) {
			// did not succeed, have to pop now
			log.debug("configuration file could not be located and read");
			log.debug("error was "+e);
			// create an unconfigured Frame
			frame = new DecoderProConfigFrame("Preferences");
			frame.show();	
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		frame.show();		
	}

	DecoderProConfigFrame frame = null;
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DecoderProConfigAction.class.getName());

}


/* @(#)eDcoderProConfigFrame.java */
