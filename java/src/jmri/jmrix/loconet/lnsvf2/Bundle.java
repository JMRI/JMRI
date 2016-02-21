// Bundle.java
package jmri.jmrix.loconet.lnsvf2;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@DefaultAnnotation({NonNull.class, CheckReturnValue.class})
@SuppressWarnings(value = "NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification = "Desired pattern is repeated class names with package-level access to members")

@net.jcip.annotations.Immutable

/**
 * Provides standard access for resource bundles in a package.
 *
 * Convention is to provide a subclass of this name in each package, working off
 * the local resource bundle name.
 *
 * @author Bob Jacobsen Copyright (C) 2012
 * @version $Revision: 17977 $
 * @since 3.3.1
 */
public class Bundle extends jmri.jmrix.loconet.Bundle {

    @Nullable
    private final static String name = "jmri.jmrix.loconet.lnsvf2.LnSvF2Bundle"; // NOI18N

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
        log.debug("interpreting key "+key+" without parameters");
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
    static String getMessage(String key, Object... subs) {
        log.debug("interpreting key "+key+" with " + subs.length + " parameters");
        return b.handleGetMessage(key, subs);
    }
    private final static Bundle b = new Bundle();

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
    protected String retry(String key) {
        return super.getBundle().handleGetMessage(key);
    }
    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(LnSv2MessageContents.class.getName());

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

}

/* @(#)Bundle.java */
