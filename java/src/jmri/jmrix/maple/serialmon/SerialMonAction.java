package jmri.jmrix.maple.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.maple.MapleSystemConnectionMemo;

/**
 * Swing action to create and register a SerialMonFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008
 */
public class SerialMonAction extends AbstractAction {

    private MapleSystemConnectionMemo _memo = null;

    public SerialMonAction(String s, MapleSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SerialMonAction(MapleSystemConnectionMemo memo) {
        this("Maple monitor", memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SerialMonAction starting SerialMonFrame: Exception: {}", ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class);

}
