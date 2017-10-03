package jmri.jmrix;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide GUI to configure communications links.
 * <P>
 * This is really just a catalog of connections to classes within the systems.
 * Reflection is used to reduce coupling at load time.
 * <P>
 * Objects of this class are based on an underlying ConnectionConfig
 * implementation, which in turn is obtained from the InstanceManager. Those
 * must be created at load time by the ConfigXml process, or in some Application
 * class.
 * <P>
 * The classes referenced are the specific subclasses of
 * {@link jmri.jmrix.ConnectionConfig} which provides the methods providing data
 * to the configuration GUI, and responding to its changes.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2004, 2010
 */
public class JmrixConfigPane extends JPanel implements PreferencesPanel {

    private static final ResourceBundle acb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    public static final String NONE_SELECTED = Bundle.getMessage("noneSelected");
    public static final String NO_PORTS_FOUND = Bundle.getMessage("noPortsFound");
    public static final String NONE = Bundle.getMessage("none");
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(JmrixConfigPane.class);

    /**
     * Get access to a pane describing existing configuration information, or
     * create one if needed.
     * <P>
     * The index argument is used to connect the new pane to the right
     * communications info. A value of "1" means the first (primary) port, 2 is
     * the second, etc.
     *
     * @param index 1-N based index of the communications object to configure.
     * @return a configuration panel for the specified communications object.
     * @deprecated since 4.5.7; use {@link #createPanel(int)} to get a new panel
     * instead.
     */
    @Deprecated
    public static JmrixConfigPane instance(int index) {
        return createPanel(index);
    }

    /**
     * Not in use within JMRI.
     *
     * @param config the configuration for which a pane is desired
     * @return an existing configuration pane or null
     * @deprecated as of 4.5.7
     */
    @Deprecated
    public static JmrixConfigPane instance(ConnectionConfig config) {
        return createPanel(config);
    }

    /*
     * Create panel is seperated off from the instance and synchronized, so that only
     * one connection can be configured at once, this prevents multiple threads from
     * trying to create the same panel at the same time.
     */
    /**
     * Create a new connection configuration panel.
     *
     * @param index the index of the desired connection configuration from
     *              {@link jmri.jmrix.ConnectionConfigManager#getConnections(int)}
     * @return the panel for the requested connection or for a new connection if
     *         index did not match an existing connection configuration
     */
    public static synchronized JmrixConfigPane createPanel(int index) {
        ConnectionConfig c = null;
        try {
            c = InstanceManager.getDefault(ConnectionConfigManager.class).getConnections(index);
            log.debug("connection {} is {}", index, c);
        } catch (IndexOutOfBoundsException ex) {
            log.debug("connection {} is null, creating new one", index);
        }
        return createPanel(c);
    }

    /**
     * Create a new configuration panel for the given connection.
     *
     * @param c the connection; if null, the panel is ready for a new connection
     * @return the new panel
     */
    public static synchronized JmrixConfigPane createPanel(ConnectionConfig c) {
        JmrixConfigPane pane = new JmrixConfigPane(c);
        if (c == null) {
            pane.isDirty = true;
        }
        return pane;
    }

    /**
     * Get access to a new pane for creating new connections.
     *
     * @return a new configuration panel
     */
    public static JmrixConfigPane createNewPanel() {
        return createPanel(null);
    }

    /**
     * Returns 0 since this class no longer maintains a collection of
     * JmrixConfigPane instances.
     *
     * @return 0
     * @deprecated since 4.5.7; use
     * {@link jmri.jmrix.ConnectionConfigManager#getConnections()} to get the
     * collection of connection configurations and use that size.
     */
    @Deprecated
    public static int getNumberOfInstances() {
        return 0;
    }

    /**
     * Does nothing.
     *
     * @param index index of pane
     * @deprecated since 4.5.7; dispose of any held panes by calling
     * {@link #dispose(jmri.jmrix.JmrixConfigPane)}
     */
    @Deprecated
    public static void dispose(int index) {
    }

