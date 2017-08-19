package jmri.jmrix.loconet.loconetovertcp;

import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Arrays;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.LayoutStyle;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import jmri.swing.PreferencesPanel;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Binding;
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.openide.util.lookup.ServiceProvider;

/**
 * Provide access to the LocoNet over TCP server settings.
 *
 * @author Randall Wood (C) 2017
 */
@ServiceProvider(service = PreferencesPanel.class)
public class LnTcpPreferencesPanel extends JPanel implements PreferencesPanel {

    private JSpinner port;
    private JLabel portLabel;
    private final LnTcpPreferences preferences;
    private JCheckBox startup;
    private ItemListener startupItemListener;
    private int startupActionPosition = -1;
    private BindingGroup bindingGroup;

    public LnTcpPreferencesPanel() {
        preferences = InstanceManager.getOptionalDefault(LnTcpPreferences.class).orElseGet(() -> {
            return InstanceManager.setDefault(LnTcpPreferences.class, new LnTcpPreferences());
        });
        initComponents();
    }

    private void initComponents() {
        bindingGroup = new BindingGroup();
        port = new JSpinner();
        portLabel = new JLabel();
        startup = new JCheckBox();

        port.setModel(new SpinnerNumberModel(1234, 1, 65535, 1));
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        port.setToolTipText(Bundle.getMessage("ToolTipPort")); // NOI18N

        Binding binding = Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, preferences, ELProperty.create("${port}"), port, BeanProperty.create("value"));
        bindingGroup.addBinding(binding);

        portLabel.setText(Bundle.getMessage("LabelPort")); // NOI18N
        portLabel.setToolTipText(Bundle.getMessage("ToolTipPort")); // NOI18N

        startup.setSelected(this.isStartupAction());
        startup.setText(Bundle.getMessage("LabelStartup")); // NOI18N
        this.startupItemListener = (ItemEvent e) -> {
            this.startup.removeItemListener(this.startupItemListener);
            StartupActionsManager manager = InstanceManager.getDefault(StartupActionsManager.class);
            if (this.startup.isSelected()) {
                PerformActionModel model = new PerformActionModel();
                model.setClassName(LnTcpServerAction.class.getName());
                if (this.startupActionPosition == -1 || this.startupActionPosition >= manager.getActions().length) {
                    manager.addAction(model);
                } else {
                    manager.setActions(this.startupActionPosition, model);
                }
            } else {
                manager.getActions(PerformActionModel.class).stream().filter((model) -> (LnTcpServerAction.class.getName().equals(model.getClassName()))).forEach((model) -> {
                    this.startupActionPosition = Arrays.asList(manager.getActions()).indexOf(model);
                    manager.removeAction(model);
                });
            }
            this.startup.addItemListener(this.startupItemListener);
        };
        this.startup.addItemListener(this.startupItemListener);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(port, GroupLayout.PREFERRED_SIZE, 75, GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(portLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addComponent(startup, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE))
                        .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(port, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(portLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(startup)
                        .addContainerGap(198, Short.MAX_VALUE))
        );

        bindingGroup.bind();
    }

    private boolean isStartupAction() {
        return InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformActionModel.class).stream()
                .anyMatch((model) -> (LnTcpServerAction.class.getName().equals(model.getClassName())));
    }

    @Override
    public String getPreferencesItem() {
        return Bundle.getMessage("PreferencesItem");
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("PreferencesItemTitle");
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return Bundle.getMessage("PreferencesItemTitle");
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
        this.preferences.savePreferences();
    }

    @Override
    public boolean isDirty() {
        return this.preferences.isDirty();
    }

    @Override
    public boolean isRestartRequired() {
        return this.preferences.isRestartRequired();
    }

    @Override
    public boolean isPreferencesValid() {
        return this.preferences.isPreferencesValid();
    }

}
