// LoadLayoutAction.java

package jmri.configurexml;

import java.awt.event.*;
import java.io.*;

import javax.swing.*;

/**
 * Load the layout hardware config.
 * <P>
 * Note: This class is obsolete, and not being used.
 *<P>
 * @author			Bob Jacobsen   Copyright (C) 2002
 * @version			$Revision: 1.4 $
 * @see             jmri.jmrit.XmlFile
 */
public class LoadLayoutAction extends AbstractAction {

    public LoadLayoutAction(String s) {
        super(s);
    }
    
    public void actionPerformed(ActionEvent e) {
        File fp = new File("layout.temp.config.xml");
        LayoutConfigXML layout = new LayoutConfigXML();
        
        layout.readFile(fp);
    }
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadLayoutAction.class.getName());
    
}
