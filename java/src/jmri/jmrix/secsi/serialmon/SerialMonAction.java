package jmri.jmrix.secsi.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.secsi.SecsiSystemConnectionMemo;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2006, 2007, 2008
  */
public class SerialMonAction extends AbstractAction {

    private SecsiSystemConnectionMemo memo = null;

    public SerialMonAction(String s,SecsiSystemConnectionMemo _memo) {
        super(s);
        memo = _memo;
    }

    public SerialMonAction(SecsiSystemConnectionMemo _memo) {
        this("SECSI Monitor",_memo);
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
