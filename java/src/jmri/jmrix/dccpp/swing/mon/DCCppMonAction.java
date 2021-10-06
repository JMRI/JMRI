package jmri.jmrix.dccpp.swing.mon;

import java.awt.event.ActionEvent;
import javax.swing.Action;
import jmri.InstanceManager;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.swing.DCCppSystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a DCCppMonFrame object
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author mstevetodd Copyright (C) 2021
 */
public class DCCppMonAction extends DCCppSystemConnectionAction {

    public DCCppMonAction(String s, DCCppSystemConnectionMemo memo) {
        super(s, memo);
    }

    public DCCppMonAction(DCCppSystemConnectionMemo memo) {
//        this(Bundle.getMessage("DCCppMonFrameTitle"), memo);
        this("DCC++ Traffic Monitor", memo);
    }

    public DCCppMonAction() {
        this(InstanceManager.getNullableDefault(DCCppSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        DCCppSystemConnectionMemo memo = getSystemConnectionMemo();
        if (memo != null) {
            DCCppMonFrame f = new DCCppMonFrame(memo);
            try {
                f.initComponents();
            } catch (Exception ex) {
                log.warn("Exception starting DCCppMonFrame", ex);
            }
            f.setVisible(true);
        } else {
            log.error("Not performing action {} because there is no connection", getValue(Action.NAME));
        }
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppMonAction.class);

}
