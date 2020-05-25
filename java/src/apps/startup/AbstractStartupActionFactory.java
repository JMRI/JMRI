package apps.startup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 *
 * @author Randall Wood Copyright 2020
 * @deprecated since 4.19.6; use
 * {@link jmri.util.startup.AbstractStartupActionFactory} instead
 */
@Deprecated
@SuppressWarnings("deprecation")
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public abstract class AbstractStartupActionFactory extends jmri.util.startup.AbstractStartupActionFactory implements StartupActionFactory {

}
