// DecoderProConfigAction.java

/**
 *
 *
 * Description:		Swing action to create DecoderProConfigFrame
 *
 * @author			Bob Jacobsen    Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */

package apps.DecoderPro;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import apps.AbstractConfigFrame;
import apps.AbstractConfigFile;

public class DecoderProConfigAction 			extends apps.AbstractConfigAction {

	protected AbstractConfigFile readFile(String name)
                throws org.jdom.JDOMException, java.io.FileNotFoundException {
		DecoderProConfigFile file = new DecoderProConfigFile();
        if (name!=null) {
            file.readFile(name);
        } else {
            file.readFile(file.defaultConfigFilename());
        }
        return file;
	}

	protected AbstractConfigFrame newFrame(String name){
		return new DecoderProConfigFrame(name);
	}

	public DecoderProConfigAction(String s) {
		super(s);
	}

}

