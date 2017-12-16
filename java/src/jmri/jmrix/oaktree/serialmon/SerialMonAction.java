package jmri.jmrix.oaktree.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.oaktree.OakTreeSystemConnectionMemo;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006
 */
public class SerialMonAction extends AbstractAction {

    private OakTreeSystemConnectionMemo _memo = null;

    public SerialMonAction(String s,OakTreeSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SerialMonAction(OakTreeSystemConnectionMemo memo) {
        this("Oak Tree monitor",memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a SerialMonFrame
        SerialMonFrame f = new SerialMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("SerialMonAction starting SerialMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class);

}
