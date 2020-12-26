package apps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * A PerformFileModel object loads an xml file when the program is started.
 *
 * @author Bob Jacobsen Copyright 2003
 * @author Randall Wood (c) 2016
 * @see jmri.util.startup.PerformFileModelFactory
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.21.1; use {@link jmri.util.startup.PerformFileModel} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public class PerformFileModel extends jmri.util.startup.PerformFileModel {
}
