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
 * @version			$Revision: 1.3 $
 * @see             jmri.jmrit.XmlFile
 */
public class LoadLayoutAction extends AbstractAction {

    public LoadLayoutAction(String s) {
        super(s);
    }
    
    public void actionPerformed(ActionEvent e) {
        File fp = new File("layout.temp.config.xml");
        LayoutConfigXML layout = new LayoutConfigXML();
        
        // write it out
        //try {
        layout.readFile(fp);
        //} catch (java.io.IOException ex) {
        //	log.error("Error writing layout config file: "+ex.getMessage());
        //}
    }
    
    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LoadLayoutAction.class.getName());
    
}
