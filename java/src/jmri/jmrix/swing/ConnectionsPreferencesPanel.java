package jmri.jmrix.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.beans.IndexedPropertyChangeEvent;
import java.beans.PropertyChangeEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import jmri.InstanceManager;
import jmri.SystemConnectionMemo;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.profile.ProfileManager;
import jmri.swing.ManagingPreferencesPanel;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;
import jmri.util.startup.AbstractActionModel;
import jmri.util.startup.StartupActionsManager;
import jmri.util.swing.JmriJOptionPane;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
@ServiceProvider(service = PreferencesPanel.class)
public class ConnectionsPreferencesPanel extends JTabbedPane implements ManagingPreferencesPanel {

    private static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle"); // for some items // NOI18N


    private final ImageIcon deleteIcon;
    private final ImageIcon deleteIconRollOver;
    private final Dimension deleteButtonSize;
    private ImageIcon addIcon;
    private boolean restartRequired = false;

    private ArrayList<JmrixConfigPane> configPanes = new ArrayList<>();

    public ConnectionsPreferencesPanel() {
        super();
        deleteIconRollOver = new ImageIcon(
                FileUtil.findURL("program:resources/icons/misc/gui3/Delete16x16.png"));
        deleteIcon = new ImageIcon(
                FileUtil.findURL("program:resources/icons/misc/gui3/Delete-bw16x16.png"));
        deleteButtonSize = new Dimension(
                deleteIcon.getIconWidth() + 2,
                deleteIcon.getIconHeight() + 2);
        addIcon = new ImageIcon(
                FileUtil.findURL("program:resources/icons/misc/gui3/Add16x16.png"));
        ConnectionConfigManager ccm = InstanceManager.getDefault(ConnectionConfigManager.class);
        ConnectionConfig[] connections = ccm.getConnections();
        if (connections.length != 0) {
            for (int i = 0; i < connections.length; i++) {
                addConnection(i, JmrixConfigPane.createPanel(i));
            }
        } else {
            addConnection(0, JmrixConfigPane.createNewPanel());
        }
        ccm.addPropertyChangeListener(ConnectionConfigManager.CONNECTIONS, (PropertyChangeEvent evt) -> {
            int i = ((IndexedPropertyChangeEvent) evt).getIndex();
            log.debug("PrefPanel ChangeListener of tab index i = {} of {} list", i+1, configPanes.size());
            //if (evt.getNewValue() == null
            //        && i < configPanes.size()
            //        && evt.getOldValue().equals(configPanes.get(i).getCurrentObject())) {
            //    //removeTab(null, i); // called same method removeTab again, conn tab was already removed by the user click
            //} else
            if ((evt.getOldValue() == null) && (evt.getNewValue() != null)) {
                for (JmrixConfigPane pane : this.configPanes) {
                    if (pane.getCurrentObject() == null ) {
                        log.error("did not expect pane.getCurrentObject()==null here for {} {} {}", i, evt.getNewValue(), configPanes);
                    } else if (pane.getCurrentObject().equals(evt.getNewValue())) {
                        return; // don't add the connection again
                    }
                }
                addConnection(i, JmrixConfigPane.createPanel(i));
            }
        });
        this.addChangeListener(addTabListener);
        newConnectionTab();
        this.setSelectedIndex(0);
    }

    transient ChangeListener addTabListener = (ChangeEvent evt) -> {
        // This method is called whenever the selected tab changes
        JTabbedPane pane = (JTabbedPane) evt.getSource();
        int sel = pane.getSelectedIndex();
        if (sel == -1) {
            addConnectionTab();
            return;
        } else {
            Icon icon = pane.getIconAt(sel);
            if (icon == addIcon) {
                addConnectionTab();
                return;
            }
        }
        activeTab();
    };

