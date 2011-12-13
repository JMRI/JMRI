// SampleAutomaton2Action.java

package jmri.jmrit.automat;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SampleAutomaton2 object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2003
 * @version         $Revision$
 */
public class SampleAutomaton2Action extends AbstractAction {

    public SampleAutomaton2Action(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton2
        AbstractAutomaton f = new SampleAutomaton2();
        f.start();
    }
}

/* @(#)SampleAutomaton2Action.java */
