/**
 * SerialPacketGenAction.java
 *
 * Description:	Swing action to create and register a SerialPacketGenFrame
 * object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 * @version
 */
package jmri.jmrix.cmri.serial.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerialPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1235681553251130747L;

    public SerialPacketGenAction(String s) {
        super(s);
    }

    public SerialPacketGenAction() {
        this("Send C/MRI message");
    }

    public void actionPerformed(ActionEvent e) {
        SerialPacketGenFrame f = new SerialPacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class.getName());
}


/* @(#)SerialPacketGenAction.java */
