package jmri.jmrit.display;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;
import jmri.jmrit.throttle.ThrottleFrame;

/**
 * Create a new throttle.
 *
 * @author  Glen Oberhauser
 * @version $Revision: 1.2 $
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
        ThrottleFrame throttle = new ThrottleFrame();
        throttle.pack();
        throttle.show();

    }

    // initialize logging
    //static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(ThrottleCreationAction.class.getName());

}