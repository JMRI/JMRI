package jmri.swing;

import java.util.List;

/**
 * A {@link jmri.swing.PreferencesPanel} that manages other PreferencesPanels
 * within its own panel.
 *
 * @author Randall Wood 2015
 */
public interface ManagingPreferencesPanel extends PreferencesPanel {

    public abstract List<PreferencesPanel> getPreferencesPanels();
}
