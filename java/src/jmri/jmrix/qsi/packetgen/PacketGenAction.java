package jmri.jmrix.qsi.packetgen;

import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import jmri.jmrix.qsi.swing.QsiSystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PacketGenFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2008
 */
public class PacketGenAction extends QsiSystemConnectionAction {

    public PacketGenAction(String s, QsiSystemConnectionMemo memo) {
        super(s, memo);
    }

    public PacketGenAction(QsiSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemSendCommand"), memo);
    }

    public PacketGenAction() {
        this(InstanceManager.getDefault(QsiSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PacketGenFrame f = new PacketGenFrame(getSystemConnectionMemo());
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class);

}
