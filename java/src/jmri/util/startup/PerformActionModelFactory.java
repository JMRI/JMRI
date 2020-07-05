package jmri.util.startup;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood 2016
 */
@ServiceProvider(service = StartupModelFactory.class)
@API(status = EXPERIMENTAL)
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
