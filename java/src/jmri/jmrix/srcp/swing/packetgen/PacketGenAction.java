package jmri.jmrix.srcp.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.srcp.SRCPSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
public class PacketGenAction extends AbstractAction {

    private SRCPSystemConnectionMemo _memo = null;

    public PacketGenAction(String s,SRCPSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(SRCPSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemSendSRCPCommand"),memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PacketGenFrame f = new PacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class);
}
