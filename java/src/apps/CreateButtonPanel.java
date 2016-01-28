// CreateButtonPanel.java
package apps;

import java.util.List;
import jmri.InstanceManager;

/**
 * Provide a GUI for configuring start-up actions.
 * <P>
 * Configures CreateButtonModel objects. A CreateButtonModel object creates
 * appropriate buttons when the program is started.
 *
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 * @see apps.CreateButtonModel
 * @deprecated Replaced by {@link apps.startup.CreateButtonModelFactory}
 */
@Deprecated
public class CreateButtonPanel extends AbstractActionPanel {

    /**
     *
     */
    private static final long serialVersionUID = 1652489978153384059L;

    public CreateButtonPanel() {
        super("ButtonButtonAdd", "ButtonButtonRemove");
    }

    @Override
    List<CreateButtonModel> rememberedObjects() {
        return InstanceManager.getDefault(StartupActionsManager.class).getActions(CreateButtonModel.class);
    }

    @Override
    AbstractActionModel getNewModel() {
        return new CreateButtonModel();
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rb.getString("TabbedLayoutCreateButton"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rb.getString("LabelTabbedLayoutCreateButton"); // NOI18N
    }
}
