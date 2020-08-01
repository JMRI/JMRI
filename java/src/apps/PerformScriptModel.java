package apps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A PerformScriptModel object runs a script when the program is started.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @see jmri.util.startup.PerformScriptModelFactory
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.21.1; use {@link jmri.util.startup.PerformScriptModel} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public class PerformScriptModel extends jmri.util.startup.PerformScriptModel {
}
