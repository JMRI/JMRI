// LocoToolsConfigAction.java

/**
 *
 *
 * Description:		Swing action to create LocoToolsConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.1 $
 */

package apps.LocoTools;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import apps.AbstractConfigFrame;
import apps.AbstractConfigFile;

public class LocoToolsConfigAction 			extends apps.AbstractConfigAction {

	protected AbstractConfigFile readFile(String name)
                throws org.jdom.JDOMException, java.io.FileNotFoundException {
		LocoToolsConfigFile file = new LocoToolsConfigFile();
        if (name!=null) {
            log.debug("using file "+name);
            file.readFile(name);
        } else {
            log.debug("for default file, use "+file.defaultConfigFilename());
            file.readFile(file.defaultConfigFilename());
        }
		return file;
	}
	protected AbstractConfigFrame newFrame(String name){
		return new LocoToolsConfigFrame(name);
	}

    /**
     * Create an action object, reading configuration with the
     * default filename.
     */
	public LocoToolsConfigAction(String actionName) {
 		super(actionName);
	}

    /**
     * Create an action object, using a specific filename for
     * configuration information.
     */
	public LocoToolsConfigAction(String actionName, String fileName) {
 		super(actionName, fileName);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoToolsConfigAction.class.getName());
}

