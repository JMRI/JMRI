// ServerAction.java
package jmri.jmrix.loconet.loconetovertcp;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Implementation of the LocoNetOverTcp LbServer Server Protocol
 *
 * @author Alex Shepherd Copyright (C) 2006
 * @version	$Revision$
 */
public class ServerAction
        extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -7774780042559216689L;

    public ServerAction(String s) {
        super(s);
        // Get a server instance to cause the config to be read and the server
        // started if necessary
        Server.getInstance();
    }

    public ServerAction() {
        this("LocoNetOverTcp Server");
        // Get a server instance to cause the config to be read and the server
        // started if necessary
        Server.getInstance();
    }

    public void actionPerformed(ActionEvent e) {
        ServerFrame f = ServerFrame.getInstance();
        f.setVisible(true);
    }
}
