package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.Optional;
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
import jmri.util.ConnectionNameFromSystemName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract StartupModelFactory with common methods for factories
 * that manipulate models that extend {@link apps.startup.AbstractActionModel}.
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
     * {@link #editModel(apps.startup.StartupModel, java.awt.Component)}.
     *
     * @return the message text
     */
    public abstract String getEditModelMessage();

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (model instanceof AbstractActionModel && this.getModelClass().isInstance(model)) {
            JList<String> actions = new JList<>(StartupActionModelUtil.getDefault().getNames());
            JComboBox<String> connections = new JComboBox<>();
            JPanel message = this.getDialogMessage(actions, connections);
            actions.setSelectedValue(model.getName(), true);
            String userName = ConnectionNameFromSystemName.getConnectionName(((AbstractActionModel) model).getSystemPrefix());
            if (userName == null) {
                userName = ""; // make not null to simplify following conditionals
            }
            if (!userName.isEmpty()) {
                connections.setSelectedItem(userName);
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
                Optional<StartupActionsManager> manager = InstanceManager.getOptionalDefault(StartupActionsManager.class);
                if (!name.equals(model.getName())) {
                    model.setName(name);
                    manager.ifPresent(sam -> {
                        sam.setRestartRequired();
                    });
                }
                if (((userName.isEmpty() && connections.getSelectedItem() != null))
                        || !userName.equals(connections.getSelectedItem())) {
                    ((AbstractActionModel) model).setSystemPrefix(ConnectionNameFromSystemName.getPrefixFromName((String) connections.getSelectedItem()));
                    manager.ifPresent(sam -> {
                        sam.setRestartRequired();
                    });
                }
            }
        }
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    private JPanel getDialogMessage(JList<String> actions, JComboBox<String> connections) {
        JLabel connectionsLabel = new JLabel(Bundle.getMessage("AbstractActionModelFactory.getDialogMessage.connectionsLabel", JLabel.LEADING)); // NOI18N
        actions.getSelectionModel().addListSelectionListener((ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                connections.removeAllItems();
                connections.setEnabled(false);
                connectionsLabel.setEnabled(false);
                String name = actions.getSelectedValue();
                if (name != null) {
                    String className = StartupActionModelUtil.getDefault().getClassName(name);
                    if (className != null && StartupActionModelUtil.getDefault().isSystemConnectionAction(className)) {
                        try {
                            Action action = (Action) Class.forName(className).getDeclaredConstructor().newInstance();
                            if (SystemConnectionAction.class.isAssignableFrom(action.getClass())) {
                                ((SystemConnectionAction) action).getSystemConnectionMemoClasses().stream().forEach((clazz) -> {
                                    InstanceManager.getList(SystemConnectionMemo.class).stream().forEach((memo) -> {
                                        if (clazz.isAssignableFrom(memo.getClass())) {
                                            connections.addItem(memo.getUserName());
                                            connections.setEnabled(true);
                                            connectionsLabel.setEnabled(true);
                                        }
                                    });
                                });
                            }
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException ex) {
                            log.error("Unable to create Action", ex);
                        }
                    }
                }
            }
        });
        connections.setEnabled(false);
        connectionsLabel.setEnabled(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
        panel.add(new JLabel(this.getEditModelMessage(), JLabel.LEADING));
        panel.add(new JScrollPane(actions));
        panel.add(connectionsLabel);
        panel.add(connections);
        return panel;
    }
}
