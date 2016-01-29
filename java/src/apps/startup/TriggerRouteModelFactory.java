package apps.startup;

import apps.StartupModel;
import java.awt.Component;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.RouteManager;

/**
 * Factory to create {@link apps.startup.TriggerRouteModel} objects.
 * 
 * @author Randall Wood (C) 2016
 */
public class TriggerRouteModelFactory implements StartupModelFactory {

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
                String userName = InstanceManager.getDefault(RouteManager.class).getBySystemName(systemName).getUserName();
                if (userName != null && !userName.isEmpty()) {
                    userNames.add(userName);
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
            }
        }
    }

}
