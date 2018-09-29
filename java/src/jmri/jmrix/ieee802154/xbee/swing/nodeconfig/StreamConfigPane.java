package jmri.jmrix.ieee802154.xbee.swing.nodeconfig;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import jmri.ConfigureManager;
import jmri.jmrix.AbstractStreamPortController;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.JmrixConfigPane;
import jmri.InstanceManager;
import jmri.swing.JTitledSeparator;
import jmri.swing.PreferencesPanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrix.ieee802154.xbee.XBeeNode;

/**
 * Provide GUI to configure communications links.
 * <p>
 * This is really just a catalog of connections to classes within the systems.
 * Reflection is used to reduce coupling at load time.
 * <p>
 * Objects of this class are based on an underlying ConnectionConfig
 * implementation, which in turn is obtained from the InstanceManager. Those
 * must be created at load time by the ConfigXml process, or in some Application
 * class.
 * <p>
 * The classes referenced are the specific subclasses of
 * {@link jmri.jmrix.ConnectionConfig} which provides the methods providing data
 * to the configuration GUI, and responding to its changes.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2003, 2004, 2010
 */
public class StreamConfigPane extends JmrixConfigPane {

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(StreamConfigPane.class);

    /*
     * Create panel is seperated off from the instance and synchronized, so that only
     * one connection can be configured at once, this prevents multiple threads from
     * trying to create the same panel at the same time.
     */
    /**
     * Create a new connection configuration panel.
     *
     * @param node the XBee node associated with the connection.
     *
     * @return the panel for the requested connection or for a new connection if
     *         index did not match an existing connection configuration
     */
    public static synchronized StreamConfigPane createPanel(XBeeNode node) {
        ConnectionConfig c = null;
        /*try {
            c = InstanceManager.getDefault(ConnectionConfigManager.class).getConnections(index);
            log.debug("connection {} is {}", index, c);
        } catch (IndexOutOfBoundsException ex) {
            log.debug("connection {} is null, creating new one", index);
        }*/
        return createPanel(c);
    }

    /**
     * Create a new configuration panel for the given connection.
     *
     * @param c the connection; if null, the panel is ready for a new connection
     * @return the new panel
     */
    public static synchronized StreamConfigPane createPanel(ConnectionConfig c) {
        StreamConfigPane pane = new StreamConfigPane(c);
        if (c == null) {
            pane.isDirty = true;
        }
        return pane;
    }

    /**
     * Disposes of the underlying connection for a configuration pane.
     *
     * @param confPane the pane to dispose of
     */
    public static void dispose(StreamConfigPane confPane) {
        if (confPane == null) {
            log.debug("no instance found therefore can not dispose of it!");
            return;
        }

        if (confPane.ccCurrent != null) {
            try {
                confPane.ccCurrent.dispose();
            } catch (RuntimeException ex) {
                log.error("Error Occurred while disposing connection {}", ex.toString());
            }
        }
        ConfigureManager cmOD = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cmOD != null) {
            cmOD.deregister(confPane);
            cmOD.deregister(confPane.ccCurrent);
        }
        InstanceManager.getDefault(ConnectionConfigManager.class).remove(confPane.ccCurrent);
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
    protected StreamConfigPane(ConnectionConfig original) {
        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);
        ccCurrent = original;

        setLayout(new BorderLayout());
        this.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));

        manuBox.addItem(NONE_SELECTED);

        //manufactureNameList = manager.getConnectionManufacturers();
        /*Set<Class<? extends AbstractStreamPortController>> subTypes = reflections.getSubTypesOf(AbstractStreamPortController.class);
        for (Class cType : subTypes) {
            manuBox.addItem(cType.getName());
        }*/
        manuBox.addItem("jmri.jmrix.lenz.XNetStreamPortController.class");
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
    public String getTabbedPreferencesTitle() {
        String title = this.getConnectionName();
        if (title == null
                && this.getCurrentProtocolName() != null
                && !this.getCurrentProtocolName().equals(StreamConfigPane.NONE)) {
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
