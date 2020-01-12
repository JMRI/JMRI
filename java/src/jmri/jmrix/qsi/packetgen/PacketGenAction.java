package jmri.jmrix.qsi.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.qsi.QsiSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PacketGenFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2007, 2008
 */
public class PacketGenAction extends AbstractAction {

    private QsiSystemConnectionMemo _memo = null;

    public PacketGenAction(String s, QsiSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(QsiSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemSendCommand"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PacketGenFrame f = new PacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class);

}
