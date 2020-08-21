package apps.gui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.19.6; use {@link jmri.util.gui.GuiLafPreferencesManager}
 * instead
 */
@Deprecated // retain until no longer allowing migratation of GUI preferences from
// apps-gui to jmri-util-gui in profile/profile.properties
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public class GuiLafPreferencesManager extends jmri.util.gui.GuiLafPreferencesManager {
}
