package jmri.jmrix.jmriclient.swing.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a PacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2008
 */
@API(status = EXPERIMENTAL)
public class PacketGenAction extends AbstractAction {

    jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo _memo = null;

    public PacketGenAction(String s, jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public PacketGenAction(jmri.jmrix.jmriclient.JMRIClientSystemConnectionMemo memo) {
        this("Generate JMRI Client message", memo);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        PacketGenFrame f = new PacketGenFrame();
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: {}", ex.toString());
        }

        // connect to the traffic controller
        f.connect(_memo.getJMRIClientTrafficController());
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(PacketGenAction.class);
}



