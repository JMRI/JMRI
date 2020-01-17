package apps.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * 
 * @author Randall Wood Copyright 2019, 2020
 * @deprecated since 4.19.3; use {@link jmri.util.gui.GuiLafPreferencesManager} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated in favor of same class in different package")
public class GuiLafPreferencesManager extends jmri.util.gui.GuiLafPreferencesManager {

    public GuiLafPreferencesManager() {
        super();
    }

}
