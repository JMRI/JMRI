package jmri.jmrix.roco.z21.swing.configtool;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a Z21ConfigFrame object
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class Z21ConfigAction extends AbstractAction {

    private jmri.jmrix.roco.z21.Z21SystemConnectionMemo _memo;

    public Z21ConfigAction(String s, jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public Z21ConfigAction(jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        this(Bundle.getMessage("Z21ConfigToolMenuItem"), memo);
    }

    public Z21ConfigAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.roco.z21.Z21SystemConnectionMemo.class).get(0);
    }

    public Z21ConfigAction() {
        this("Z21 Configuration Tool");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a Z21ConfigFrame
        Z21ConfigFrame f = new Z21ConfigFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("Z21ConfigAction starting Z21ConfigFrame: Exception: {}",ex);
        }
        f.setVisible(true);

    }

    private static final Logger log = LoggerFactory.getLogger(Z21ConfigAction.class);

}
