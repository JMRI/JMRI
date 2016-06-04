package jmri.jmrix.secsi.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialPacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007, 2008
 * @version	$Revision$
 */
public class SerialPacketGenAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5258440487857635217L;

    public SerialPacketGenAction(String s) {
        super(s);
    }

    public SerialPacketGenAction() {
        this("Send SECSI message");
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
