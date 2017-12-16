package jmri.jmrix.grapevine.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007
 */
public class SerialMonAction extends AbstractAction {

    private GrapevineSystemConnectionMemo memo = null;

    public SerialMonAction(String s,GrapevineSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public SerialMonAction(GrapevineSystemConnectionMemo _memo) {
        this("Grapevine Tree monitor",_memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame(memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SerialMonAction starting SerialMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class);

}
