// JmriDemoConfigFrame.java

package apps.JmriDemo;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import org.jdom.Element;
import org.jdom.Attribute;

/**
 * JmriDemoConfigFrame provides startup configuration, a GUI for setting
 * config/preferences, and read/write support.  Its specific to JmriDemo
 * but should eventually be generalized.  Note that routine GUI config,
 * menu building, etc is done in other code.
 *<P>For now, we're implicitly assuming that configuration of these
 * things is _only_ done here, so that we don't have to track anything
 * else.  When asked to write the config, we just write the values
 * stored in local variables.
 *
 * @author			Bob Jacobsen   Copyright (C) 2001
 * @version			$Revision: 1.3 $
 */
public class JmriDemoConfigFrame extends apps.AbstractConfigFrame {
    
    public JmriDemoConfigFrame(String name) {
        super(name);
    }
    
    /**
     * Abstract method to save the data
     */
    public void saveContents() {
        JmriDemoConfigFile f = new JmriDemoConfigFile();
        f.makeBackupFile(f.defaultConfigFilename());
        f.writeFile(f.defaultConfigFilename(), this);
    }
    
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(JmriDemoConfigFrame.class.getName());
    
}
