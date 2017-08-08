package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;
import javax.swing.Icon;
import jmri.InstanceManager;
import jmri.beans.Beans;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Create a new throttle.
 *
 * @author Glen Oberhauser
 */
public class ThrottleCreationAction extends JmriAbstractAction {

    public ThrottleCreationAction(String s, WindowInterface wi) {
        super(s, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public ThrottleCreationAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     */
    public ThrottleCreationAction(String s) {
        super(s);
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class) == null) {
            setEnabled(false);
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
        if (Beans.hasProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP)) {
            group = (String) Beans.getProperty(wi, RosterGroupSelector.SELECTED_ROSTER_GROUP);
        }
        ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame();
        tf.getAddressPanel().getRosterEntrySelector().setSelectedRosterGroup(group);
        tf.toFront();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }
}
