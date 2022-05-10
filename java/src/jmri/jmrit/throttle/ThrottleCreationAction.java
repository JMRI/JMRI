package jmri.jmrit.throttle;

import java.awt.event.ActionEvent;

import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.JMenu;

import jmri.InstanceManager;
import jmri.ThrottleManager;
import jmri.beans.BeanUtil;
import jmri.jmrit.roster.rostergroup.RosterGroupSelector;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.util.swing.JmriAbstractAction;
import jmri.util.swing.WindowInterface;

/**
 * Create a new throttle.
 *
 * @author Glen Oberhauser
 */
public class ThrottleCreationAction extends JmriAbstractAction {

    private final ConnectionConfig connectionConfig;

    public ThrottleCreationAction(String s, WindowInterface wi) {
        super(s, wi);
        connectionConfig = null;
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    public ThrottleCreationAction(String s, Icon i, WindowInterface wi) {
        super(s, i, wi);
        connectionConfig = null;
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(ThrottleManager.class) == null) {
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
        connectionConfig = null;
        // disable the ourselves if there is no throttle Manager
        if (jmri.InstanceManager.getNullableDefault(ThrottleManager.class) == null) {
            setEnabled(false);
        }
    }

    /**
     * Constructor
     *
     * @param s Name for the action.
     * @param connectionConfig the connection config
     */
    public ThrottleCreationAction(String s, ConnectionConfig connectionConfig) {
        super(s);
        this.connectionConfig = connectionConfig;
        // disable the ourselves if there is no throttle Manager
        if ((connectionConfig == null)
                || !connectionConfig.getAdapter().getSystemConnectionMemo()
                        .provides(ThrottleManager.class)) {
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
        ThrottleFrame tf = InstanceManager.getDefault(ThrottleFrameManager.class).createThrottleFrame(connectionConfig);
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

        ConnectionConfigManager ccm = InstanceManager.getNullableDefault(ConnectionConfigManager.class);
        if (ccm == null) return;

        int numConnectionsWithThrottleManager = 0;

        for (ConnectionConfig c : ccm) {
            if (c.getAdapter().getSystemConnectionMemo().provides(ThrottleManager.class)) {
                ThrottleManager connectionThrottleManager =
                        c.getAdapter().getSystemConnectionMemo().get(ThrottleManager.class);
                if (connectionThrottleManager != null) numConnectionsWithThrottleManager++;
            }
        }

        if (numConnectionsWithThrottleManager > 1) {
            JMenu throttleConnectionMenu = new JMenu(Bundle.getMessage("MenuThrottlesForConnections"));

            ThrottleManager defaultThrottleManager = InstanceManager.getDefault(ThrottleManager.class);

            for (ConnectionConfig c : InstanceManager.getDefault(ConnectionConfigManager.class)) {

                ThrottleManager connectionThrottleManager = c.getAdapter().getSystemConnectionMemo().get(ThrottleManager.class);
                if (connectionThrottleManager != null) {
                    if (connectionThrottleManager == defaultThrottleManager) {
                        throttleConnectionMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(
                                Bundle.getMessage("MenuItemNewThrottleWithConnectionDefault", c.getConnectionName()),
                                c));
                    } else {
                        throttleConnectionMenu.add(new jmri.jmrit.throttle.ThrottleCreationAction(
                                Bundle.getMessage("MenuItemNewThrottleWithConnection", c.getConnectionName()),
                                c));
                    }
                }
            }

            throttleMenu.add(throttleConnectionMenu);
        }
    }
}
