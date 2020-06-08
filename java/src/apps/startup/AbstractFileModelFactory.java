package apps.startup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provide an abstract StartupModelFactory with common methods for factories
 * that manipulate models that open files.
 *
 * @author Randall Wood
 * @deprecated since 4.21.1; use {@link jmri.util.startup.AbstractFileModelFactory} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public abstract class AbstractFileModelFactory extends jmri.util.startup.AbstractFileModelFactory {
}
