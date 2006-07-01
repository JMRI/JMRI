// JythonAutomatonAction.java

package jmri.jmrit.automat;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Swing action to create and register a
 * JythonAutomaton object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2003
 * @version         $Revision: 1.1 $
 */
public class JythonAutomatonAction extends AbstractAction {

    public JythonAutomatonAction(String s) { super(s);}

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton
        AbstractAutomaton f = new JythonAutomaton();
        f.start();
    }
}

/* @(#)JythonAutomatonAction.java */
