package jmri.jmrit.logixng.startup;

import java.awt.Component;

import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Module;
import jmri.jmrit.logixng.ModuleManager;
import jmri.util.startup.StartupModelFactory;
import jmri.util.startup.StartupModel;
import jmri.util.startup.StartupActionsManager;
import jmri.util.swing.JComboBoxUtil;
import jmri.util.swing.JmriJOptionPane;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for {@link apps.startup.LogixNG_StartupPauseModel} objects.
 *
 * @author Randall Wood     (C) 2016
 * @author Daniel Bergqvist (C) 2026
 */
@ServiceProvider(service = StartupModelFactory.class)
public class LogixNG_StartupPauseFactory implements StartupModelFactory {

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return LogixNG_StartupPauseModel.class;
    }

    @Override
    public StartupModel newModel() {
        return new LogixNG_StartupPauseModel();
    }

    @Override
    public String getDescription() {
        return Bundle.getMessage("WaitForLogixNGModuleDescription");
    }

    @Override
    public String getActionText() {
        return Bundle.getMessage("WaitForLogixNGModuleText", this.getDescription()); // NOI18N
    }

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (model instanceof LogixNG_StartupPauseModel && this.getModelClass().isInstance(model)) {
            LogixNG_StartupPauseModel theModel = (LogixNG_StartupPauseModel) model;

            String moduleName = theModel.getModuleName();

            Module module = null;
            if (moduleName != null) {
                module = InstanceManager.getDefault(ModuleManager.class).getModule(moduleName);
            }

            JComboBox<ModuleItem> moduleComboBox = new JComboBox<>();
            moduleComboBox.addItem(new ModuleItem(null));
            for (Module m : InstanceManager.getDefault(ModuleManager.class).getNamedBeanSet()) {
    //            System.out.format("Root socket type: %s%n", m.getRootSocketType().getName());
                if ("DefaultFemaleDigitalExpressionSocket".equals(m.getRootSocketType().getName())) {
                    ModuleItem mi = new ModuleItem(m);
                    moduleComboBox.addItem(mi);
                    if (module == m) {
                        moduleComboBox.setSelectedItem(mi);
                    }
                }
            }
            JComboBoxUtil.setupComboBoxMaxRows(moduleComboBox);

            int result = JmriJOptionPane.showConfirmDialog(parent,
                    this.getDialogMessage(moduleComboBox),
                    this.getDescription(),
                    JmriJOptionPane.OK_CANCEL_OPTION,
                    JmriJOptionPane.PLAIN_MESSAGE);

            Module newModule = null;
            if (moduleComboBox.getSelectedIndex() != -1) {
                newModule = moduleComboBox.getItemAt(moduleComboBox.getSelectedIndex())._module;
            }
            if (result == JmriJOptionPane.OK_OPTION && module != newModule) {
                if (newModule != null) {
                    theModel.setModuleName(newModule.getDisplayName());
                } else {
                    theModel.setModuleName(null);
                }
                InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
            }
        }
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    private JPanel getDialogMessage(JComboBox<ModuleItem> comboBox) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("WaitForLogixNGModule_SelectModule"))); // NOI18N
        panel.add(comboBox);
        return panel;
    }


    private static class ModuleItem {

        private final Module _module;

        public ModuleItem(Module m) {
            _module = m;
        }

        @Override
        public String toString() {
            if (_module == null) return "";
            else return _module.getDisplayName();
        }
    }

}
