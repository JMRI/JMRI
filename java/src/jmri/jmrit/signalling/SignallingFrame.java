package jmri.jmrit.signalling;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;

/**
 * Provide a JFrame to display a table of Signal Mast Logic.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingFrame extends jmri.util.JmriJFrame {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    public SignallingFrame() {
        super(false, true);
    }

    JButton sendButton;
    SignallingPanel sigPanel;

    /**
     * Set the Signal Mast Logic frame's initial state.
     *
     * @param source The Signal Mast this SML is directly linked to
     * @param dest The Signal Mast this SML is looking at
     * @throws Exception when an error occurs during initialization
     */
    public void initComponents(jmri.SignalMast source, jmri.SignalMast dest) throws Exception {
        // the following code sets the frame's initial state
        sigPanel = new SignallingPanel(source, dest, this);

        setTitle(rb.getString("SignallingPairs"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(sigPanel);

        addHelpMenu("package.jmri.jmrit.signalling.AddEditSignallingLogic", true);

        // pack for display
        pack();
    }
}
