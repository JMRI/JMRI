// JToolBarUtil.java

package jmri.util;

import javax.swing.*;
import java.io.File;
import org.jdom.*;
import java.awt.*;

/**
 * Common utility methods for working with JToolBars.
 * <P>
 * Chief among these is the loadToolBar method, for
 * creating a JToolBar from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.1 $
 */

public class JToolBarUtil {

    static public JToolBar loadToolBar(String filename) {
        Element root;
        
        try {
            root = new jmri.jmrit.XmlFile(){}.rootFromName(filename);
        } catch (Exception e) {
            log.error("Could not parse JMenu file \""+filename+"\" due to: "+e);
            return null;
        }
        
        JToolBar retval = new JToolBar(root.getChild("name").getText());
        
        for (Object item : root.getChildren("item")) {
            Element child = (Element)item;
            if (child.getChild("icon") != null) {
                retval.add(new JButton(new ImageIcon(child.getChild("icon").getText())));
            } else if (child.getChild("name") != null) {
                retval.add(new JButton(child.getChild("name").getText()));
            }
        }
        return retval;
    }
    
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JToolBarUtil.class.getName());
}