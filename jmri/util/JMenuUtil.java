// JMenuUtil.java

package jmri.util;

import javax.swing.*;
import java.io.File;
import org.jdom.*;

/**
 * Common utility methods for working with JMenus.
 * <P>
 * Chief among these is the loadMenu method, for
 * creating a set of menus from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.1 $
 */

public class JMenuUtil {

    static public JMenu[] loadMenu(String filename) {
        Element root;
        
        try {
            root = new jmri.jmrit.XmlFile(){}.rootFromName(filename);
        } catch (Exception e) {
            log.error("Could not parse JMenu file \""+filename+"\" due to: "+e);
            return null;
        }
        int n = root.getChildren("menu").size();
        JMenu[] retval = new JMenu[n];
        
        int i = 0;
        for (Object child : root.getChildren("menu")) {
            retval[i++] = jMenuFromElement((Element)child);
        }
        return retval;
    }
    
    static JMenu jMenuFromElement(Element main) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();
        JMenu menu = new JMenu(name);
        
        for (Object item : main.getChildren()) {
            Element child = (Element) item;
            if (child.getName().equals("node")) {
                String n = child.getChild("name").getText();
                menu.add(new JMenuItem(n));
            } else if (child.getName().equals("menu")) {
                menu.add(jMenuFromElement(child));
            }
        }
        return menu;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMenuUtil.class.getName());
}