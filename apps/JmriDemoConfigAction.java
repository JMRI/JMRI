// JmriDemoConfigAction.java

/** 
 * 
 *
 * Description:		Swing action to create JmriDemoConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: JmriDemoConfigAction.java,v 1.1 2002-02-20 07:33:45 jacobsen Exp $
 */

package jmri.apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.apps.AbstractConfigFrame;
import jmri.apps.AbstractConfigFile;

public class JmriDemoConfigAction 			extends jmri.apps.AbstractConfigAction {

	protected AbstractConfigFile readFile() throws org.jdom.JDOMException, java.io.FileNotFoundException {
		JmriDemoConfigFile file = new JmriDemoConfigFile();
		file.readFile(file.defaultConfigFilename());
		return file;
	}
	protected AbstractConfigFrame newFrame(String name){
		return new JmriDemoConfigFrame(name);
	}

	public JmriDemoConfigAction(String s) { 
		super(s);
	}
	
	/** not finding a file or having a config fail isn't 
	 *  really an error
	 */
	protected void configFailed() {}
	protected void readFailed() {}
			
}

