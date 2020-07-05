package jmri.jmrix.lenz.swing.stackmon;

import java.awt.event.ActionEvent;
import jmri.jmrix.lenz.swing.AbstractXPressNetAction;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * Swing action to create and register a StackMonFrame object
 *
 * @author Paul Bender Copyright (C) 2005
 */
@API(status = EXPERIMENTAL)
public class StackMonAction extends AbstractXPressNetAction {

    public StackMonAction(String s, jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        super(s,memo);
    }

    public StackMonAction(jmri.jmrix.lenz.XNetSystemConnectionMemo memo) {
        this(Bundle.getMessage("MenuItemCSDatabaseManager"), memo);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // create a StackMonFrame
        StackMonFrame f = new StackMonFrame(_memo);
        f.setVisible(true);
    }

}
