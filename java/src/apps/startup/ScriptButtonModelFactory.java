package apps.startup;

import java.awt.Component;
import java.io.File;
import javax.swing.JFileChooser;

import jmri.InstanceManager;
import jmri.script.swing.ScriptFileChooser;
import jmri.util.startup.StartupModelFactory;
import jmri.util.startup.StartupModel;
import jmri.util.startup.StartupActionsManager;
import jmri.util.swing.JmriJOptionPane;

import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Randall Wood 2016
 */
@ServiceProvider(service = StartupModelFactory.class)
public class ScriptButtonModelFactory implements StartupModelFactory {

    private ScriptFileChooser chooser = null;

    public ScriptButtonModelFactory() {
    }

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return ScriptButtonModel.class;
    }

    @Override
    public ScriptButtonModel newModel() {
        return new ScriptButtonModel();
    }

    @Override
    public String getDescription() {
        return Bundle.getMessage(this.getModelClass().getCanonicalName());
    }

    @Override
    public String getActionText() {
        return Bundle.getMessage("EditableStartupAction", this.getDescription()); // NOI18N
    }

    @Override
    public void editModel(StartupModel model, Component parent) {
        if (this.chooser == null) {
            this.chooser = new ScriptFileChooser();
            this.chooser.setDialogTitle(this.getDescription());
            this.chooser.setDialogType(JFileChooser.CUSTOM_DIALOG);
        }
        if (ScriptButtonModel.class.isInstance(model)) {
            ScriptButtonPanel buttonPanel = new ScriptButtonPanel(this.chooser, parent);
            if (((ScriptButtonModel) model).getScript() != null) {
                buttonPanel.setScript(((ScriptButtonModel) model).getScript().getPath());
            } else {
                buttonPanel.setScript(""); // NOI18N
            }
            buttonPanel.setButtonName(model.getName());
            JmriJOptionPane.showMessageDialog(parent,
                    buttonPanel,
                    this.getDescription(),
                    JmriJOptionPane.PLAIN_MESSAGE);
            if (!buttonPanel.getButtonName().isEmpty()) {
                model.setName(buttonPanel.getButtonName());
                ((ScriptButtonModel) model).setScript(new File(buttonPanel.getScript()));
                InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
            }
        }
    }

    @Override
    public void initialize() {
        // nothing to do
    }

}
