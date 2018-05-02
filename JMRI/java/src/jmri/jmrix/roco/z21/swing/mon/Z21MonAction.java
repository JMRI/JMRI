package jmri.jmrix.roco.z21.swing.mon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Swing action to create and register a Z21MonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2013
 */
public class Z21MonAction extends AbstractAction {

    private jmri.jmrix.roco.z21.Z21SystemConnectionMemo _memo;

    public Z21MonAction(String s, jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public Z21MonAction(jmri.jmrix.roco.z21.Z21SystemConnectionMemo memo) {
        this(Bundle.getMessage("Z21MonitorTitle"), memo);
    }

    public Z21MonAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.roco.z21.Z21SystemConnectionMemo.class).get(0);
    }

    public Z21MonAction() {
        this(Bundle.getMessage("Z21MonitorTitle"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a Z21MonFrame
        Z21MonFrame f = new Z21MonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("Z21MonAction starting Z21MonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(Z21MonAction.class);

}
