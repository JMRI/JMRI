package jmri.jmrix.lenz.swing.packetgen;

import java.awt.event.ActionEvent;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an XpressNet PacketGenFrame
 * object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class PacketGenAction extends AbstractXPressNetAction {

    public PacketGenAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public PacketGenAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(Bundle.getMessage("PacketGenFrameTitle"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a PacketGenFrame
        PacketGenFrame f = new PacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}", ex.toString());
        }
        f.setVisible(true);

        // connect to the TrafficController
        f.connect(_memo.getXNetTrafficController());
    }

    private static final Logger log = LoggerFactory.getLogger(PacketGenAction.class);

}
