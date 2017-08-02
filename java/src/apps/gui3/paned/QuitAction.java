package apps.gui3.paned;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriPanel;
import jmri.util.swing.WindowInterface;

/**
 * Action to quit the program
 *
 * Ignores WindowInterface.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 */
public class QuitAction extends jmri.util.swing.JmriAbstractAction {

    public QuitAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public QuitAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).shutdown();
        } catch (Exception ex) {
            System.err.println("Continuing after error in handleQuit: " + ex); // can't count on logging here
        }
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public void dispose() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
