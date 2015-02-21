// SampleAutomaton2Action.java
package jmri.jmrit.automat;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SampleAutomaton2 object
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class SampleAutomaton2Action extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -3925216468128349295L;

    public SampleAutomaton2Action(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton2
        AbstractAutomaton f = new SampleAutomaton2();
        f.start();
    }
}

/* @(#)SampleAutomaton2Action.java */
