package jmri.web.server;

/**
 * @author Steve Todd Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2012, 2014, 2016
 */
import apps.PerformActionModel;
import apps.StartupActionsManager;
import java.awt.event.ActionEvent;
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
import org.jdesktop.beansbinding.BindingGroup;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service = PreferencesPanel.class)
public class WebServerPreferencesPanel extends JPanel implements PreferencesPanel {

    private JSpinner port;
    private JCheckBox readonlyPower;
    private JLabel portLabel;
    private final WebServerPreferences preferences;
    private JCheckBox startup;
    private ItemListener startupItemListener;
    private int startupActionPosition = -1;
    private BindingGroup bindingGroup;

    public WebServerPreferencesPanel() {
        preferences = InstanceManager.getDefault(WebServerPreferences.class);
        initComponents();
    }

    private void initComponents() {
        bindingGroup = new BindingGroup();
        port = new JSpinner();
        portLabel = new JLabel();
        readonlyPower = new JCheckBox();
        startup = new JCheckBox();

        port.setModel(new SpinnerNumberModel(12080, 1, 65535, 1));
        port.setEditor(new JSpinner.NumberEditor(port, "#"));
        port.setToolTipText(Bundle.getMessage("ToolTipPort")); // NOI18N

        bindingGroup.addBinding(Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, preferences, ELProperty.create("${port}"), port, BeanProperty.create("value")));

        portLabel.setText(Bundle.getMessage("LabelPort")); // NOI18N
        portLabel.setToolTipText(Bundle.getMessage("ToolTipPort")); // NOI18N

        readonlyPower.setText(Bundle.getMessage("LabelReadonlyPower")); // NOI18N
        readonlyPower.addActionListener((ActionEvent e) -> {
            readonlyPower.setToolTipText(Bundle.getMessage(readonlyPower.isSelected() ? "ToolTipReadonlyPowerTrue" : "ToolTipReadonlyPowerFalse"));
        });

        bindingGroup.addBinding(Bindings.createAutoBinding(AutoBinding.UpdateStrategy.READ_WRITE, preferences, ELProperty.create("${readonlyPower}"), readonlyPower, BeanProperty.create("selected")));

        startup.setSelected(this.isStartupAction());
        startup.setText(Bundle.getMessage("LabelStartup")); // NOI18N
        this.startupItemListener = (ItemEvent e) -> {
            this.startup.removeItemListener(this.startupItemListener);
            StartupActionsManager manager = InstanceManager.getDefault(StartupActionsManager.class);
            if (this.startup.isSelected()) {
                PerformActionModel model = new PerformActionModel();
                model.setClassName(WebServerAction.class.getName());
                if (this.startupActionPosition == -1 || this.startupActionPosition >= manager.getActions().length) {
                    manager.addAction(model);
                } else {
                    manager.setActions(this.startupActionPosition, model);
                }
            } else {
                manager.getActions(PerformActionModel.class).stream().filter((model) -> (WebServerAction.class.getName().equals(model.getClassName()))).forEach((model) -> {
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
                                .addComponent(startup, GroupLayout.DEFAULT_SIZE, 388, Short.MAX_VALUE)
                                .addComponent(readonlyPower, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(port, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(portLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(readonlyPower)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(startup)
                        .addContainerGap(198, Short.MAX_VALUE))
        );

        bindingGroup.bind();
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
        this.preferences.save();
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
        return true; // no validity checking performed
    }

    @Override
    public int getSortOrder() {
        return 1100;
    }
    
    private boolean isStartupAction() {
        return InstanceManager.getDefault(StartupActionsManager.class).getActions(PerformActionModel.class).stream()
                .anyMatch((model) -> (WebServerAction.class.getName().equals(model.getClassName())));
    }
}
