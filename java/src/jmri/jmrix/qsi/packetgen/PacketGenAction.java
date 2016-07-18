package jmri.jmrix.qsi.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;

/**
 * Swing action to create and register a PacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 */
public class PacketGenAction extends AbstractAction {

    private QsiSystemConnectionMemo _memo = null;

    public PacketGenAction(String s,QsiSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(QsiSystemConnectionMemo memo) {
        this(java.util.ResourceBundle.getBundle("jmri.jmrix.JmrixSystemsBundle")
                .getString("MenuItemSendCommand"),memo);
    }

    public void actionPerformed(ActionEvent e) {
        PacketGenFrame f = new PacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class.getName());
}
