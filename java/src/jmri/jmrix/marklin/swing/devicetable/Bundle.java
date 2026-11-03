package jmri.jmrix.marklin.swing.devicetable;

import java.util.Locale;
import javax.annotation.CheckForNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Package-local access to the parent Marklin resource bundle.
 */
@SuppressFBWarnings(value = {"NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", "HSM_HIDING_METHOD"},
        justification = "Standard JMRI package-local bundle access")
public class Bundle extends jmri.jmrix.marklin.Bundle {

    static String getMessage(String key) {
        return getBundle().handleGetMessage(key);
    }

    static String getMessage(String key, Object... subs) {
        return getBundle().handleGetMessage(key, subs);
    }

    static String getMessage(Locale locale, String key, Object... subs) {
        return getBundle().handleGetMessage(locale, key, subs);
    }

    private static final Bundle b = new Bundle();

    @Override
    @CheckForNull
    protected String bundleName() {
        return null;
    }

    protected static jmri.Bundle getBundle() {
        return b;
    }

    @Override
    protected String retry(Locale locale, String key) {
        return super.getBundle().handleGetMessage(locale, key);
    }
}
