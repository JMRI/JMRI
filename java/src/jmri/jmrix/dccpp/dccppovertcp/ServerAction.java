package jmri.jmrix.dccpp.dccppovertcp;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;

/**
 * Implementation of the DCCppOverTcp LbServer Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 * @author Mark Underwood Copyright (C) 2015
 */
public class ServerAction
        extends AbstractAction {

    public ServerAction(String s) {
        super(s);
        // Get a server instance to cause the config to be read and the server
        // started if necessary
        InstanceManager.getDefault(Server.class);
    }

    public ServerAction() {
        this("DCC++OverTcp Server");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ServerFrame f = InstanceManager.getDefault(ServerFrame.class);
        f.setVisible(true);
    }

}
