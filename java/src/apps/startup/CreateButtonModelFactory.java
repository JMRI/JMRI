package apps.startup;

import apps.CreateButtonModel;
import apps.StartupModel;

/**
 *
 * @author Randall Wood 2016
 */
public class CreateButtonModelFactory extends AbstractActionModelFactory {

    public CreateButtonModelFactory() {
    }

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return CreateButtonModel.class;
    }

    @Override
    public CreateButtonModel newModel() {
        return new CreateButtonModel();
    }

    @Override
    public void editModel(StartupModel model) {
        if (this.getModelClass().isInstance(model)) {
            super.editModel(model);
        }
    }

}
