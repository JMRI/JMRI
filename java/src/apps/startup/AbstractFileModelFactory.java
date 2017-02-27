package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import java.io.IOException;
import javax.swing.JFileChooser;
import jmri.InstanceManager;
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

    /**
     * This factory simply displays a {@link javax.swing.JFileChooser} to allow
     * users to configure the action. Subclasses to performAction the correct file
 chooser by implementing this method.
     *
     * @return a configured file chooser.
     */
    abstract protected JFileChooser setFileChooser();

    @Override
    public String getActionText() {
        return Bundle.getMessage("EditableStartupAction", this.getDescription());
    }

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (this.getModelClass().isInstance(model)) {
            if (this.chooser == null) {
                this.chooser = this.setFileChooser();
                this.chooser.setDialogTitle(this.getDescription());
                this.chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
            }
            if (this.chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
                try {
                    String name = model.getName();
                    if (name == null || !name.equals(this.chooser.getSelectedFile().getCanonicalPath())) {
                        model.setName(this.chooser.getSelectedFile().getCanonicalPath());
                        InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
                    }
                } catch (IOException ex) {
                    log.error("File {} does not exist.", this.chooser.getSelectedFile());
                }
            }
        }
    }

    @Override
    public void initialize() {
        // nothing to do
    }
}
