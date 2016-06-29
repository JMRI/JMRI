package apps.startup;

import apps.CreateButtonModel;

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
    public String getEditModelMessage() {
        return Bundle.getMessage("CreateButtonModelFactory.editModel.message");
    }
}
