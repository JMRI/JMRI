// JythonWindow.java

package jmri.jmrit.jython;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.JFrame;
import javax.swing.text.Document;
import java.awt.Font;

/**
 * This Action creates a JFrame displaying
 * the thread output log from the {@link RunJythonScript} class.
 *
 * @author	Bob Jacobsen    Copyright (C) 2004
 * @version     $Revision: 1.2 $
 */
public class JythonWindow extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     * @param name Action name
     */
    public JythonWindow(String name) {
        super(name);
    }

    /**
     * Invoking this action via an event triggers
     * display of a file dialog. If a file is selected,
     * it's then invoked as a script.
     * @param e
     */
    public void actionPerformed(ActionEvent e) {
        jmri.util.PythonInterp.getPythonInterpreter();

        java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("jmri.jmrit.jython.JythonBundle");

        JFrame f = new JFrame(rb.getString("TitleOutputFrame"));
        f.getContentPane().add(
            new javax.swing.JScrollPane(
                area = new javax.swing.JTextArea(jmri.util.PythonInterp.outputlog.getDocument(), null, 12, 50)
            ));

        // set a monospaced font
        int size = area.getFont().getSize();
        area.setFont(new Font("Monospaced", Font.PLAIN, size));

        f.pack();
        f.show();
    }

    javax.swing.JTextArea area;
}

/* @(#)JythonWindow.java */
