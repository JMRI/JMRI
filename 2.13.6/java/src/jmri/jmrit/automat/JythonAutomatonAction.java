// JythonAutomatonAction.java

package jmri.jmrit.automat;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.io.*;
import javax.swing.*;

/**
 * Swing action to create and register a
 * JythonAutomaton object
 *
 * @author	    Bob Jacobsen    Copyright (C) 2003, 2007
 * @version         $Revision$
 */
public class JythonAutomatonAction extends AbstractAction {

    public JythonAutomatonAction(String s, JPanel who) {
        super(s);
        _who = who;
    }

    JPanel _who;

    public void actionPerformed(ActionEvent e) {
        // create a SampleAutomaton
        JFileChooser fci = jmri.jmrit.XmlFile.userFileChooser("Python script files", "py");
        fci.setDialogTitle("Find desired script file");
        int retVal = fci.showOpenDialog(_who);
        // handle selection or cancel
        if (retVal == JFileChooser.APPROVE_OPTION) {
            File file = fci.getSelectedFile();
            // create an object to handle script and run
            (new JythonAutomaton(file.toString())).start();
        }
    }
}

/* @(#)JythonAutomatonAction.java */
