// SampleAutomaton3Action.java

package jmri.jmrit.automat;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SampleAutomaton3 object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2003
 * @version         $Revision$
 */
public class SampleAutomaton3Action extends AbstractAction {

    public SampleAutomaton3Action(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton2
        AbstractAutomaton f = new SampleAutomaton3();
        f.start();
    }
}

/* @(#)SampleAutomaton3Action.java */
