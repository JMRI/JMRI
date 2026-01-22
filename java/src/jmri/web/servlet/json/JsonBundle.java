package jmri.web.servlet.json;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Locale;
import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@CheckReturnValue

@javax.annotation.concurrent.Immutable

/**
 * Provides standard access for resource bundles in a package.
 * <p>
 * Convention is to provide a subclass of this name in each package, working off
 * the local resource bundle name.
 * <p>
 * This instance is named differently than usual since two Bundle classes exist
 * for this package: one that inherits from jmri.web.servlet and one that
 * inherits from jmri.server.json
 *
 * @author Bob Jacobsen Copyright (C) 2012
 * @since 3.3.1
 */
@SuppressFBWarnings(value = {"HSM_HIDING_METHOD"},
    justification = "Desired pattern is repeated classes with package-level access to members")
public class JsonBundle extends jmri.server.json.Bundle {

    @CheckForNull
    private static final String name = null; // NOI18N

    //
    // below here is boilerplate to be copied exactly
    //
    /**
     * Provides a translated string for a given key from the package resource
     * bundle or parent.
     * <p>
     * Note that this is intentionally package-local access.
     *
     * @param key Bundle key to be translated
     * @return Internationalized text
     */
    static String getMessage(String key) {
        return getBundle().handleGetMessage(key);
    }

    /**
     * Provides a translated string for a given key in a given locale from the
     * package resource bundle or parent.
     * <p>
     * Note that this is intentionally package-local access.
     *
     * @param locale The locale to be used
     * @param key    Bundle key to be translated
     * @return Internationalized text
     */
    static String getMessage(Locale locale, String key) {
        return getBundle().handleGetMessage(locale, key);
    }

    /**
     * Merges user data with a translated string for a given key from the
     * package resource bundle or parent.
     * <p>
     * Uses the transformation conventions of the Java MessageFormat utility.
     * <p>
     * Note that this is intentionally package-local access.
     *
     * @see java.text.MessageFormat
     * @param key  Bundle key to be translated
     * @param subs One or more objects to be inserted into the message
     * @return Internationalized text
     */
    static String getMessage(String key, Object... subs) {
        return getBundle().handleGetMessage(key, subs);
    }

    /**
     * Merges user data with a translated string for a given key in a given
     * locale from the package resource bundle or parent.
     * <p>
     * Uses the transformation conventions of the Java MessageFormat utility.
     * <p>
     * Note that this is intentionally package-local access.
     *
     * @see java.text.MessageFormat
     * @param locale The locale to be used
     * @param key    Bundle key to be translated
     * @param subs   One or more objects to be inserted into the message
     * @return Internationalized text
     */
    static String getMessage(Locale locale, String key, Object... subs) {
        return getBundle().handleGetMessage(locale, key, subs);
    }

    private final static JsonBundle b = new JsonBundle();

    @Override
    @CheckForNull
    protected String bundleName() {
        return name;
    }

    protected static jmri.Bundle getBundle() {
        return b;
    }

    @Override
    protected String retry(Locale locale, String key) {
        return super.getBundle().handleGetMessage(locale, key);
    }

}
