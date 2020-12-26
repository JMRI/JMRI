package jmri.jmrix.powerline.swing.packetgen;

import java.awt.event.ActionEvent;
import jmri.InstanceManager;
import jmri.jmrix.powerline.SerialSystemConnectionMemo;
import jmri.jmrix.powerline.SerialTrafficController;
import jmri.jmrix.powerline.swing.PowerlineSystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialPacketGenFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007, 2008 Converted to multiple
 * connection
 * @author kcameron Copyright (C) 2011
 */
public class SerialPacketGenAction extends PowerlineSystemConnectionAction {

    public SerialPacketGenAction(String s, SerialTrafficController tc) {
        super(s, tc.getAdapterMemo());
        this.tc = tc;
    }

    public SerialPacketGenAction(SerialTrafficController tc) {
        this(Bundle.getMessage("SendPacketTitle"), tc);
        this.tc = tc;
    }

    public SerialPacketGenAction() {
        this(InstanceManager.getDefault(SerialSystemConnectionMemo.class).getTrafficController());
    }

    SerialTrafficController tc = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        SerialPacketGenFrame f = new SerialPacketGenFrame(tc);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: ", ex);
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class);

}
