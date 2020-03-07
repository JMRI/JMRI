package jmri.jmrix.roco.z21.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a z21 PacketGenFrame object.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 */
public class PacketGenAction extends AbstractAction {

    jmri.jmrix.roco.z21.Z21SystemConnectionMemo _memo = null;

    public PacketGenAction(String s, jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        this(Bundle.getMessage("SendZ21MessageTitle"), memo);
    }

    public PacketGenAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.roco.z21.Z21SystemConnectionMemo.class).get(0);
    }

    public PacketGenAction() {
        this(Bundle.getMessage("SendZ21MessageTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a PacketGenFrame
        PacketGenFrame f = new PacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}",ex,ex);
        }
        f.setVisible(true);

        // connect to the TrafficController
        f.connect(_memo.getTrafficController());
    }

    private static final Logger log = LoggerFactory.getLogger(PacketGenAction.class);

}
