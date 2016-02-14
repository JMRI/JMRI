/**
 * z21MonAction.java
 *
 * Description:	Swing action to create and register a z21MonFrame object
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 * @author	Paul Bender Copyright (C) 2013
 * @version $Revision$
 */
package jmri.jmrix.roco.z21.swing.mon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class z21MonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -5378782642878718080L;
    private jmri.jmrix.roco.z21.z21SystemConnectionMemo _memo;

    public z21MonAction(String s, jmri.jmrix.roco.z21.z21SystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public z21MonAction(jmri.jmrix.roco.z21.z21SystemConnectionMemo memo) {
        this("Z21 Monitor", memo);
    }

    public z21MonAction(String s) {
        super(s);
        // If there is no system memo given, assume the system memo
        // is the first one in the instance list.
        _memo = jmri.InstanceManager.
                getList(jmri.jmrix.roco.z21.z21SystemConnectionMemo.class).get(0);
    }

    public z21MonAction() {
        this("Z21 Monitor");
    }

    public void actionPerformed(ActionEvent e) {
        // create a z21MonFrame
        z21MonFrame f = new z21MonFrame(_memo);
        try {
            f.initComponents();
        } catch (Exception ex) {
            log.warn("z21MonAction starting z21MonFrame: Exception: " + ex.toString());
        }
        f.setVisible(true);

    }

    private final static Logger log = LoggerFactory.getLogger(z21MonAction.class);

}


/* @(#)z21MonAction.java */
