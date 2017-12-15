package jmri.jmrix.secsi.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Swing action to create and register a SerialPacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2007, 2008
  */
public class SerialPacketGenAction extends AbstractAction {

    private SecsiSystemConnectionMemo memo = null;

    public SerialPacketGenAction(String s,SecsiSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public SerialPacketGenAction(SecsiSystemConnectionMemo _memo) {
        this("Send SECSI message",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SerialPacketGenFrame f = new SerialPacketGenFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class);
}