    /**
     * Disposes of the underlying connection for a configuration pane.
     *
     * @param confPane the pane to dispose of
     */
    public static void dispose(JmrixConfigPane confPane) {
        if (confPane == null) {
            log.debug("no instance found therefore can not dispose of it!");
            return;
        }

        if (confPane.ccCurrent != null) {
            try {
                confPane.ccCurrent.dispose();
            } catch (RuntimeException ex) {
                log.error("Error Occured while disposing connection {}", ex.toString());
            }
        }
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.deregister(confPane);
            cmOD.deregister(confPane.ccCurrent);
        }
        InstanceManager.getDefault(ConnectionConfigManager.class).remove(confPane.ccCurrent);
    }

    /**
     * Returns -1 since a centralized collection of JmrixConfigPanes is not
     * maintained.
     *
     * @param confPane the pane
     * @return -1
     * @deprecated since 4.5.7 without direct replacement.
     */
    @Deprecated
    public static int getInstanceNumber(JmrixConfigPane confPane) {
        return -1;
    }

    /**
     * Does nothing useful.
     *
     * @return an empty list
     * @deprecated since 4.5.7; use
     * {@link jmri.jmrix.ConnectionConfigManager#getConnections()} to get a list
     * of connections
     */
    @Deprecated
    public static ArrayList<JmrixConfigPane> getListOfConfigPanes() {
        return new ArrayList<>();
    }
    private boolean isDirty = false;

    JComboBox<String> modeBox = new JComboBox<>();
    JComboBox<String> manuBox = new JComboBox<>();

    JPanel details = new JPanel();
    String[] classConnectionNameList;
    ConnectionConfig[] classConnectionList;
    String[] manufactureNameList;

    jmri.UserPreferencesManager p = jmri.InstanceManager.getDefault(jmri.UserPreferencesManager.class);

    ConnectionConfig ccCurrent = null;

    /**
     * Use "instance" to get one of these. That allows it to reconnect to
     * existing information in an existing ConnectionConfig object. It's
     * permitted to call this with a null argument, e.g. for when first
     * configuring the system.
     */
    private JmrixConfigPane(ConnectionConfig original) {

        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);
        ccCurrent = original;

        setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        manuBox.addItem(NONE_SELECTED);

        manufactureNameList = manager.getConnectionManufacturers();
        for (String manuName : manufactureNameList) {
            if (original != null && original.getManufacturer() != null
                    && original.getManufacturer().equals(manuName)) {
                manuBox.addItem(manuName);
                manuBox.setSelectedItem(manuName);
            } else {
                manuBox.addItem(manuName);
            }
        }
        manuBox.addActionListener((ActionEvent evt) -> {
            updateComboConnection();
        });

        // get the list of ConnectionConfig items into a selection box
        classConnectionNameList = manager.getConnectionTypes((String) manuBox.getSelectedItem());
        classConnectionList = new jmri.jmrix.ConnectionConfig[classConnectionNameList.length + 1];
        modeBox.addItem(NONE_SELECTED);
        if (manuBox.getSelectedIndex() != 0) {
            modeBox.setEnabled(true);
        } else {
            modeBox.setSelectedIndex(0);
            modeBox.setEnabled(false);
        }
        int n = 1;
        if (manuBox.getSelectedIndex() != 0) {
            for (String className : classConnectionNameList) {
                try {
                    ConnectionConfig config;
                    if (original != null && original.getClass().getName().equals(className)) {
                        config = original;
                        log.debug("matched existing config object");
                        modeBox.addItem(config.name());
                        modeBox.setSelectedItem(config.name());
                        if (classConnectionNameList.length == 1) {
                            modeBox.setSelectedIndex(1);
                        }
                    } else {
                        Class<?> cl = Class.forName(className);
                        config = (ConnectionConfig) cl.newInstance();
                        modeBox.addItem(config.name());
                    }
                    classConnectionList[n++] = config;
                } catch (NullPointerException e) {
                    log.error("Attempt to load {} failed.", className, e);
                } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.debug("Attempt to load {} failed: {}.", className, e);
                }
            }
            if ((modeBox.getSelectedIndex() == 0) && (p.getComboBoxLastSelection((String) manuBox.getSelectedItem()) != null)) {
                modeBox.setSelectedItem(p.getComboBoxLastSelection((String) manuBox.getSelectedItem()));
            }
        }
        modeBox.addActionListener((ActionEvent a) -> {
            if ((String) modeBox.getSelectedItem() != null) {
                if (!((String) modeBox.getSelectedItem()).equals(NONE_SELECTED)) {
                    p.addComboBoxLastSelection((String) manuBox.getSelectedItem(), (String) modeBox.getSelectedItem());
                }
            }
            selection();
        });
        JPanel manufacturerPanel = new JPanel();
        manufacturerPanel.add(manuBox);
        JPanel connectionPanel = new JPanel();
        connectionPanel.add(modeBox);
        JPanel initialPanel = new JPanel();
        initialPanel.setLayout(new BoxLayout(initialPanel, BoxLayout.Y_AXIS));
        initialPanel.add(new JTitledSeparator(Bundle.getMessage("SystemManufacturer"))); // NOI18N
        initialPanel.add(manufacturerPanel);
        initialPanel.add(new JTitledSeparator(Bundle.getMessage("SystemConnection"))); // NOI18N
        initialPanel.add(connectionPanel);
        add(initialPanel, BorderLayout.NORTH);
        initialPanel.add(new JTitledSeparator(Bundle.getMessage("Settings"))); // NOI18N
        JScrollPane scroll = new JScrollPane(details);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        add(scroll, BorderLayout.CENTER);

        selection();  // first time through, pretend we've selected a value
        // to load the rest of the GUI
    }

    public void updateComboConnection() {
        modeBox.removeAllItems();
        modeBox.addItem(NONE_SELECTED);
        classConnectionNameList = InstanceManager.getDefault(ConnectionConfigManager.class).getConnectionTypes((String) manuBox.getSelectedItem());
        classConnectionList = new jmri.jmrix.ConnectionConfig[classConnectionNameList.length + 1];

        if (manuBox.getSelectedIndex() != 0) {
            modeBox.setEnabled(true);
        } else {
            modeBox.setSelectedIndex(0);
            modeBox.setEnabled(false);
        }

        int n = 1;
        if (manuBox.getSelectedIndex() != 0) {
            for (String classConnectionNameList1 : classConnectionNameList) {
                try {
                    jmri.jmrix.ConnectionConfig config;
                    Class<?> cl = Class.forName(classConnectionNameList1);
                    config = (jmri.jmrix.ConnectionConfig) cl.newInstance();
                    modeBox.addItem(config.name());
                    classConnectionList[n++] = config;
                    if (classConnectionNameList.length == 1) {
                        modeBox.setSelectedIndex(1);
                    }
                } catch (NullPointerException | ClassNotFoundException | InstantiationException | IllegalAccessException e) {
                    log.warn("Attempt to load {} failed: {}", classConnectionNameList1, e);
                }
            }
            if (p.getComboBoxLastSelection((String) manuBox.getSelectedItem()) != null) {
                modeBox.setSelectedItem(p.getComboBoxLastSelection((String) manuBox.getSelectedItem()));
            }
        } else {
            if (ccCurrent != null) {
                ccCurrent.dispose();
            }
        }
    }

    void selection() {
        ConnectionConfig old = this.ccCurrent;
        int current = modeBox.getSelectedIndex();
        details.removeAll();
        // first choice is -no- protocol chosen
        log.debug("new selection is {} {}", current, modeBox.getSelectedItem());
        if ((current != 0) && (current != -1)) {
            if ((ccCurrent != null) && (ccCurrent != classConnectionList[current])) {
                ccCurrent.dispose();
            }
            ccCurrent = classConnectionList[current];
            classConnectionList[current].loadDetails(details);
            classConnectionList[current].setManufacturer((String) manuBox.getSelectedItem());
        } else {
            if (ccCurrent != null) {
                ccCurrent.dispose();
            }
        }
        if (old != this.ccCurrent) {
            this.ccCurrent.register();
        }
        validate();

        repaint();
    }

    public String getConnectionName() {
        int current = modeBox.getSelectedIndex();
        if (current == 0) {
            return null;
        }
        return classConnectionList[current].getConnectionName();
    }

    public String getCurrentManufacturerName() {
        int current = modeBox.getSelectedIndex();
        if (current == 0) {
            return NONE;
        }
        return classConnectionList[current].getManufacturer();
    }

    public String getCurrentProtocolName() {
        int current = modeBox.getSelectedIndex();
        if (current == 0) {
            return NONE;
        }
        return classConnectionList[current].name();
    }

    public String getCurrentProtocolInfo() {
        int current = modeBox.getSelectedIndex();
        if (current == 0) {
            return NONE;
        }
        return classConnectionList[current].getInfo();
    }

    public ConnectionConfig getCurrentObject() {
        int current = modeBox.getSelectedIndex();
        if (current != 0) {
            return classConnectionList[current];
        }
        return null;
    }

    public boolean getDisabled() {
        int current = modeBox.getSelectedIndex();
        if (current == 0) {
            return false;
        }
        return classConnectionList[current].getDisabled();
    }

    public void setDisabled(boolean disabled) {
        int current = modeBox.getSelectedIndex();
        if (current == 0) {
            return;
        }
        classConnectionList[current].setDisabled(disabled);
    }

    @Override
    public String getPreferencesItem() {
        return "CONNECTIONS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return acb.getString("MenuConnections"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        String title = this.getConnectionName();
        if (title == null
                && this.getCurrentProtocolName() != null
                && !this.getCurrentProtocolName().equals(JmrixConfigPane.NONE)) {
            title = this.getCurrentProtocolName();
        }
        if (title != null && !this.getDisabled()) {
            title = "(" + title + ")";
        }
        return title;
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return true;
    }

    @Override
    public String getPreferencesTooltip() {
        return this.getTabbedPreferencesTitle();
    }

    @Override
    public void savePreferences() {
        // do nothing - the persistant manager will take care of this
    }

    @Override
    public boolean isDirty() {
        // avoid potentially expensive exrta test for isDirty
        if (log.isDebugEnabled()) {
            log.debug("Connection \"{}\" is {}.",
                    this.getConnectionName(),
                    (this.isDirty || ((this.ccCurrent != null) ? this.ccCurrent.isDirty() : true) ? "dirty" : "clean"));
        }
        return this.isDirty || ((this.ccCurrent != null) ? this.ccCurrent.isDirty() : true);
    }

    @Override
    public boolean isRestartRequired() {
        return (this.ccCurrent != null) ? this.ccCurrent.isRestartRequired() : this.isDirty();
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }

}
