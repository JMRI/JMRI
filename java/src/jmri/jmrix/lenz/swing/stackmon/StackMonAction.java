// StackMonAction.java
package jmri.jmrix.lenz.swing.stackmon;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a StackMonFrame object
 *
 * @author	Paul Bender Copyright (C) 2005
 * @version $Revision$
 */
public class StackMonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -6608133121577126292L;
    private jmri.jmrix.lenz.XNetSystemConnectionMemo _memo = null;

    public StackMonAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s);
        _memo = memo;
    }

    public StackMonAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this("Stack Monitor", memo);
    }

    public void actionPerformed(ActionEvent e) {

        // create a StackMonFrame
        StackMonFrame f = new StackMonFrame(_memo);
        f.setVisible(true);

    }
}


/* @(#)SlotMonAction.java */
