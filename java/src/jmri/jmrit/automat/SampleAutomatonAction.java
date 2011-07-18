// SampleAutomatonAction.java

package jmri.jmrit.automat;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * SampleAutomaton object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2003
 * @version         $Revision$
 */
public class SampleAutomatonAction extends AbstractAction {

    public SampleAutomatonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton
        AbstractAutomaton f = new SampleAutomaton();
        f.start();
    }
}

/* @(#)SampleAutomatonAction.java */
