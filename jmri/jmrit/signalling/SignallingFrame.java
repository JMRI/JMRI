// StatusFrame.java

package jmri.jmrit.signalling;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import java.util.ResourceBundle;

/**
 * Frame for ECoS status
 * @author	Bob Jacobsen   Copyright (C) 2008
 * @version $Revision: 1.1 $
*/
public class SignallingFrame extends jmri.util.JmriJFrame {

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.signalling.signallingBundle");

    public SignallingFrame() {
        super(false, true);
    }
    
    JButton sendButton;
    SignallingPanel sigPanel;
    protected boolean reuseFrameSavedSized = false;

    public void initComponents(jmri.SignalMast source, jmri.SignalMast dest) throws Exception {
        // the following code sets the frame's initial state
        if ((source==null) ||  (dest==null))
            sigPanel = new SignallingPanel(this);
        else
            sigPanel = new SignallingPanel(source, dest, this);
        
        setTitle(rb.getString("SignallingPairs"));
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        getContentPane().add(sigPanel);

		addHelpMenu("package.jmri.jmrit.signalling.SignallingFrame", true);

        // pack for display
        pack();
    }
}

