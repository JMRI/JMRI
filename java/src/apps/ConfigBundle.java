package apps;

import java.util.Locale;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@CheckReturnValue

@javax.annotation.concurrent.Immutable

/**
 * Provides access for resource bundles in a package.
 * This version however is a very special application of that and does not follow the use of Bundle.java
 *
 * Convention is to provide a subclass of this name in each package, working off
 * the local resource bundle name.
 *
 * @author Bob Jacobsen Copyright (C) 2012
 * @since 3.3.1
 */
public class ConfigBundle extends apps.Bundle {

    private final static String name = "apps.AppsConfigBundle"; // NOI18N

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
    public static String getMessage(String key) {
        return b.handleGetMessage(key);
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
    public static String getMessage(String key, Object... subs) {
        return b.handleGetMessage(key, subs);
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
        return b.handleGetMessage(locale, key, subs);
    }

    private final static ConfigBundle b = new ConfigBundle();

    @Override
    @Nullable
    protected String bundleName() {
        return name;
    }

    @Override
    protected jmri.Bundle getBundle() {
        return b;
    }

    @Override
    protected String retry(Locale locale,String key) {
        return super.getBundle().handleGetMessage(locale,key);
    }

}
