package jmri.jmrit.signalling;

import javax.swing.BoxLayout;
import javax.swing.JButton;

/**
 * Provide a JFrame to display a table of Signal Mast Logic.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingFrame extends jmri.util.JmriJFrame {

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
     */
    public void initComponents(jmri.SignalMast source, jmri.SignalMast dest) {
        // the following code sets the frame's initial state
        sigPanel = new SignallingPanel(source, dest, this);

        setTitle(Bundle.getMessage("SignallingPairs"));  // NOI18N
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(sigPanel);

        addHelpMenu("package.jmri.jmrit.signalling.AddEditSignallingLogic", true);  // NOI18N

        // pack for display
        pack();
    }
}
