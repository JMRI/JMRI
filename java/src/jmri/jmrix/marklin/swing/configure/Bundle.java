
package jmri.jmrix.marklin.swing.configure;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Locale;
import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@CheckReturnValue
@SuppressFBWarnings(value = {"NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", "HSM_HIDING_METHOD"},
    justification = "Desired pattern is repeated class names with package-level access to members")

@javax.annotation.concurrent.Immutable

/**
 * Provides standard access for resource bundles in a package.
 */
public class Bundle extends jmri.jmrix.marklin.Bundle {

    @CheckForNull private static final String name = null; // No local resources

    static String getMessage(String key) {
        return getBundle().handleGetMessage(key);
    }

    static String getMessage(String key, Object ... subs) {
        return getBundle().handleGetMessage(key, subs);
    }

    static String getMessage(Locale locale, String key, Object... subs) {
        return getBundle().handleGetMessage(locale, key, subs);
    }

    private static final Bundle b = new Bundle();
    @Override @CheckForNull protected String bundleName() {return name; }
    protected static jmri.Bundle getBundle() { return b; }

    @Override
    protected String retry(Locale locale, String key) {
        return super.getBundle().handleGetMessage(locale, key);
    }
}
