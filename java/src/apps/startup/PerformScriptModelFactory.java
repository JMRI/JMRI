package apps.startup;

import apps.PerformScriptModel;
import apps.StartupModel;
import javax.swing.JFileChooser;
import jmri.script.ScriptFileChooser;

/**
 *
 * @author Randall Wood
 */
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
