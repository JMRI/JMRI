// AbstractConfigAction.java

/** 
 * 
 *
 * Description:		Abstract base action to create a ConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: AbstractConfigAction.java,v 1.1 2002-02-20 07:33:45 jacobsen Exp $
 */

package jmri.apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

abstract public class AbstractConfigAction 			extends AbstractAction {

	abstract protected AbstractConfigFile readFile()  throws org.jdom.JDOMException, java.io.FileNotFoundException;
	abstract protected AbstractConfigFrame newFrame(String name);
	
	public AbstractConfigAction(String s) { 
		super(s);
		// see if the file can be read
		try {
			jmri.jmrit.XmlFile.ensurePrefsPresent(jmri.jmrit.XmlFile.prefsDir());
			AbstractConfigFile file = readFile();
			log.debug("configuration file located and read");
			frame = newFrame("Preferences");
			log.debug("start configuration from file: "+file.defaultConfigFilename());
			
			// try to configure
			boolean ok = false;
			try { ok = frame.configure(file); }
			  catch (java.lang.UnsatisfiedLinkError e) {
					// failed badly - tell about it
					log.error("unexpected error configuring application. Quit application, delete prefs/*Config.xml and try again\n"+e);
					// make sure config frame is presented
					ok = false;
			}
			if ( !ok ) {
				// config failed, handle that
				configFailed();
			}
			log.debug("configuration complete");
		} catch (Exception e) {
			readFailed(e);
		}
	}
	
    public void actionPerformed(ActionEvent e) {
		frame.show();		
	}

	/**
	 * What action should be taken if there's
	 * a failure while doing the configure?
	 * Default is to show the frame.
	 */
	protected void configFailed() {
		frame.show();
	}
	
	/**
	 * What action should be taken if there's
	 * a failure while doing the file read?
	 * Default is to show the frame.
	 */
	protected void readFailed(Exception e) {
		// did not succeed, have to pop now
		log.info("configuration file could not be located and read");
		log.debug("error was "+e);
		// create an unconfigured Frame
		frame = newFrame("Preferences");
		frame.show();	
	}

	public String getCurrentProtocolName() { return (frame != null ? frame.getCurrentProtocolName():null); }
	public String getCurrentPortName() { return (frame != null ? frame.getCurrentPortName():null); }

	AbstractConfigFrame frame = null;
	
	// initialize logging	
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(AbstractConfigAction.class.getName());

}

