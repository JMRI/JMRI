package jmri.jmrix.sprog.packetgen;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SprogPacketGenFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SprogPacketGenAction extends AbstractAction {

    private SprogSystemConnectionMemo _memo;

    public SprogPacketGenAction(String s,SprogSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SprogPacketGenFrame f = new SprogPacketGenFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.error("Exception: " + ex.toString());
        }
        f.setVisible(true);
    }
    private final static Logger log = LoggerFactory.getLogger(SprogPacketGenAction.class);
}



