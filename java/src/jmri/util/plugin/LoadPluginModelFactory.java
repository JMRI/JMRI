package jmri.util.plugin;

import javax.swing.JFileChooser;

import jmri.util.startup.AbstractFileModelFactory;
import jmri.util.startup.StartupModel;
import jmri.util.startup.StartupModelFactory;

import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for LoadPluginModel.
 *
 * @author Randall Wood (C) 2016
 * @author Daniel Bergqvist (C) 2023
 */
@ServiceProvider(service = StartupModelFactory.class)
public class LoadPluginModelFactory extends AbstractFileModelFactory {

    public LoadPluginModelFactory() {
    }

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return LoadPluginModel.class;
    }

    @Override
    public LoadPluginModel newModel() {
        return new LoadPluginModel();
    }

    @Override
    protected JFileChooser setFileChooser() {
        return new PluginFileChooser();
    }

    @Override
    public String getDescription() {
        return Bundle.getMessage(this.getModelClass().getCanonicalName());
    }
}
