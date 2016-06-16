package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import jmri.InstanceManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.swing.SystemConnectionAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract StartupModelFactory with common methods for factories
 * that manipulate models that extend {@link apps.AbstractActionModel}.
 *
 * @author Randall Wood
 */
abstract public class AbstractActionModelFactory implements StartupModelFactory {

    private final static Logger log = LoggerFactory.getLogger(AbstractActionModelFactory.class);

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
            JComboBox<String> connections = new JComboBox<>();
            JPanel message = this.getDialogMessage(actions, connections);
            actions.setSelectedValue(model.getName(), true);
            String systemName = ((AbstractActionModel) model).getSystemPrefix();
            if (!systemName.isEmpty()) {
                connections.setSelectedItem(systemName);
            }
            int result = JOptionPane.showOptionDialog(parent,
                    message,
                    this.getDescription(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    null);
            if (result == JOptionPane.OK_OPTION) {
                String name = actions.getSelectedValue();
                if (!name.equals(model.getName())) {
                    model.setName(name);
                    InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
                }
                if ((systemName.isEmpty() && connections.getSelectedItem() != null)
                        || !systemName.equals(connections.getSelectedItem())) {
                    ((AbstractActionModel) model).setSystemPrefix((String) connections.getSelectedItem());
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

    private JPanel getDialogMessage(JList<String> actions, JComboBox<String> connections) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.add(new JLabel(this.getEditModelMessage()));
        panel.add(new JScrollPane(actions));
        panel.add(connections);
        actions.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            connections.removeAllItems();
            String name = InstanceManager.getDefault(StartupActionModelUtil.class).getNames()[e.getFirstIndex()];
            if (name != null) {
                String className = InstanceManager.getDefault(StartupActionModelUtil.class).getClassName(name);
                if (className != null && InstanceManager.getDefault(StartupActionModelUtil.class).isSystemConnectionAction(className)) {
                    try {
                        Action action = (Action) Class.forName(className).newInstance();
                        if (SystemConnectionAction.class.isAssignableFrom(action.getClass())) {
                            ((SystemConnectionAction) action).getSystemConnectionMemoClasses().stream().forEach((clazz) -> {
                                InstanceManager.getList(SystemConnectionMemo.class).stream().forEach((memo) -> {
                                    if (clazz.isAssignableFrom(memo.getClass())) {
                                        connections.addItem(memo.getSystemPrefix());
                                    }
                                });
                            });
                        }
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        log.error("Unable to create Action", ex);
                    }
                }
            }
            connections.setEnabled(connections.getItemCount() != 0);
        });
        return panel;
    }
}
