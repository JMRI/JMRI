package jmri.jmrix.grapevine.serialmon;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.grapevine.GrapevineSystemConnectionMemo;
import jmri.jmrix.grapevine.swing.GrapevineSystemConnectionAction;

/**
 * Swing action to create and register a SerialMonFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2006, 2007
 */
public class SerialMonAction extends GrapevineSystemConnectionAction {

    public SerialMonAction(String s, GrapevineSystemConnectionMemo memo) {
        super(s, memo);
    }

    public SerialMonAction(GrapevineSystemConnectionMemo memo) {
        this(Bundle.getMessage("MonitorXTitle", "Grapevine"), memo);
    }

    public SerialMonAction() {
        this(InstanceManager.getNullableDefault(GrapevineSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        GrapevineSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            // create a SerialMonFrame
            SerialMonFrame f = new SerialMonFrame(memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("SerialMonAction starting SerialMonFrame: Exception: {}", ex.toString());
            }
            f.setVisible(true);
        } else {
            log.error("No connection to run {} with", getValue(Action.NAME));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SerialMonAction.class);

}
