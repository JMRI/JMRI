package jmri.jmrit.jython;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import jmri.util.JmriJFrame;

/**
 * This Action runs creates an InputWindow for sending input to the global
 * jython interpreter
 *
 * @author Bob Jacobsen Copyright (C) 2004
 */
public class InputWindowAction extends AbstractAction {

    /**
     * Constructor just initializes parent class.
     *
     * @param name Action name
     */
    public InputWindowAction(String name) {
        super(name);
    }

    public InputWindowAction() {
        super("Script Input Window");
    }

    /**
     * Invoking this action via an event triggers display of a file dialog. If a
     * file is selected, it's then invoked as a script.
     *
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        f = new JmriJFrame(Bundle.getMessage("TitleInputFrame"));
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), javax.swing.BoxLayout.Y_AXIS));
        f.getContentPane().add(new InputWindow());

        f.pack();
        f.setVisible(true);
    }

    public JFrame getFrame() {
        return f;
    }

    JFrame f;
}
