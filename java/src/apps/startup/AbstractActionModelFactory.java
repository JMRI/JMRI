package apps.startup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Provide an abstract StartupModelFactory with common methods for factories
 * that manipulate models that extend {@link jmri.util.startup.AbstractActionModel}.
 *
 * @author Randall Wood
 * @deprecated since 4.21.1; use {@link jmri.util.startup.AbstractActionModelFactory} instead
 */
@Deprecated
@SuppressFBWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Deprecated by refactoring; retaining unchanged until removal")
public abstract class AbstractActionModelFactory extends jmri.util.startup.AbstractActionModelFactory {
}
