package jmri.jmrit.signalling;

import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.JButton;

/**
 * Frame for Signal Logic Source Mast status.
 *
 * @author Kevin Dickerson Copyright (C) 2011
 */
public class SignallingSourceFrame extends jmri.util.JmriJFrame {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.SignallingBundle");

    public SignallingSourceFrame() {
        super(false, true);
    }

    JButton sendButton;
    SignallingSourcePanel sigPanel;
    // boolean inEditMode = false; // to warn and prevent opening more than 1 editing session
    // cannot determine finishing of Edit session (cf. SignalGroupTableAction#addPressed(e)

    public void initComponents(jmri.SignalMast source) throws Exception {
        // the following code sets the frame's initial state
        sigPanel = new SignallingSourcePanel(source);

        setTitle(rb.getString("SignallingPairs") + ": " + source.getDisplayName());
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(sigPanel);

        addHelpMenu("package.jmri.jmrit.signalling.SignallingSourceFrame", true);

        // pack for display
        pack();
    }
}
