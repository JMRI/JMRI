// SampleAutomatonAction.java

package jmri.jmrit.automat;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * PowerPanel object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2003
 * @version         $Revision: 1.1 $
 */
public class SampleAutomatonAction extends AbstractAction {

    public SampleAutomatonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton
        SampleAutomaton f = new SampleAutomaton();
        f.start();
    }
}

/* @(#)SampleAutomatonAction.java */
