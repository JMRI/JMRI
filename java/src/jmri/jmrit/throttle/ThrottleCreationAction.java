package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JMenu;

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
    private final String connectionName;

    public ThrottleCreationAction(String s, WindowInterface wi) {
        super(s, wi);
        throttleManager = jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
        connectionName = null;
        // disable the ourselves if there is no throttle Manager
        if (throttleManager == null) {
            super.setEnabled(false);
        }
    }

    public ThrottleCreationAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        throttleManager = jmri.InstanceManager.getNullableDefault(jmri.ThrottleManager.class);
        connectionName = null;
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
        connectionName = null;
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
     * @param connectionName the name of the connection
     */
    public ThrottleCreationAction(String s, ThrottleManager throttleManager, String connectionName) {
        super(s);
        this.throttleManager = throttleManager;
        this.connectionName = connectionName;
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
        ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame(throttleManager, connectionName);
        tf.getAddressPanel().getRosterEntrySelector().setSelectedRosterGroup(group);
        tf.toFront();
    }

    // never invoked, because we overrode actionPerformed above
    @Override
    public jmri.util.swing.JmriPanel makePanel() {
        throw new IllegalArgumentException("Should not be invoked");
    }

    public static void addNewThrottleItemsToThrottleMenu(@Nonnull JMenu throttleMenu) {

        throttleMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(Bundle.getMessage("MenuItemNewThrottle")));

        jmri.jmrix.ConnectionConfigManager ccm = InstanceManager.getNullableDefault(jmri.jmrix.ConnectionConfigManager.class);
        if (ccm == null) return;

        int numConnectionsWithThrottleManager = 0;

        for (jmri.jmrix.ConnectionConfig c : ccm) {
            jmri.ThrottleManager connectionThrottleManager = c.getAdapter().getSystemConnectionMemo().get(jmri.ThrottleManager.class);
            if (connectionThrottleManager != null) numConnectionsWithThrottleManager++;
        }

        if (numConnectionsWithThrottleManager > 1) {
            JMenu throttleConnectionMenu = new JMenu(Bundle.getMessage("MenuThrottlesForConnections"));

            jmri.ThrottleManager defaultThrottleManager = InstanceManager.getDefault(jmri.ThrottleManager.class);

            for (jmri.jmrix.ConnectionConfig c : InstanceManager.getDefault(jmri.jmrix.ConnectionConfigManager.class)) {

                jmri.ThrottleManager connectionThrottleManager = c.getAdapter().getSystemConnectionMemo().get(jmri.ThrottleManager.class);
                if (connectionThrottleManager != null) {
                    if (connectionThrottleManager == defaultThrottleManager) {
                        throttleConnectionMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(
                                Bundle.getMessage("MenuItemNewThrottleWithConnectionDefault", c.getConnectionName()),
                                connectionThrottleManager, c.getConnectionName()));
                    } else {
                        throttleConnectionMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(
                                Bundle.getMessage("MenuItemNewThrottleWithConnection", c.getConnectionName()),
                                connectionThrottleManager, c.getConnectionName()));
                    }
                }
            }

            throttleMenu.add(throttleConnectionMenu);
        }
    }
}
