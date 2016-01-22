package apps.startup;

import apps.StartupModel;
import java.awt.Component;
import java.io.IOException;
import javax.swing.JFileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide an abstract StartupModelFactory with common methods for factories
 * that manipulate models that open files.
 *
 * @author Randall Wood
 */
public abstract class AbstractFileModelFactory implements StartupModelFactory {

    private JFileChooser chooser = null;
    private final static Logger log = LoggerFactory.getLogger(AbstractFileModelFactory.class);

    @Override
    public String getDescription() {
        return Bundle.getMessage(this.getModelClass().getCanonicalName());
    }

    abstract protected JFileChooser setFileChooser();

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (this.getModelClass().isInstance(model)) {
            if (this.chooser == null) {
                this.chooser = this.setFileChooser();
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
}
