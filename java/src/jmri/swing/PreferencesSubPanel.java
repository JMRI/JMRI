package jmri.swing;

import org.apiguardian.api.API;
import static org.apiguardian.api.API.Status.*;

@API(status = EXPERIMENTAL)
public interface PreferencesSubPanel extends PreferencesPanel {

    public abstract String getParentClassName();

    public abstract void setParent(PreferencesPanel parent);

    public abstract PreferencesPanel getParent();
}
