package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;

public class ThrottlesListAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottlesListAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public ThrottlesListAction() {
        this("Throttles list");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesList();
    }
}
