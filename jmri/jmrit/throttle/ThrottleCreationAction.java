package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version     $Revision: 1.18 $
 */
public class ThrottleCreationAction extends AbstractAction {

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
}
