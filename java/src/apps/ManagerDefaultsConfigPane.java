package apps;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.managers.ManagerDefaultSelector;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.swing.PreferencesPanel;
import jmri.util.javaworld.GridLayout2;
import jmri.util.swing.JmriPanel;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide GUI to configure InstanceManager defaults.
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @since 2.9.5
 */
@ServiceProvider(service = PreferencesPanel.class)
public final class ManagerDefaultsConfigPane extends JmriPanel implements PreferencesPanel {

    private static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    private boolean dirty = false;

    public ManagerDefaultsConfigPane() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        matrix = new JPanel();
        add(matrix);
        InstanceManager.getDefault(ManagerDefaultSelector.class).addPropertyChangeListener((PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals("Updated")) {
                update();
            }
        });
        update();
    }

    JPanel matrix;

    /**
     * Invoke when first displayed to load and present options.
     */
    public void update() {
        log.debug(" update start");
        matrix.removeAll();

        // this doesn't find non-migrated systems, how do we handle that eventually?
        List<SystemConnectionMemo> connList = InstanceManager.getList(SystemConnectionMemo.class);
        if (!connList.isEmpty()) {
            log.debug("   update of {} connections", connList.size());
            reloadConnections(connList);
        } else {
            log.debug("   update with no new-form system connections configured");
            matrix.add(new JLabel("No new-form system connections configured"));
        }
        log.debug(" update end");
    }

    void reloadConnections(List<SystemConnectionMemo> connList) {
        log.debug(" reloadConnections start");
        ManagerDefaultSelector manager = InstanceManager.getDefault(ManagerDefaultSelector.class);
        matrix.setLayout(new GridLayout2(connList.size() + 1, manager.knownManagers.length + 1));
        matrix.add(new JLabel(""));

        for (ManagerDefaultSelector.Item item : manager.knownManagers) {
            log.trace("   Add typeName {}", item.typeName);
            matrix.add(new JLabel(item.typeName));
        }
        groups = new ButtonGroup[manager.knownManagers.length];
        for (int i = 0; i < manager.knownManagers.length; i++) {
            groups[i] = new ButtonGroup();
        }
        boolean[] selected = new boolean[manager.knownManagers.length];
        for (int x = 0; x < connList.size(); x++) { // up to down
            jmri.jmrix.SystemConnectionMemo memo = connList.get(x);
            String connectionName = memo.getUserName();
            log.trace("   Connection name {}", connectionName);
            matrix.add(new JLabel(connectionName));
            int i = 0;
            for (ManagerDefaultSelector.Item item : manager.knownManagers) { // left to right
                log.trace("      item {}", item.typeName);
                if (memo.provides(item.managerClass)) {
                    JRadioButton r = new SelectionButton(connectionName, item.typeName, item.managerClass, this);
                    matrix.add(r);
                    groups[i].add(r);
                    if (!selected[i] && manager.getDefault(item.managerClass) == null) {
                        log.trace("      setting selected based on default");
                        r.setSelected(true);
                        selected[i] = true;
                    }
                } else {
                    // leave a blank
                    JRadioButton r = new JRadioButton();
                    r.setToolTipText(Bundle.getMessage("TooltipDefaultnotValid", connectionName, dropTags(item.typeName)));
                    r.setEnabled(false);
                    matrix.add(r);
                }
                i++; //we need to increment 'i' as we are going onto the next group even if we added a blank button
            }
        }
        revalidate();
        log.debug(" reloadConnections end");
    }

    ButtonGroup[] groups;

    @Override
    public String getPreferencesItem() {
        return "DEFAULTS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return rb.getString("MenuDefaults"); // NOI18N
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rb.getString("TabbedLayoutDefaults"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rb.getString("LabelTabbedLayoutDefaults"); // NOI18N
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
        return null;
    }

    @Override
    public void savePreferences() {
        Profile profile = ProfileManager.getDefault().getActiveProfile();
        if (profile != null) {
            InstanceManager.getDefault(ManagerDefaultSelector.class).savePreferences(profile);
        }
    }

    @Override
    public boolean isDirty() {
        return this.dirty;
    }

    @Override
    public boolean isRestartRequired() {
        return this.isDirty();
    }

    @Override
    public boolean isPreferencesValid() {
        return InstanceManager.getDefault(ManagerDefaultSelector.class).isPreferencesValid(ProfileManager.getDefault().getActiveProfile());
    }

    private static String dropTags(String s) {
        //while (s.contains("<")) {
            return s.replaceAll("</?[a-zA-Z]*>"," ");
        //}
    }
    
    /**
     * Captive class to track changes.
     */
    static final class SelectionButton extends JRadioButton {

        SelectionButton(String connectionName, String managerName, Class<?> managerClass, ManagerDefaultsConfigPane pane) {
            super();
            this.managerClass = managerClass;
            this.connectionName = connectionName;
            // we want to remove tags from the manager name
            this.managerName = dropTags(managerName);

            // for screen readers
            setToolTipText(makeToolTipText());
            
            log.trace("      SelectionButton ctor for {} as {}", connectionName, managerName);
            if (connectionName.equals(InstanceManager.getDefault(ManagerDefaultSelector.class).getDefault(managerClass))) {
                this.setSelected(true);
            }

            addActionListener((ActionEvent e) -> {
                if (isSelected()) {
                    InstanceManager.getDefault(ManagerDefaultSelector.class).setDefault(SelectionButton.this.managerClass, SelectionButton.this.connectionName);
                    pane.dirty = true;
                    setToolTipText(Bundle.getMessage("TooltipDefaultSelectedRestart", this.connectionName, this.managerName)); // update the tooltip when selected
                }
            });

        }
        String connectionName;
        String managerName;
        Class<?> managerClass;
        
        private String makeToolTipText() { 
            return (isSelected()? Bundle.getMessage("TooltipDefaultSelected", connectionName, managerName):
                    Bundle.getMessage("TooltipDefaultNotSelected", connectionName, managerName));
        }

        @Override
        public void setSelected(boolean t) {
            super.setSelected(t);
            log.debug("SelectionButton setSelected called with {}", t);
            setToolTipText(makeToolTipText());
            if (t) {
                InstanceManager.getDefault(ManagerDefaultSelector.class).setDefault(this.managerClass, this.connectionName);
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ManagerDefaultsConfigPane.class);

}
