// DecoderProConfigAction.java

/**
 *
 *
 * Description:		Swing action to create DecoderProConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Id: DecoderProConfigAction.java,v 1.2 2002-02-28 17:24:16 jacobsen Exp $
 */

package apps.DecoderPro;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import apps.AbstractConfigFrame;
import apps.AbstractConfigFile;

public class DecoderProConfigAction 			extends apps.AbstractConfigAction {

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

