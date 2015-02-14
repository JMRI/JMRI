// PerformActionPanel.java
package apps;

import java.util.List;

/**
 * Provide a GUI for configuring PerformActionModel objects.
 * <P>
 * A PerformModel object invokes a Swing Action when the program is started.
 * <P>
 *
 * <P>
 * @author	Bob Jacobsen Copyright 2003
 * @version $Revision$
 * @see apps.PerformActionModel
 */
public class PerformActionPanel extends AbstractActionPanel {

    /**
     *
     */
    private static final long serialVersionUID = 8339294658454017607L;

    public PerformActionPanel() {
        super("ButtonActionAdd", "ButtonActionRemove");
    }

    @Override
    List<PerformActionModel> rememberedObjects() {
        return PerformActionModel.rememberedObjects();
    }

    @Override
    AbstractActionModel getNewModel() {
        return new PerformActionModel();
    }

    @Override
    public String getTabbedPreferencesTitle() {
        return rb.getString("TabbedLayoutStartupActions"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return rb.getString("LabelTabbedLayoutStartupActions"); // NOI18N
    }
}
