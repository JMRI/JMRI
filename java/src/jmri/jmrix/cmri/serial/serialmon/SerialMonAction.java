package jmri.jmrix.cmri.serial.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SerialMonAction extends AbstractAction {

    private CMRISystemConnectionMemo _memo = null;

    public SerialMonAction(String s,CMRISystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public SerialMonAction(CMRISystemConnectionMemo memo) {
        this("C/MRI monitor",memo);
    }

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

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class.getName());

}
