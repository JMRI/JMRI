// SendPacketAction.java

package jmri.jmrit.sendpacket;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a SendPacketFrame object
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class SendPacketAction extends AbstractAction {

    public SendPacketAction(String s) { super(s);}

    public SendPacketAction() { this("Send DCC packet");}

    public void actionPerformed(ActionEvent e) {
        // create a SendPacketFrame
        SendPacketFrame f = new SendPacketFrame();
        try {
            f.initComponents();
        }
        catch (Exception ex) {
            log.error("Exception: "+ex.toString());
        }
        f.show();
    }
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SendPacketAction.class.getName());
}

/* @(#)LocoGenAction.java */
