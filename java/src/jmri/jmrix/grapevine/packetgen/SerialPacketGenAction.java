package jmri.jmrix.grapevine.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.swing.GrapevineSystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialPacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2007
 */
public class SerialPacketGenAction extends GrapevineSystemConnectionAction {

    public SerialPacketGenAction(String s, GrapevineSystemConnectionMemo memo) {
        super(s, memo);
    }

    public SerialPacketGenAction(GrapevineSystemConnectionMemo memo) {
        this(Bundle.getMessage("SendXCommandTitle", Bundle.getMessage("MenuSystem")), memo);
    }

    public SerialPacketGenAction() {
        this(InstanceManager.getNullableDefault(GrapevineSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GrapevineSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
        SerialPacketGenFrame f = new SerialPacketGenFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}", ex.toString());
        }
        f.setVisible(true);
        } else {
            log.error("No connection, so not performing action {}", getValue(Action.NAME));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialPacketGenAction.class);

}
