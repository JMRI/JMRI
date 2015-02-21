package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;

public class ThrottlesPreferencesAction extends AbstractAction {

    /**
     *
     */
    private static final long serialVersionUID = -1272033268282072446L;

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottlesPreferencesAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.throttleManagerInstance() == null) {
            setEnabled(false);
        }
    }

    public ThrottlesPreferencesAction() {
        this("Throttles preferences");
    }

    public void actionPerformed(ActionEvent e) {
        jmri.jmrit.throttle.ThrottleFrameManager.instance().showThrottlesPreferences();
    }
}
