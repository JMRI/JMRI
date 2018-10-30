package jmri.jmrit.signalling;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import jmri.SignalMast;
import jmri.util.JmriJFrame;

/**
 * Provide a JFrame to display a pane to edit the Signal Mast Logic for one Signal Mast.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 * @author Egbert Broerse 2018
 */
public class SignallingFrame extends JmriJFrame {

    public SignallingFrame() {
        super(false, true);
    }

    JButton sendButton;
    SignallingPanel sigPanel;

    /**
     * Set the Signal Mast Logic frame's initial state.
     *
     * @see SignallingPanel
     * @param source The Signal Mast this SML is directly linked to
     * @param dest   The Signal Mast this SML is looking at
     */
    public void initComponents(SignalMast source, SignalMast dest) {
        // the following code sets the frame's initial state
        sigPanel = new SignallingPanel(source, dest, this);

        setTitle(Bundle.getMessage("SignallingPairs"));  // NOI18N
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(sigPanel); // panels are created in SignallingPanel()

        addHelpMenu("package.jmri.jmrit.signalling.AddEditSignallingLogic", true);  // NOI18N

        // pack for display
        pack();
    }

}
