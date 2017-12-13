package jmri.jmrix.grapevine.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;

/**
 * Swing action to create and register a SerialPacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 */
public class SerialPacketGenAction extends AbstractAction {

    private GrapevineSystemConnectionMemo memo = null;

    public SerialPacketGenAction(String s,GrapevineSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public SerialPacketGenAction(GrapevineSystemConnectionMemo _memo) {
        this("Send Grapevine message",_memo);
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
