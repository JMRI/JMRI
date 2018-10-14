package jmri.jmrit.beantable.signalmast;

import javax.swing.BoxLayout;
import jmri.util.JmriJFrame;

/**
 * JFrame to create a new SignalMast
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class SignalMastRepeaterJFrame extends JmriJFrame {

    public SignalMastRepeaterJFrame() {
        super(Bundle.getMessage("TitleSignalMastRepeater"), false, true);

        addHelpMenu("package.jmri.jmrit.beantable.SignalMastRepeater", true);
        getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));

        add(sigMastPanel = new SignalMastRepeaterPanel());
        pack();
    }

    SignalMastRepeaterPanel sigMastPanel = null;

}
