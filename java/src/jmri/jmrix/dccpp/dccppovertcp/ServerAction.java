// ServerAction.java
package jmri.jmrix.dccpp.dccppovertcp;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Implementation of the DCCppOverTcp LbServer Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 * @author Mark Underwood Copyright (C) 2015
 * @version	$Revision$
 */
public class ServerAction
        extends AbstractAction {

    public ServerAction(String s) {
        super(s);
        // Get a server instance to cause the config to be read and the server
        // started if necessary
        Server.getInstance();
    }

    public ServerAction() {
        this("DCC++OverTcp Server");
        // Get a server instance to cause the config to be read and the server
        // started if necessary
        Server.getInstance();
    }

    public void actionPerformed(ActionEvent e) {
        ServerFrame f = ServerFrame.getInstance();
        f.setVisible(true);
    }
}
