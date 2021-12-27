package jmri.util.startup;

import javax.swing.JFileChooser;

import jmri.script.swing.ScriptFileChooser;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood
 */
@ServiceProvider(service = StartupModelFactory.class)
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
