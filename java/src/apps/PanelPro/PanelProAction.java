package apps.PanelPro;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Create a new PanelPro start window.
 *
 * @author Bob Jacobsen (C) 2014
 */
public class PanelProAction extends JmriAbstractAction {

    public PanelProAction(String s, WindowInterface wi) {
        super(s, wi);
    }

    public PanelProAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public PanelProAction(String s) {
        super(s);
    }

    public PanelProAction() {
        this("PanelPro");
    }

    apps.AppsLaunchFrame frame = null;

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        if (frame == null) {
            frame = new apps.AppsLaunchFrame(new PanelProPane(), "PanelPro");
        }
        frame.setVisible(true);
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

}
