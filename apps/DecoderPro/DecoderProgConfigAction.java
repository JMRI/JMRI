// DecoderProConfigAction.java

/** 
 * 
 *
 * Description:		Swing action to create DecoderProConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: DecoderProgConfigAction.java,v 1.3 2001-12-09 17:59:43 jacobsen Exp $
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
			log.debug("start configuration from file: "+DecoderProConfigFile.defaultConfigFilename());
			if ( !frame.configure(file)) {
				// show the config frame if it didn't work
				frame.show();
			}
			log.debug("configuration complete");
		} catch (Exception e) {
			// did not succeed, have to pop now
			log.info("configuration file could not be located and read");
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
