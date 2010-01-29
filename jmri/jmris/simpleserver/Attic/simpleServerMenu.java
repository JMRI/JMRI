/**
 * simpleServerMenu.java
 */

package jmri.jmris.simpleserver;

import javax.swing.*;
import java.util.*;

/**
 * Create a "JMRI Simple Server" menu containing the Server interface to the JMRI 
 * system-independent tools
 *
 * @author	Paul Bender   Copyright 2009
 * @version     $Revision: 1.1 $
 */
public class simpleServerMenu extends JMenu {
    public simpleServerMenu(String name) {
        this();
        setText(name);
    }

    public simpleServerMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.simpleserver.simpleServerBundle");

        setText(rb.getString("MenuServer"));
	add(new jmri.jmris.simpleserver.simpleServerAction(rb.getString("MenuItemStartServer")));

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(simpleServerMenu.class.getName());
}


