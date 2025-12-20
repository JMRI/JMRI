package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;

import jmri.InstanceManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.JmriPanel;

public class ThrottlesListAction extends JmriAbstractAction {

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

    // never invoked, because we overrode actionPerformed above
    @Override
    public JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
