package jmri.jmrix.easydcc.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an EasyDccPacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccPacketGenAction extends AbstractAction {

    private EasyDccSystemConnectionMemo _memo = null;

    public EasyDccPacketGenAction(String s, EasyDccSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public EasyDccPacketGenAction(EasyDccSystemConnectionMemo memo) {
        this(Bundle.getMessage("SendXCommandTitle", "EasyDCC"), memo);
    }

    public EasyDccPacketGenAction() {
        this(jmri.InstanceManager.getDefault(jmri.jmrix.easydcc.EasyDccSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        EasyDccPacketGenFrame f = new EasyDccPacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}", ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccPacketGenAction.class);

}
