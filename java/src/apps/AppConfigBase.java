package apps;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import jmri.Application;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.UserPreferencesManager;
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
 * @author	Bob Jacobsen Copyright (C) 2003, 2008, 2010
 * @author Matthew Harris copyright (c) 2009
 * @author	Ken Cameron Copyright (C) 2011
 */
public class AppConfigBase extends JmriPanel {

    /**
     * All preferences panels handled, whether persisted or not. This is a
     * LinkedHashMap and not just a HashMap because parts of JMRI are dependent
     * upon the order in which preferences are read. The order is determined by
     * the 
     */
    protected LinkedHashMap<String, PreferencesPanel> preferencesPanels = new LinkedHashMap<>();

    protected static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");

    private static final Logger log = LoggerFactory.getLogger(AppConfigBase.class);

    /**
     * Construct a configuration panel for inclusion in a preferences or
     * configuration dialog with default number of connections.
     */
    public AppConfigBase() {
    }

    /**
     * @deprecated as of 2.13.3, directly access the connection configuration
     * from the instance list
     * jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class)
     */
    @Deprecated
    public static String getManufacturerName(int index) {
        return JmrixConfigPane.instance(index).getCurrentManufacturerName();
    }

    /**
     * @deprecated as of 2.13.3, directly access the connection configuration
     * from the instance list
     * jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class)
     */
    @Deprecated
    public static String getConnection(int index) {
        return JmrixConfigPane.instance(index).getCurrentProtocolName();
    }

    /**
     * @deprecated as of 2.13.3, directly access the connection configuration
     * from the instance list
     * jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class)
     */
    @Deprecated
    public static String getPort(int index) {
        return JmrixConfigPane.instance(index).getCurrentProtocolInfo();
    }

    /**
     * @deprecated as of 2.13.3, directly access the connection configuration
     * from the instance list
     * jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class)
     */
    @Deprecated
    public static String getConnectionName(int index) {
        return JmrixConfigPane.instance(index).getConnectionName();
    }

    /**
     * @deprecated as of 2.13.3, directly access the connection configuration
     * from the instance list
     * jmri.InstanceManager.configureManagerInstance().getInstanceList(jmri.jmrix.ConnectionConfig.class)
     */
    @Deprecated
    public static boolean getDisabled(int index) {
        return JmrixConfigPane.instance(index).getDisabled();
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
        Map<String, List<JmrixConfigPane>> ports = new HashMap<>();
        ArrayList<JmrixConfigPane> configPaneList = JmrixConfigPane.getListOfConfigPanes();
        for (JmrixConfigPane configPane : configPaneList) {
            if (!configPane.getDisabled()) {
                String port = configPane.getCurrentProtocolInfo();
                /*We need to test to make sure that the connection port is not set to (none)
                 If it is set to none, then it is likely a simulator.*/
                if (!port.equals(JmrixConfigPane.NONE)) {
                    if (!ports.containsKey(port)) {
                        List<JmrixConfigPane> arg1 = new ArrayList<>();
                        arg1.add(configPane);
                        ports.put(port, arg1);
                    } else {
                        ports.get(port).add(configPane);
                    }
                }
            }
        }
        boolean ret = true;
        /* one or more dups or NONE, lets see if it is dups */
        for (Map.Entry<String, List<JmrixConfigPane>> e : ports.entrySet()) {
            if (e.getValue().size() > 1) {
                /* dup port found */
                ret = false;
                StringBuilder nameB = new StringBuilder();
                for (int n = 0; n < e.getValue().size(); n++) {
                    nameB.append(e.getValue().get(n).getCurrentManufacturerName());
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
        for (JmrixConfigPane configPane : JmrixConfigPane.getListOfConfigPanes()) {
            String port = configPane.getCurrentProtocolInfo();
            if (port.equals(JmrixConfigPane.NONE_SELECTED) || port.equals(JmrixConfigPane.NO_PORTS_FOUND)) {
                if (JOptionPane.showConfirmDialog(null, MessageFormat.format(rb.getString("MessageSerialPortWarning"), new Object[]{port, configPane.getCurrentProtocolName()}), rb.getString("MessageSerialPortNotValid"), JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) != JOptionPane.YES_OPTION) {
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
        InstanceManager.configureManagerInstance().removePrefItems();
        // put the new GUI managedPreferences on the persistance list
        this.getPreferencesPanels().values().stream().forEach((panel) -> {
            this.registerWithConfigureManager(panel);
        });
        InstanceManager.configureManagerInstance().storePrefs();
    }

    private void registerWithConfigureManager(PreferencesPanel panel) {
        if (panel.isPersistant()) {
            InstanceManager.configureManagerInstance().registerPref(panel);
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
