package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.ThrottleManager;
import jmri.InstanceManager;

/**
 * Create a new throttle.
 *
 * @author			Glen Oberhauser
 * @version
 */
public class ThrottleCreationAction extends AbstractAction {

    /**
     * Constructor
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
    }

    /**
     * The action is performed. Create a new ThrottleFrame and
     * position it adequately on the screen.
     * @param e The event causing the action.
     */
    public void actionPerformed(ActionEvent e) {
        ThrottleFrame tf = new ThrottleFrame();
        tf.pack();
        tf.setVisible(true);
        ThrottleFrameManager manager = InstanceManager.throttleFrameManagerInstance();
        manager.notifyNewThrottleFrame(tf);

    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleCreationAction.class.getName());

}