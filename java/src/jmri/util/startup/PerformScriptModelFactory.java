package jmri.util.startup;

import javax.swing.JFileChooser;

import jmri.script.ScriptFileChooser;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = StartupModelFactory.class)
@API(status = EXPERIMENTAL)
public class PerformScriptModelFactory extends AbstractFileModelFactory {

    public PerformScriptModelFactory() {
    }

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return PerformScriptModel.class;
    }

    @Override
    public PerformScriptModel newModel() {
        return new PerformScriptModel();
    }

    @Override
    protected JFileChooser setFileChooser() {
        return new ScriptFileChooser();
    }

}
