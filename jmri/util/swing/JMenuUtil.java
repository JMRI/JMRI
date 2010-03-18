// JMenuUtil.java

package jmri.util.swing;

import java.io.File;
import javax.swing.*;
import org.jdom.*;

/**
 * Common utility methods for working with JMenus.
 * <P>
 * Chief among these is the loadMenu method, for
 * creating a set of menus from an XML definition
 *
 * @author Bob Jacobsen  Copyright 2003, 2010
 * @version $Revision: 1.4 $
 * @since 2.9.4
 */

public class JMenuUtil extends GuiUtilBase {

    static public JMenu[] loadMenu(File file, WindowInterface wi, Object context) {
        Element root = rootFromFile(file);

        int n = root.getChildren("node").size();
        JMenu[] retval = new JMenu[n];
        
        int i = 0;
        for (Object child : root.getChildren("node")) {
            retval[i++] = jMenuFromElement((Element)child, wi, context);
        }
        return retval;
    }
    
    static JMenu jMenuFromElement(Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();
        JMenu menu = new JMenu(name);
        
        for (Object item : main.getChildren("node")) {
            Element child = (Element) item;
            if (child.getChildren("node").size() == 0) {  // leaf
                Action act = actionFromNode(child, wi, context);
                menu.add(new JMenuItem(act));
            } else {
                menu.add(jMenuFromElement(child, wi, context)); // not leaf
            }
        }
        return menu;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMenuUtil.class.getName());
}