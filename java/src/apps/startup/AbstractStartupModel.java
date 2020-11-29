package apps.startup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Abstract startup action model.
 *
 * @author Randall Wood (c) 2016, 2020
 * @deprecated since 4.21.1; use {@link jmri.util.startup.AbstractStartupModel} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public abstract class AbstractStartupModel extends jmri.util.startup.AbstractStartupModel {

    protected AbstractStartupModel() {
        super();
    }
}
