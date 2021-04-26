package apps.startup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.19.6; use {@link jmri.util.startup.StartupModel} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_INTERFACE", justification = "Deprecated by refactoring; retaining unchanged until removal")
public interface StartupModel extends jmri.util.startup.StartupModel {
    
}
