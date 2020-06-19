package apps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.21.1; use {@link jmri.util.startup.StartupActionsManager}
 * instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public class StartupActionsManager extends jmri.util.startup.StartupActionsManager {

}
