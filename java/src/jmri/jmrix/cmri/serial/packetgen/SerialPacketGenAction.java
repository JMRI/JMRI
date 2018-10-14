package jmri.jmrix.cmri.serial.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialPacketGenFrame
 * object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SerialPacketGenAction extends AbstractAction {

    private CMRISystemConnectionMemo _memo = null;

    public SerialPacketGenAction(String s,CMRISystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SerialPacketGenAction(CMRISystemConnectionMemo memo) {
        this(Bundle.getMessage("SendCommandTitle"),memo);
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
