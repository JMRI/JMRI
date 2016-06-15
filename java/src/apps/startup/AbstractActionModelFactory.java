package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
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
            JList<String> actions = new JList<>(InstanceManager.getDefault(StartupActionModelUtil.class).getNames());
            actions.setSelectedValue(model.getName(), true);
            Component connections = new JLabel("FEED ME!");
            int result = JOptionPane.showConfirmDialog(parent,
                    this.getDialogMessage(actions, connections),
                    this.getDescription(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION) {
                String name = actions.getSelectedValue();
                if (!name.equals(model.getName())) {
                    model.setName(name);
                    InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
                }
            }
        }
    }

    @Override
    public void initialize() {
        if (InstanceManager.getDefault(StartupActionModelUtil.class) == null) {
            InstanceManager.setDefault(StartupActionModelUtil.class, new StartupActionModelUtil());
        }
    }

    private JPanel getDialogMessage(Component actions, Component connections) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(this.getEditModelMessage()));
        panel.add(actions);
        panel.add(connections);
        return panel;
    }
}
