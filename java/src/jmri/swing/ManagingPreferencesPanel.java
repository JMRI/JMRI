package jmri.swing;

import java.util.List;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

/**
 * A {@link jmri.swing.PreferencesPanel} that manages other PreferencesPanels
 * within its own panel.
 *
 * @author Randall Wood 2015
 */
@API(status = EXPERIMENTAL)
public interface ManagingPreferencesPanel extends PreferencesPanel {

    public abstract List<PreferencesPanel> getPreferencesPanels();
}
