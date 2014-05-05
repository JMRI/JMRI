package apps.PanelPro;

import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;
import jmri.beans.Beans;

/**
 * Create a new PanelPro start window
 *
 * @author		Bob Jacobsen   (C) 2014
 * @version     $Revision$
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
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
        if (frame == null) frame = new apps.AppsLaunchFrame(new PanelProPane(), "PanelPro");
        frame.setVisible(true);
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
