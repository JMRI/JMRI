package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Factory to create {@link apps.startup.TriggerRouteModel} objects.
 *
 * @author Randall Wood (C) 2016
 */
@ServiceProvider(service = StartupModelFactory.class)
public class TriggerRouteModelFactory implements StartupModelFactory {

    private final static Logger log = LoggerFactory.getLogger(TriggerRouteModelFactory.class);

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return TriggerRouteModel.class;
    }

    @Override
    public String getDescription() {
        return Bundle.getMessage(this.getModelClass().getCanonicalName());
    }

    @Override
    public String getActionText() {
        return Bundle.getMessage("EditableStartupAction", this.getDescription()); // NOI18N
    }

    @Override
    public StartupModel newModel() {
        return new TriggerRouteModel();
    }

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (this.getModelClass().isInstance(model)) {
            ArrayList<String> userNames = new ArrayList<>();
            InstanceManager.getDefault(RouteManager.class).getSystemNameList().stream().forEach((systemName) -> {
                Route r = InstanceManager.getDefault(RouteManager.class).getBySystemName(systemName);
                if (r != null) {
                    String userName = r.getUserName();
                    if (userName != null && !userName.isEmpty()) {
                        userNames.add(userName);
                    }
                } else {
                    log.error("Failed to get route {}", systemName);
                }
            });
            userNames.sort(null);
            String name = (String) JOptionPane.showInputDialog(parent,
                    Bundle.getMessage("TriggerRouteModelFactory.editModel.message"), // NOI18N
                    this.getDescription(),
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    userNames.toArray(new String[userNames.size()]),
                    model.getName());
            if (name != null && !name.equals(model.getName())) {
                model.setName(name);
                InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
            }
        }
    }

    @Override
    public void initialize() {
        // nothing to do
    }
}
