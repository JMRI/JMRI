// LocoToolsConfigFrame.java

package apps.LocoTools;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * LocoToolsConfigFrame provides startup configuration, a GUI for setting
 * config/preferences, and read/write support.  Its specific to LocoTools
 * but should eventually be generalized.  Note that routine GUI config,
 * menu building, etc is done in other code.
 *<P>For now, we're implicitly assuming that configuration of these
 * things is _only_ done here, so that we don't have to track anything
 * else.  When asked to write the config, we just write the values
 * stored in local variables.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.2 $
 */
public class LocoToolsConfigFrame extends apps.AbstractConfigFrame {

	public LocoToolsConfigFrame(String name) {
		super(name);
	}

    /**
     * LocoTools only supports the LocoNet protocols, so the parent member is overloaded
     */
    public String[] availableProtocols() {
        return  new String[] {"(None selected)",
                                "LocoNet LocoBuffer","LocoNet MS100",
                                "LocoNet Server", "LocoNet HexFile"
                                };
    }
	/**
	 * Abstract method to save the data
	 */
	public void saveContents() {
		LocoToolsConfigFile f = new LocoToolsConfigFile();
		f.makeBackupFile(f.defaultConfigFilename());
		f.writeFile(f.defaultConfigFilename(), this);
	}


	// initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoToolsConfigFrame.class.getName());

}
