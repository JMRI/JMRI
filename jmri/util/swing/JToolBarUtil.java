// JToolBarUtil.java

package jmri.util.swing;

import javax.swing.*;
import java.io.File;
import org.jdom.*;
import java.awt.*;
import jmri.util.swing.*;

/**
 * Common utility methods for working with JToolBars.
 * <P>
 * Chief among these is the loadToolBar method, for
 * creating a JToolBar from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.1 $
 */

public class JToolBarUtil extends GuiUtilBase {

    static public JToolBar loadToolBar(String filename) {
        return loadToolBar(filename, null);
    }

    static public JToolBar loadToolBar(String filename, WindowInterface wi) {
        Element root;
        
        try {
            root = new jmri.jmrit.XmlFile(){}.rootFromName(filename);
        } catch (Exception e) {
            log.error("Could not parse JMenu file \""+filename+"\" due to: "+e);
            return null;
        }
        
        JToolBar retval = new JToolBar(root.getChild("name").getText());
        
        for (Object item : root.getChildren("node")) {
            Action act = actionFromNode((Element)item, wi);
            if (act == null) continue;
            if (act.getValue(javax.swing.Action.SMALL_ICON) != null) {
                // icon present, add explicitly
                JButton b = new JButton((javax.swing.Icon)act.getValue(javax.swing.Action.SMALL_ICON));
                b.setAction(act);
                retval.add(b);
            } else {
                retval.add(new JButton(act));
            }
        }
        return retval;
        
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JToolBarUtil.class.getName());
}