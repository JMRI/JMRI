// SampleAutomatonAction.java
package jmri.jmrit.automat;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Swing action to create and register a SampleAutomaton object
 *
 * @author	Bob Jacobsen Copyright (C) 2003
 * @version $Revision$
 */
public class SampleAutomatonAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1426277051577869462L;

    public SampleAutomatonAction(String s) {
        super(s);
    }

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton
        AbstractAutomaton f = new SampleAutomaton();
        f.start();
    }
}

/* @(#)SampleAutomatonAction.java */
