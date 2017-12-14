package jmri.jmrix.oaktree.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;

/**
 * Swing action to create and register a SerialPacketGenFrame
 * object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SerialPacketGenAction extends AbstractAction {

    private OakTreeSystemConnectionMemo _memo = null;

    public SerialPacketGenAction(String s,OakTreeSystemConnectionMemo memo) {
        super(s);
    }

    public SerialPacketGenAction(OakTreeSystemConnectionMemo memo) {
        this("Send Oak Tree message",memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SerialPacketGenFrame f = new SerialPacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class);
}

