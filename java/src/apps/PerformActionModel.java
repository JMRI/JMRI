package apps;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Invokes a Swing Action when the program is started.
 * <p>
 * The list of actions available is defined in the
 * {@link jmri.util.startup.AbstractActionModel} superclass.
 * <p>
 * This is a separate class, even though it has no additional behavior, so that
 * persistence systems realize the type of data being stored.
 * <p>
 * This class remains in the {@code apps} package for historical reasons related
 * to preferences storage.
 *
 * @author Bob Jacobsen Copyright 2003
 * @see jmri.util.startup.PerformActionModelFactory
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.21.1; use {@link jmri.util.startup.PerformActionModel} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public class PerformActionModel extends jmri.util.startup.PerformActionModel {

    public PerformActionModel() {
        super();
    }
}
