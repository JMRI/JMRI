// JmriDemoConfigAction.java

/**
 *
 *
 * Description:		Swing action to create JmriDemoConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: JmriDemoConfigAction.java,v 1.2 2002-03-09 23:32:36 jacobsen Exp $
 */

package apps.JmriDemo;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import apps.AbstractConfigFrame;
import apps.AbstractConfigFile;

public class JmriDemoConfigAction 			extends apps.AbstractConfigAction {

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
	 *  really an error; record it for later
	 */
	protected void configFailed() {
        super.configFailed();
    }

	protected void readFailed(Exception e) {
        super.readFailed(e);
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriDemoConfigAction.class.getName());
}