    private void activeTab() {
        for (int i = 0; i < this.getTabCount() - 1; i++) {
            JPanel panel = (JPanel) this.getTabComponentAt(i);
            if (panel != null) {
                panel.invalidate();
                Component[] comp = panel.getComponents();
                for (Component c : comp) {
                    if (c instanceof JButton) {
                        if (i == this.getSelectedIndex()) {
                            c.setVisible(true);
                        } else {
                            c.setVisible(false);
                        }
                    }
                }
            }
        }
    }

    private void addConnection(int tabPosition, final JmrixConfigPane configPane) {
        JPanel p = new JPanel();
        p.setLayout(new BorderLayout());
        p.add(configPane, BorderLayout.CENTER);

        JButton tabCloseButton = new JButton(deleteIcon);
        tabCloseButton.setPreferredSize(deleteButtonSize);
        tabCloseButton.setBorderPainted(false);
        tabCloseButton.setRolloverIcon(deleteIconRollOver);
        tabCloseButton.setVisible(false);

        JPanel c = new JPanel();
        c.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0));
        final JCheckBox disable = new JCheckBox(rb.getString("ButtonDisableConnection"));
        disable.setSelected(configPane.getDisabled());
        disable.addActionListener((ActionEvent e) -> {
            configPane.setDisabled(disable.isSelected());
        });
        c.add(disable);
        p.add(c, BorderLayout.SOUTH);
        String title;

        if (configPane.getConnectionName() != null) {
            title = configPane.getConnectionName();
        } else if ((configPane.getCurrentProtocolName() != null)
                && (!configPane.getCurrentProtocolName().equals(
                        JmrixConfigPane.NONE))) {
            title = configPane.getCurrentProtocolName();
        } else {
            title = rb.getString("TabbedLayoutConnection") + (tabPosition + 1);
            if (this.indexOfTab(title) != -1) {
                for (int x = 2; x < 12; x++) {
                    title = rb.getString("TabbedLayoutConnection")
                            + (tabPosition + 2);
                    if (this.indexOfTab(title) != -1) {
                        break;
                    }
                }
            }
        }

        final JPanel tabTitle = new JPanel(new BorderLayout(5, 0));
        tabTitle.setOpaque(false);
        p.setName(title);

        if (configPane.getDisabled()) {
            title = "(" + title + ")";
        }

        JLabel tabLabel = new JLabel(title, JLabel.LEFT);
        tabTitle.add(tabLabel, BorderLayout.WEST);
        tabTitle.add(tabCloseButton, BorderLayout.EAST);

        this.configPanes.add(configPane);
        this.add(p);
        this.setTabComponentAt(tabPosition, tabTitle);

        tabCloseButton.addActionListener((ActionEvent e) -> { // initial user Delete conn click
            removeTab(e, this.indexOfTabComponent(tabTitle));
        });

        this.setToolTipTextAt(tabPosition, title);

        if (configPane.getCurrentObject() != null) {
            if (ConnectionStatus.instance().isConnectionOk(
                    configPane.getCurrentObject().getAdapter().getSystemConnectionMemo())) {
                tabLabel.setForeground(Color.black);
            } else {
                tabLabel.setForeground(Color.red);
            }
        }
        if (configPane.getDisabled()) {
            tabLabel.setForeground(Color.ORANGE);
        }

    }

    void addConnectionTab() {
        this.removeTabAt(this.indexOfTab(addIcon));
        addConnection(configPanes.size(), JmrixConfigPane.createNewPanel());
        newConnectionTab();
    }

    private void newConnectionTab() {
        this.addTab(null, addIcon, null, rb.getString("ToolTipAddNewConnection"));
        this.setSelectedIndex(this.getTabCount() - 2);
    }

    private void removeTab(ActionEvent e, int x) {
        int i;
        i = x; // copy x for handling

        if (i != -1) {
            // only prompt if triggered by action event
            if (e != null && !confirmDelete(getSystemPrefix(this.configPanes.get(i)), this.getTitleAt(i))) {
                return;
            }

            log.debug("i = {}, this.getTabCount() = {}, configPanes.size()={}", i, this.getTabCount(), configPanes.size());
            JmrixConfigPane configPane = this.configPanes.get(i);
            this.removeChangeListener(addTabListener);
            this.remove(i);
            log.debug("start of connection #{} disposal", i);
            try {
                JmrixConfigPane.dispose(configPane);
            } catch (NullPointerException ex) {
                log.error("Caught Null Pointer Exception while removing connection tab {}", i);
            }
            log.debug("connection tab #{} successfully disposed. configPanes.size() = {}", i+1, configPanes.size());
            this.configPanes.remove(i); // on delete of connection a loop (listener) called this method 2x
            restartRequired = true;
            if (this.getTabCount() == 1) { // normally not required, stays in place while adding or deleting
                addConnectionTab();
            }
            if (x != 0) {
                this.setSelectedIndex(x - 1);
            } else {
                this.setSelectedIndex(0);
            }
            this.addChangeListener(addTabListener);
        }
        activeTab();
    }

    /**
     * Get the system prefix of the connection in a pane.
     *
     * @param configPane the connection pane
     * @return the system prefix, or null if the pane has no connection
     */
    @CheckForNull
    static String getSystemPrefix(@CheckForNull JmrixConfigPane configPane) {
        if (configPane == null) {
            return null;
        }
        ConnectionConfig config = configPane.getCurrentObject();
        if (config == null || config.getAdapter() == null
                || config.getAdapter().getSystemConnectionMemo() == null) {
            return null;
        }
        return config.getAdapter().getSystemConnectionMemo().getSystemPrefix();
    }

    /**
     * Ask the user to confirm deleting a connection.
     * <p>
     * If the connection has Start Up actions, one dialog both confirms the
     * delete and asks what to do with each action: remove it (the default),
     * keep it, or move it to another connection that offers the same action
     * and does not already have it. Otherwise the plain Yes/No question is
     * asked. Nothing is changed unless the user confirms.
     *
     * @param prefix the system prefix of the connection being deleted; may be
     *               null if it has no connection
     * @param title  the name of the connection, for the dialog
     * @return true to carry on deleting the connection, false if cancelled
     */
    boolean confirmDelete(@CheckForNull String prefix, String title) {
        String question = MessageFormat.format(rb.getString("MessageDoDelete"), title);
        StartupActionsManager manager = InstanceManager.getOptionalDefault(StartupActionsManager.class).orElse(null);
        List<AbstractActionModel> models = (prefix == null || manager == null)
                ? new ArrayList<>() : manager.getActionsForConnection(prefix);
        if (models.isEmpty()) {
            return JmriJOptionPane.showConfirmDialog(this, question,
                    rb.getString("MessageDeleteConnection"),
                    JmriJOptionPane.YES_NO_OPTION) == JmriJOptionPane.YES_OPTION;
        }

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(2, 2, 2, 2);
        panel.add(new JLabel(question), c);
        c.gridy++;
        c.insets = new Insets(8, 2, 6, 2);
        panel.add(new JLabel(rb.getString("MessageStartupActionsForConnection")), c);
        c.gridwidth = 1;
        c.insets = new Insets(2, 2, 2, 8);
        List<JComboBox<StartupActionChoice>> boxes = new ArrayList<>();
        for (AbstractActionModel model : models) {
            JComboBox<StartupActionChoice> box = new JComboBox<>();
            for (StartupActionChoice choice : getChoices(manager, model, prefix)) {
                box.addItem(choice);
            }
            boxes.add(box);
            c.gridy++;
            c.gridx = 0;
            panel.add(new JLabel(model.getName()), c);
            c.gridx = 1;
            panel.add(box, c);
        }

        String[] options = {rb.getString("ButtonDeleteConnection"), Bundle.getMessage("ButtonCancel")};
        int n = JmriJOptionPane.showOptionDialog(this, panel, rb.getString("MessageDeleteConnection"),
                JmriJOptionPane.DEFAULT_OPTION, JmriJOptionPane.QUESTION_MESSAGE, null, options, options[1]);
        if (n != 0) {
            return false;
        }
        for (int k = 0; k < models.size(); k++) {
            applyStartupActionChoice(manager, models.get(k), (StartupActionChoice) boxes.get(k).getSelectedItem());
        }
        return true;
    }

    /**
     * Get the choices for a Start Up action of a connection being deleted:
     * remove (first, the default), keep, and a move to each other running
     * connection that offers the action and does not already have it.
     *
     * @param manager the Start Up actions manager
     * @param model   the action
     * @param prefix  the system prefix of the connection being deleted
     * @return the choices, in the order shown
     */
    @Nonnull
    static List<StartupActionChoice> getChoices(@Nonnull StartupActionsManager manager,
            @Nonnull AbstractActionModel model, @CheckForNull String prefix) {
        List<StartupActionChoice> choices = new ArrayList<>();
        choices.add(new StartupActionChoice(StartupActionChoice.Kind.REMOVE, null,
                rb.getString("StartupActionRemove")));
        choices.add(new StartupActionChoice(StartupActionChoice.Kind.KEEP, null,
                rb.getString("StartupActionKeep")));
        for (SystemConnectionMemo memo : StartupActionsManager.getConnectionsOffering(model, prefix)) {
            if (!hasAction(manager, model.getClassName(), memo.getSystemPrefix())) {
                choices.add(new StartupActionChoice(StartupActionChoice.Kind.MOVE, memo.getSystemPrefix(),
                        MessageFormat.format(rb.getString("StartupActionMoveTo"),
                                memo.getUserName(), memo.getSystemPrefix())));
            }
        }
        return choices;
    }

    private static boolean hasAction(StartupActionsManager manager, String className, String systemPrefix) {
        return manager.getActionsForConnection(systemPrefix).stream()
                .anyMatch(other -> className.equals(other.getClassName()));
    }

    /**
     * Carry out the choice made for a Start Up action of a connection being
     * deleted.
     *
     * @param manager the Start Up actions manager
     * @param model   the action
     * @param choice  what to do; null is treated as remove
     */
    static void applyStartupActionChoice(@Nonnull StartupActionsManager manager,
            @Nonnull AbstractActionModel model, @CheckForNull StartupActionChoice choice) {
        if (choice == null || choice.kind == StartupActionChoice.Kind.REMOVE) {
            manager.removeAction(model);
        } else if (choice.kind == StartupActionChoice.Kind.MOVE && choice.systemPrefix != null) {
            manager.reassignAction(model, choice.systemPrefix);
        }
        // KEEP: left as it is; shown in red until moved or removed
    }

    /**
     * One choice for a Start Up action of a connection being deleted.
     */
    static class StartupActionChoice {

        enum Kind {
            REMOVE, KEEP, MOVE
        }

        final Kind kind;
        final String systemPrefix; // the connection to move to, for MOVE only
        private final String text;

        StartupActionChoice(@Nonnull Kind kind, @CheckForNull String systemPrefix, @Nonnull String text) {
            this.kind = kind;
            this.systemPrefix = systemPrefix;
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }
    }

    @Override
    public String getPreferencesItem() {
        return "CONNECTIONS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rb.getString("MenuConnections"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return null;
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
        return false;
    }

    @Override
    public String getPreferencesTooltip() {
        return null;
    }

    @Override
    public void savePreferences() {
        InstanceManager.getDefault(ConnectionConfigManager.class).savePreferences(ProfileManager.getDefault().getActiveProfile());
    }

    @Override
    public boolean isDirty() {
        return this.configPanes.stream().anyMatch(JmrixConfigPane::isDirty);
    }

    @Override
    public boolean isRestartRequired() {
        return this.restartRequired
                || this.configPanes.stream().anyMatch(JmrixConfigPane::isRestartRequired);
    }

    @Override
    public boolean isPreferencesValid() {
        return this.configPanes.stream().allMatch(JmrixConfigPane::isPreferencesValid);
    }

    @Override
    public List<PreferencesPanel> getPreferencesPanels() {
        return new ArrayList<>(this.configPanes);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionsPreferencesPanel.class);

}
