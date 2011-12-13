// InputWindowAction.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import jmri.util.JmriJFrame;

import jmri.util.PythonInterp;

/**
 * This Action runs creates an InputWindow for sending input to the
 * global jython interpreter
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision$
 */
public class InputWindowAction extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     * @param name Action name
     */
    public InputWindowAction(String name) {
        super(name);
    }

    public InputWindowAction() {
        super("Script Input Window");
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

        f = new JmriJFrame(rb.getString("TitleInputFrame"));
        f.getContentPane().setLayout(new javax.swing.BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        f.getContentPane().add(new InputWindow());

        f.pack();
        f.setVisible(true);

    }

    public JFrame getFrame() { return f; }

    JFrame f;
}

/* @(#)InputWindowAction.java */
