package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import jmri.InstanceManager;

public class ThrottlesPreferencesAction extends AbstractAction {

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottlesPreferencesAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public ThrottlesPreferencesAction() {
        this("Throttles preferences");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InstanceManager.getDefault(ThrottleFrameManager.class).showThrottlesPreferences();
    }
}
