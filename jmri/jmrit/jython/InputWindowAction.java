// InputWindowAction.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JTextArea;
import java.awt.Font;
import jmri.util.PythonInterp;

/**
 * This Action runs creates an InputWindow for sending input to the
 * global jython interpreter
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.1 $
 */
public class InputWindowAction extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     * @param name Action name
     */
    public InputWindowAction(String name) {
        super(name);
    }

    /**
     * Invoking this action via an event triggers
     * display of a file dialog. If a file is selected,
     * it's then invoked as a script.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        PythonInterp.getPythonInterpreter();

        java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");

        JFrame f = new JFrame(rb.getString("TitleInputFrame"));
        f.getContentPane().setLayout(new javax.swing.BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        f.getContentPane().add(new InputWindow());

        f.pack();
        f.show();

    }

}

/* @(#)InputWindowAction.java */
