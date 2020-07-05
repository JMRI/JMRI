package jmri.jmrix.loconet.loconetovertcp;

import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Implementation of the LocoNet over TCP Server Protocol.
 *
 * @author Alex Shepherd Copyright (C) 2006
 */
@API(status = EXPERIMENTAL)
public class LnTcpServerAction
        extends AbstractAction {

    public LnTcpServerAction(String s) {
        super(s);
    }

    public LnTcpServerAction() {
        this(Bundle.getMessage("ServerAction"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        LnTcpServer.getDefault().enable();
        if (!GraphicsEnvironment.isHeadless()) {
            LnTcpServerFrame.getDefault().setVisible(true);
        }
    }
}
