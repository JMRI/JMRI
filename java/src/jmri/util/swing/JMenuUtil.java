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
 * @version $Revision$
 * @since 2.9.4
 */

public class JMenuUtil extends GuiUtilBase {

    static public JMenu[] loadMenu(File file, WindowInterface wi, Object context) {
        Element root = rootFromFile(file);

        int n = root.getChildren("node").size();
        JMenu[] retval = new JMenu[n];
        
        int i = 0;
        for (Object child : root.getChildren("node")) {
            JMenu menuItem = jMenuFromElement((Element)child, wi, context);
            retval[i++] = menuItem;
            if(((Element)child).getChild("mnemonic")!=null && menuItem!=null){
                menuItem.setMnemonic(convertStringToKeyEvent(((Element)child).getChild("mnemonic").getText()));
            }
        }
        return retval;
    }
    
    static JMenu jMenuFromElement(Element main, WindowInterface wi, Object context) {
        String name = "<none>";
        Element e = main.getChild("name");
        if (e != null) name = e.getText();
        JMenu menu = new JMenu(name);
        
        for (Object item : main.getChildren("node")) {
            JMenuItem menuItem = null;
            Element child = (Element) item;
            if (child.getChildren("node").size() == 0) {  // leaf
                if (child.getText().equals("separator"))
                    menu.addSeparator();
                else {
                    Action act = actionFromNode(child, wi, context);
                    menu.add(menuItem = new JMenuItem(act));
                }
            } else {
                menu.add(menuItem = jMenuFromElement(child, wi, context)); // not leaf
            }
            if(menuItem!=null && child.getChild("mnemonic")!=null){
               menuItem.setMnemonic(convertStringToKeyEvent(child.getChild("mnemonic").getText()));
            }
        }
        return menu;
    }
    
    static int convertStringToKeyEvent(String st){
    	char a = (st.toLowerCase()).charAt(0);
        int kcode = a - 32;
        return kcode;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(JMenuUtil.class.getName());
}