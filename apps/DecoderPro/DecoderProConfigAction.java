// DecoderProConfigAction.java

/** 
 * 
 *
 * Description:		Swing action to create DecoderProConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: DecoderProConfigAction.java,v 1.1 2002-02-20 07:33:46 jacobsen Exp $
 */

package jmri.apps;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import jmri.apps.AbstractConfigFrame;
import jmri.apps.AbstractConfigFile;

public class DecoderProConfigAction 			extends jmri.apps.AbstractConfigAction {

	protected AbstractConfigFile readFile() throws org.jdom.JDOMException, java.io.FileNotFoundException {
		DecoderProConfigFile file = new DecoderProConfigFile();
		file.readFile(file.defaultConfigFilename());
		return file;
	}
	protected AbstractConfigFrame newFrame(String name){
		return new DecoderProConfigFrame(name);
	}

	public DecoderProConfigAction(String s) { 
		super(s);
	}
		
}

