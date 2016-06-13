package apps.startup;

import apps.StartupActionsManager;
import apps.StartupModel;
import java.awt.Component;
import javax.swing.JOptionPane;
import jmri.InstanceManager;

/**
 * Provide an abstract StartupModelFactory with common methods for factories
 * that manipulate models that extend {@link apps.AbstractActionModel}.
 *
 * @author Randall Wood
 */
abstract public class AbstractActionModelFactory implements StartupModelFactory {

    @Override
    public String getDescription() {
        return Bundle.getMessage(this.getModelClass().getCanonicalName());
    }

    @Override
    public String getActionText() {
        return Bundle.getMessage("EditableStartupAction", this.getDescription());
    }

    /**
     * Get the message text for the dialog created in
     * {@link #editModel(apps.StartupModel, java.awt.Component)}.
     *
     * @return the message text
     */
    public abstract String getEditModelMessage();

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (this.getModelClass().isInstance(model)) {
            String name = (String) JOptionPane.showInputDialog(parent,
                    this.getEditModelMessage(),
                    this.getDescription(),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    InstanceManager.getDefault(StartupActionModelUtil.class).getNames(),
                    model.getName());
            if (name != null && !name.equals(model.getName())) {
                model.setName(name);
                InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
            }
        }
    }

    @Override
    public void initialize() {
        if (InstanceManager.getDefault(StartupActionModelUtil.class) == null) {
            InstanceManager.setDefault(StartupActionModelUtil.class, new StartupActionModelUtil());
        }
    }
}
