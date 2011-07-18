// CreateButtonPanel.java

package apps;

import java.util.List;

/**
 * Provide a GUI for configuring start-up actions.
 * <P>Configures CreateButtonModel objects.
 * A CreateButtonModel object creates appropriate buttons when the
 * program is started.
 *
 * <P>
 * @author	Bob Jacobsen   Copyright 2003
 * @version     $Revision$
 * @see apps.CreateButtonModel
 */
public class CreateButtonPanel extends AbstractActionPanel {

    public CreateButtonPanel() {
        super("ButtonButtonAdd", "ButtonButtonRemove");
    }
    List<CreateButtonModel> rememberedObjects() { return CreateButtonModel.rememberedObjects(); }
    AbstractActionModel getNewModel(){ return new CreateButtonModel(); }
}


