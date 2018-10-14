package jmri.jmrix.acela.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an AcelaPacketGenFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 *
 * @author Bob Coleman, Copyright (C) 2007, 2008 Based on CMRI serial example,
 * modified to establish Acela support.
 */
public class AcelaPacketGenAction extends AbstractAction {

    private AcelaSystemConnectionMemo _memo = null;

    public AcelaPacketGenAction(String s, AcelaSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public AcelaPacketGenAction() {
        this(Bundle.getMessage("AcelaSendCommandTitle"),
                jmri.InstanceManager.getDefault(jmri.jmrix.acela.AcelaSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AcelaPacketGenFrame f = new AcelaPacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaPacketGenAction.class);

}
