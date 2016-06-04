package jmri.jmrix.swing;

import apps.gui3.TabbedPreferences;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import jmri.InstanceManager;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.JmrixConfigPane;
import jmri.profile.ProfileManager;
import jmri.swing.ManagingPreferencesPanel;
import jmri.swing.PreferencesPanel;
import jmri.util.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood randall.h.wood@alexandriasoftware.com
 */
public class ConnectionsPreferencesPanel extends JTabbedPane implements ManagingPreferencesPanel {

    private static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle"); // for some items // NOI18N
    private static final Logger log = LoggerFactory.getLogger(ConnectionsPreferencesPanel.class);
    private static final long serialVersionUID = 3007932795460089101L;

    private final TabbedPreferences preferences;
    private final ImageIcon deleteIcon;
    private final ImageIcon deleteIconRollOver;
    private final Dimension deleteButtonSize;
    private ImageIcon addIcon;
    private boolean restartRequired = false;

    private ArrayList<JmrixConfigPane> configPanes = new ArrayList<>();

    public ConnectionsPreferencesPanel(TabbedPreferences preferences) {
        super();
        this.preferences = preferences;
        deleteIconRollOver = new ImageIcon(
                FileUtil.findURL("program:resources/icons/misc/gui3/Delete16x16.png"));
        deleteIcon = new ImageIcon(
                FileUtil.findURL("program:resources/icons/misc/gui3/Delete-bw16x16.png"));
        deleteButtonSize = new Dimension(
                deleteIcon.getIconWidth() + 2,
                deleteIcon.getIconHeight() + 2);
        addIcon = new ImageIcon(
                FileUtil.findURL("program:resources/icons/misc/gui3/Add16x16.png"));
        if (this.preferences != null) {
            ConnectionConfig[] connections = InstanceManager.getDefault(ConnectionConfigManager.class).getConnections();
            if (connections.length != 0) {
                for (int x = 0; x < connections.length; x++) {
                    JmrixConfigPane configPane = JmrixConfigPane.instance(x);
                    addConnection(x, configPane);
                }
            } else {
                addConnection(0, JmrixConfigPane.createNewPanel());
            }
            this.addChangeListener(addTabListener);
            newConnectionTab();
            this.setSelectedIndex(0);
        }
    }

    public ConnectionsPreferencesPanel() {
        this(InstanceManager.getDefault(TabbedPreferences.class));
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

        tabCloseButton.addActionListener((ActionEvent e) -> {
            removeTab(e, this.indexOfTabComponent(tabTitle));
        });

        this.setToolTipTextAt(tabPosition, title);

        if (ConnectionStatus.instance().isConnectionOk(
                configPane.getCurrentProtocolInfo())) {
            tabLabel.setForeground(Color.black);
        } else {
            tabLabel.setForeground(Color.red);
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

        i = x;

        if (i != -1) {
            int n = JOptionPane.showConfirmDialog(null, MessageFormat.format(
                    rb.getString("MessageDoDelete"),
                    new Object[]{this.getTitleAt(i)}),
                    rb.getString("MessageDeleteConnection"),
                    JOptionPane.YES_NO_OPTION);
            if (n != JOptionPane.YES_OPTION) {
                return;
            }

            JmrixConfigPane configPane = this.configPanes.get(i);

            this.removeChangeListener(addTabListener);
            this.remove(i); // was x
            try {
                JmrixConfigPane.dispose(configPane);
            } catch (NullPointerException ex) {
                log.error("Caught Null Pointer Exception while removing connection tab");
            }
            this.configPanes.remove(i);
            if (this.getTabCount() == 1) {
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
        return this.configPanes.stream().anyMatch((panel) -> (panel.isDirty()));
    }

    @Override
    public boolean isRestartRequired() {
        return this.restartRequired
                || this.configPanes.stream().anyMatch((panel) -> (panel.isRestartRequired()));
    }

    @Override
    public boolean isPreferencesValid() {
        return this.configPanes.stream().allMatch((panel) -> (panel.isPreferencesValid()));
    }

    @Override
    public List<PreferencesPanel> getPreferencesPanels() {
        return new ArrayList<>(this.configPanes);
    }
}
