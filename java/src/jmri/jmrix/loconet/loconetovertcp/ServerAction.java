package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Implementation of the LocoNet over TCP Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
public class ServerAction
        extends AbstractAction {

    public ServerAction(String s) {
        super(s);
    }

    public ServerAction() {
        this(Bundle.getMessage("ServerAction"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Server.getDefault().enable();
        if (!GraphicsEnvironment.isHeadless()) {
            ServerFrame.getDefault().setVisible(true);
        }
    }
}
