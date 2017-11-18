package jmri.jmrix.tmcc.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.tmcc.TmccSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialPacketGenFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SerialPacketGenAction extends AbstractAction {

    private TmccSystemConnectionMemo _memo = null;

    public SerialPacketGenAction(String s, TmccSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SerialPacketGenAction(TmccSystemConnectionMemo memo) {
        this("Generate TMCC message", memo);
    }

    public SerialPacketGenAction() {
        this(Bundle.getMessage("SendCommandTitle"), jmri.InstanceManager.getDefault(TmccSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SerialPacketGenFrame f = new SerialPacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}", ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class);

}
