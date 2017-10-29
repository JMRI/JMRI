package apps.startup;

import apps.StartupActionsManager;
import java.awt.Component;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import jmri.InstanceManager;
import org.openide.util.lookup.ServiceProvider;

/**
 * Factory for {@link apps.startup.StartupPauseModel} objects.
 *
 * @author Randall Wood (c) 2016
 */
@ServiceProvider(service = StartupModelFactory.class)
public class StartupPauseFactory implements StartupModelFactory {

    @Override
    public Class<? extends StartupModel> getModelClass() {
        return StartupPauseModel.class;
    }

    @Override
    public StartupModel newModel() {
        return new StartupPauseModel();
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
        if (model instanceof StartupPauseModel && this.getModelClass().isInstance(model)) {
            int delay = ((StartupPauseModel) model).getDelay();
            SpinnerNumberModel snm = new SpinnerNumberModel(
                    delay >= 0 ? delay : StartupPauseModel.DEFAULT_DELAY,
                    0,
                    600,
                    1);
            JSpinner spinner = new JSpinner(snm);
            int result = JOptionPane.showConfirmDialog(parent,
                    this.getDialogMessage(spinner),
                    this.getDescription(),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);
            if (result == JOptionPane.OK_OPTION && delay != snm.getNumber().intValue()) {
                ((StartupPauseModel) model).setDelay(snm.getNumber().intValue());
                InstanceManager.getDefault(StartupActionsManager.class).setRestartRequired();
            }
        }
    }

    @Override
    public void initialize() {
        // nothing to do
    }

    private JPanel getDialogMessage(JSpinner spinner) {
        JPanel panel = new JPanel();
        panel.add(new JLabel(Bundle.getMessage("StartupPauseModelFactory.editModel.messagePrefix"))); // NOI18N
        panel.add(spinner);
        panel.add(new JLabel(Bundle.getMessage("StartupPauseModelFactory.editModel.messagePostfix"))); // NOI18N
        return panel;
    }

}
