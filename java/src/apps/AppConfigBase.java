package apps;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmri.Application;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.JmrixConfigPane;
import jmri.swing.ManagingPreferencesPanel;
import jmri.swing.PreferencesPanel;
import jmri.util.swing.JmriPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic configuration infrastructure, to be used by specific GUI
 * implementations
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2008, 2010
 * @author Matthew Harris copyright (c) 2009
 * @author Ken Cameron Copyright (C) 2011
 */
public class AppConfigBase extends JmriPanel {

    /**
     * All preferences panels handled, whether persisted or not.
     */
    protected HashMap<String, PreferencesPanel> preferencesPanels = new HashMap<>();

    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    private static final Logger log = LoggerFactory.getLogger(AppConfigBase.class);

    /**
     * Construct a configuration panel for inclusion in a preferences or
     * configuration dialog with default number of connections.
     */
    public AppConfigBase() {
    }

    /**
     * Detect duplicate connection types It depends on all connections have the
     * first word be the same if they share the same type. So LocoNet ... is a
     * fine example.
     * <p>
     * This also was broken when the names for systems were updated before JMRI
     * 2.9.4, so it should be revisited.
     *
     * @return true if OK, false if duplicates present.
     */
    private boolean checkDups() {
        Map<String, List<ConnectionConfig>> ports = new HashMap<>();
        ConnectionConfig[] connections = InstanceManager.getDefault(ConnectionConfigManager.class).getConnections();
        for (ConnectionConfig connection : connections) {
            if (!connection.getDisabled()) {
                String port = connection.getInfo();
                if (!port.equals(JmrixConfigPane.NONE)) {
                    if (!ports.containsKey(port)) {
                        List<ConnectionConfig> arg1 = new ArrayList<>();
                        arg1.add(connection);
                        ports.put(port, arg1);
                    } else {
                        ports.get(port).add(connection);
                    }
                }
            }
        }
        boolean ret = true;
        /* one or more dups or NONE, lets see if it is dups */
        for (Map.Entry<String, List<ConnectionConfig>> e : ports.entrySet()) {
            if (e.getValue().size() > 1) {
                /* dup port found */
                ret = false;
                StringBuilder nameB = new StringBuilder();
                for (int n = 0; n < e.getValue().size(); n++) {
                    nameB.append(e.getValue().get(n).getManufacturer());
                    nameB.append("|");
                }
                String instanceNames = new String(nameB);
                instanceNames = instanceNames.substring(0, instanceNames.lastIndexOf("|"));
                instanceNames = instanceNames.replaceAll("[|]", ", ");
                log.error("Duplicate ports found on: " + instanceNames + " for port: " + e.getKey());
            }
        }
        return ret;
    }

    /**
     * Checks to see if user selected a valid serial port
     *
     * @return true if okay
     */
    private boolean checkPortNames() {
        for (ConnectionConfig connection : InstanceManager.getDefault(ConnectionConfigManager.class).getConnections()) {
            String port = connection.getInfo();
            if (port.equals(JmrixConfigPane.NONE_SELECTED) || port.equals(JmrixConfigPane.NO_PORTS_FOUND)) {
                if (JOptionPane.YES_OPTION != JOptionPane.showConfirmDialog(
                        null,
                        MessageFormat.format(rb.getString("MessageSerialPortWarning"), new Object[]{port, connection.getConnectionName()}),
                        rb.getString("MessageSerialPortNotValid"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void dispose() {
        this.preferencesPanels.clear();
    }

    public void saveContents() {
        // remove old prefs that are registered in ConfigManager
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.removePrefItems();
        }
        // put the new GUI managedPreferences on the persistance list
        this.getPreferencesPanels().values().stream().forEach((panel) -> {
            this.registerWithConfigureManager(panel);
        });
        if (cm != null) {
            cm.storePrefs();
        }
    }

    private void registerWithConfigureManager(PreferencesPanel panel) {
        if (panel.isPersistant()) {
            ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
            if (cm != null) {
                cm.registerPref(panel);
            }
        }
        if (panel instanceof ManagingPreferencesPanel) {
            log.debug("Iterating over managed panels within {}/{}", panel.getPreferencesItemText(), panel.getTabbedPreferencesTitle());
            ((ManagingPreferencesPanel) panel).getPreferencesPanels().stream().forEach((managed) -> {
                log.debug("Registering {} with the ConfigureManager", managed.getClass().getName());
                this.registerWithConfigureManager(managed);
            });
        }
    }

    /**
     * Handle the Save button: Backup the file, write a new one, prompt for what
     * to do next. To do that, the last step is to present a dialog box
     * prompting the user to end the program, if required.
     *
     * @param restartRequired true if JMRI should prompt user to restart
     */
    public void savePressed(boolean restartRequired) {
        // true if port name OK
        if (!checkPortNames()) {
            return;
        }
        // true if there arn't any duplicates
        if (!checkDups()) {
            if (!(JOptionPane.showConfirmDialog(null, rb.getString("MessageLongDupsWarning"), rb.getString("MessageShortDupsWarning"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION)) {
                return;
            }
        }
        saveContents();
        final UserPreferencesManager p;
        p = InstanceManager.getDefault(UserPreferencesManager.class);
        p.resetChangeMade();
        if (restartRequired && !InstanceManager.getDefault(ShutDownManager.class).isShuttingDown()) {
            JLabel question = new JLabel(MessageFormat.format(rb.getString("MessageLongQuitWarning"), Application.getApplicationName()));
            Object[] options = {rb.getString("RestartNow"), rb.getString("RestartLater")};
            int retVal = JOptionPane.showOptionDialog(this,
                    question,
                    MessageFormat.format(rb.getString("MessageShortQuitWarning"), Application.getApplicationName()),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    null);
            switch (retVal) {
                case JOptionPane.YES_OPTION:
                    dispose();
                    Apps.handleRestart();
                    break;
                case JOptionPane.NO_OPTION:
                    break;
                default:
                    break;
            }
        }
        // don't restart the program, just close the window
        if (getTopLevelAncestor() != null) {
            getTopLevelAncestor().setVisible(false);
        }
    }

    public String getClassDescription() {
        return rb.getString("Application");
    }

    public String getClassName() {
        return AppConfigBase.class.getName();
    }

    /**
     * @return the preferencesPanels
     */
    public HashMap<String, PreferencesPanel> getPreferencesPanels() {
        return preferencesPanels;
    }

}
