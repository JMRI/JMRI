package apps.startup;

import apps.PerformScriptModel;
import apps.StartupModel;
import java.awt.Component;
import java.io.IOException;
import javax.swing.JFileChooser;
import jmri.script.ScriptFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class PerformScriptModelFactory implements StartupModelFactory {

    private JFileChooser chooser = null;
    private final static Logger log = LoggerFactory.getLogger(PerformScriptModelFactory.class);

    public PerformScriptModelFactory() {
    }

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return PerformScriptModel.class;
    }

    @Override
    public String getDescription() {
        return Bundle.getMessage(this.getModelClass().getCanonicalName());
    }

    @Override
    public PerformScriptModel newModel() {
        return new PerformScriptModel();
    }

    @Override
    public void editModel(StartupModel model) {
        this.editModel(model, null);
    }

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (this.chooser == null) {
            this.chooser = new ScriptFileChooser();
        }
        if (this.chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            try {
                if (model.getName() == null || !model.getName().equals(this.chooser.getSelectedFile().getCanonicalPath())) {
                    model.setName(this.chooser.getSelectedFile().getCanonicalPath());
                }
            } catch (IOException ex) {
                log.error("File {} does not exist.", this.chooser.getSelectedFile());
            }
        }
    }
    
}
