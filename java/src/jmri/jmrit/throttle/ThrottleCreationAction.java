package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;
import javax.swing.Icon;

/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version     $Revision$
 */
public class ThrottleCreationAction extends JmriAbstractAction {

    public ThrottleCreationAction(String s, WindowInterface wi) {
    	super(s, wi);
    }
     
 	public ThrottleCreationAction(String s, Icon i, WindowInterface wi) {
    	super(s, i, wi);
    }
    
    /**
     * Constructor
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
    // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance()==null) {
            setEnabled(false);
        }         
    }

    public ThrottleCreationAction() {
        this(ThrottleBundle.bundle().getString("MenuItemNewThrottle"));
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
    	ThrottleFrame tf = ThrottleFrameManager.instance().createThrottleFrame();
		tf.toFront();
    }
    
    // never invoked, because we overrode actionPerformed above
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
