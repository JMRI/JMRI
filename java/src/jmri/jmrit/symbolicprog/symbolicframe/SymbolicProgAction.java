package jmri.jmrit.symbolicprog.symbolicframe;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SymbolicProg object
 *
 * @author	Bob Jacobsen Copyright (C) 2001
 */
public class SymbolicProgAction extends AbstractAction {
    public SymbolicProgAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {

        // create a SimpleProgFrame
        SymbolicProgFrame f = new SymbolicProgFrame();
        f.setVisible(true);

    }
}
