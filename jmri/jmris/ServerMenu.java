/**
 * ServerMenu.java
 */

package jmri.jmris;

import javax.swing.*;
import java.util.*;

/**
 * Create a "Server" menu containing the Server interface to the JMRI 
 * system-independent tools
 *
 * @author	Paul Bender   Copyright 2010
 * @version     $Revision: 1.1 $
 */
public class ServerMenu extends JMenu {
    public ServerMenu(String name) {
        this();
        setText(name);
    }

    public ServerMenu() {

        super();

        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmris.JmriServerBundle");

        setText(rb.getString("MenuServer"));
	add(new jmri.jmris.JmriServerAction(rb.getString("MenuItemStartServer")));

    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ServerMenu.class.getName());
}


