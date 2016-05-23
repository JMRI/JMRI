package jmri.jmrit.operations.setup;

import javax.swing.JComponent;
import jmri.jmrit.operations.OperationsPanel;
import jmri.swing.PreferencesPanel;

/**
 *
 * @author Randall Wood {@literal <randall.h.wood@alexandriasoftware.com>}
 */
public abstract class OperationsPreferencesPanel extends OperationsPanel implements PreferencesPanel {

    @Override
    public String getPreferencesItem() {
        return "OPERATIONS"; // NOI18N
    }

    @Override
    public String getPreferencesItemText() {
        return Bundle.getMessage("MenuOperations"); // NOI18N
    }

    @Override
    public String getLabelKey() {
        return null;
    }

    @Override
    public JComponent getPreferencesComponent() {
        return this;
    }

    @Override
    public boolean isPersistant() {
        return false;
    }

    @Override
    public boolean isRestartRequired() {
        return false;
    }

    @Override
    public boolean isPreferencesValid() {
        return true; // no validity checking performed
    }
}
