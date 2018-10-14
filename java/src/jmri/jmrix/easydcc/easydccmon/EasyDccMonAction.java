package jmri.jmrix.easydcc.easydccmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.jmrix.easydcc.EasyDccSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register an EasyDccMonFrame object.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 */
public class EasyDccMonAction extends AbstractAction {

    private EasyDccSystemConnectionMemo _memo = null;

    public EasyDccMonAction(String s, EasyDccSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public EasyDccMonAction() {
        this(Bundle.getMessage("MonitorXTitle", "EasyDCC"), jmri.InstanceManager.getDefault(EasyDccSystemConnectionMemo.class));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create an EasyDccMonFrame
        EasyDccMonFrame f = new EasyDccMonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("EasyDccMonAction starting EasyDccMonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccMonAction.class);

}
