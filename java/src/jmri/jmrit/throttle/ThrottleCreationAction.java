package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.beans.BeanUtil;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Create a new throttle.
 *
 * @author Glen Oberhauser
 */
public class ThrottleCreationAction extends JmriAbstractAction {

    private final ThrottleManager throttleManager;

    public ThrottleCreationAction(String s, WindowInterface wi) {
        super(s, wi);
        throttleManager = jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
        // disable the ourselves if there is no throttle Manager
        if (throttleManager == null) {
            super.setEnabled(false);
        }
    }

    public ThrottleCreationAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        throttleManager = jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
        // disable the ourselves if there is no throttle Manager
        if (throttleManager == null) {
            super.setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
        throttleManager = jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
        // disable the ourselves if there is no throttle Manager
        if (throttleManager == null) {
            super.setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     * @param throttleManager the throttle manager
     */
    public ThrottleCreationAction(String s, ThrottleManager throttleManager) {
        super(s);
        this.throttleManager = throttleManager;
        // disable the ourselves if there is no throttle Manager
        if (throttleManager == null) {
            super.setEnabled(false);
        }
    }

    public ThrottleCreationAction() {
        this(Bundle.getMessage("MenuItemNewThrottle"));
    }

    /**
     * The action is performed. Create a new ThrottleFrame.
     *
     * @param e The event causing the action.
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        String group = null;
        if (BeanUtil.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            group = (String) BeanUtil.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame(throttleManager);
        tf.getAddressPanel().getRosterEntrySelector().setSelectedRosterGroup(group);
        tf.toFront();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
