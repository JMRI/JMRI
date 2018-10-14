package jmri.jmrix.maple.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Swing action to create and register a SerialPacketGenFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class SerialPacketGenAction extends AbstractAction {

    private MapleSystemConnectionMemo _memo = null;

    public SerialPacketGenAction(String s, MapleSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SerialPacketGenAction(MapleSystemConnectionMemo  memo) {
        this(Bundle.getMessage("SendXCommandTitle", "Maple"), memo);
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
