// ManagerDefaultsConfigPane.java
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
import jmri.swing.PreferencesPanel;
import jmri.util.javaworld.GridLayout2;
import jmri.util.swing.JmriPanel;

/**
 * Provide GUI to configure InstanceManager defaults.
 * <P>
 *
 * @author Bob Jacobsen Copyright (C) 2010
 * @version	$Revision$
 * @since 2.9.5
 */
public class ManagerDefaultsConfigPane extends JmriPanel implements PreferencesPanel {

    /**
     *
     */
    private static final long serialVersionUID = 4382220076212974325L;
    private static final ResourceBundle rb = ResourceBundle.getBundle("apps.AppsConfigBundle");
    private boolean dirty = false;

    public ManagerDefaultsConfigPane() {

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        matrix = new JPanel();
        add(matrix);
        ManagerDefaultSelector.instance.addPropertyChangeListener((PropertyChangeEvent e) -> {
            if (e.getPropertyName().equals("Updated")) {
                update();
            }
        });
        update();
    }

    JPanel matrix;

    /**
     * Invoke when first displayed to load and present options
     */
    public void update() {
        matrix.removeAll();

        // this doesn't find non-migrated systems, how do we handle that eventually?
        List<SystemConnectionMemo> connList = InstanceManager.getList(SystemConnectionMemo.class);
        if (connList != null) {
            reloadConnections(connList);
        } else {
            matrix.add(new JLabel("No new-form system connections configured"));
        }
    }

    void reloadConnections(List<SystemConnectionMemo> connList) {
        matrix.setLayout(new GridLayout2(connList.size() + 1, ManagerDefaultSelector.instance.knownManagers.length + 1));
        matrix.add(new JLabel(""));

        for (ManagerDefaultSelector.Item item : ManagerDefaultSelector.instance.knownManagers) {
            matrix.add(new JLabel(item.typeName));
        }
        groups = new ButtonGroup[ManagerDefaultSelector.instance.knownManagers.length];
        for (int i = 0; i < ManagerDefaultSelector.instance.knownManagers.length; i++) {
            groups[i] = new ButtonGroup();
        }
        for (int x = 0; x < connList.size(); x++) {
            jmri.jmrix.SystemConnectionMemo memo = connList.get(x);
            String name = memo.getUserName();
            matrix.add(new JLabel(name));
            int i = 0;
            for (ManagerDefaultSelector.Item item : ManagerDefaultSelector.instance.knownManagers) {
                if (memo.provides(item.managerClass)) {
                    JRadioButton r = new SelectionButton(name, item.managerClass, this);
                    matrix.add(r);
                    groups[i].add(r);
                    if (x == connList.size() - 1 && ManagerDefaultSelector.instance.getDefault(item.managerClass) == null) {
                        r.setSelected(true);
                    }
                } else {
                    // leave a blank
                    JRadioButton r = new JRadioButton();
                    r.setEnabled(false);
                    matrix.add(r);
                }
                i++; //we need to increment 'i' as we are going onto the next group even if we added a blank button
            }
        }
        revalidate();

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
        // do nothing - the persistant manager will take care of this
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
        return true; // no validity checking performed
    }

    /**
     * Captive class to track changes
     */
    static class SelectionButton extends JRadioButton {

        /**
         *
         */
        private static final long serialVersionUID = -2572336492673634333L;

        SelectionButton(String name, Class<?> managerClass, ManagerDefaultsConfigPane pane) {
            super();
            this.managerClass = managerClass;
            this.name = name;

            if (name.equals(ManagerDefaultSelector.instance.getDefault(managerClass))) {
                this.setSelected(true);
            }

            addActionListener((ActionEvent e) -> {
                if (isSelected()) {
                    ManagerDefaultSelector.instance.setDefault(SelectionButton.this.managerClass, SelectionButton.this.name);
                    pane.dirty = true;
                }
            });

        }
        String name;
        Class<?> managerClass;

        @Override
        public void setSelected(boolean t) {
            super.setSelected(t);
            if (t) {
                ManagerDefaultSelector.instance.setDefault(this.managerClass, this.name);
            }
        }
    }
}
