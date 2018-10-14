package jmri.jmrit.signalling;

import javax.swing.BoxLayout;
import javax.swing.JButton;

/**
 * Frame for Signal Logic Source Mast status.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingSourceFrame extends jmri.util.JmriJFrame {

    public SignallingSourceFrame() {
        super(false, true);
    }

    JButton sendButton;
    SignallingSourcePanel sigPanel;
    // boolean inEditMode = false; // to warn and prevent opening more than 1 editing session
    // cannot determine finishing of Edit session (cf. SignalGroupTableAction#addPressed(e)

    public void initComponents(jmri.SignalMast source) {
        // the following code sets the frame's initial state
        sigPanel = new SignallingSourcePanel(source);

        setTitle(Bundle.getMessage("SignallingPairs") + ": " + source.getDisplayName());  // NOI18N
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(sigPanel);

        addHelpMenu("package.jmri.jmrit.signalling.SignallingSourceFrame", true);  // NOI18N

        // pack for display
        pack();

        // setup window closing listener
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                // remove property change listeners
                if (sigPanel != null) {
                    sigPanel.dispose();
                }
            }
        });
    }
}
