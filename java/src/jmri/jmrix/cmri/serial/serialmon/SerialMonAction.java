package jmri.jmrix.cmri.serial.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.swing.CMRISystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a SerialMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class SerialMonAction extends CMRISystemConnectionAction {

    public SerialMonAction(String s, CMRISystemConnectionMemo memo) {
        super(s, memo);
    }

    public SerialMonAction(CMRISystemConnectionMemo memo) {
        this(Bundle.getMessage("SerialCommandMonTitle"), memo);
    }

    public SerialMonAction() {
        this(InstanceManager.getNullableDefault(CMRISystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CMRISystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            SerialMonFrame f = new SerialMonFrame(memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("Exception starting SerialMonFrame", ex);
            }
            f.setVisible(true);
        } else {
            log.error("Not performing action {} because there is no connection", getValue(Action.NAME));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class);

}
