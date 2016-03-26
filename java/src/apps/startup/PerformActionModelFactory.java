package apps.startup;

import apps.PerformActionModel;
import apps.StartupModel;

/**
 *
 * @author Randall Wood 2016
 */
public class PerformActionModelFactory extends AbstractActionModelFactory {

    public PerformActionModelFactory() {
    }

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return PerformActionModel.class;
    }

    @Override
    public PerformActionModel newModel() {
        return new PerformActionModel();
    }

    @Override
    public String getEditModelMessage() {
        return Bundle.getMessage("PerformActionModelFactory.editModel.message");
    }    
}
