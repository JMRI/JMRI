package jmri;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javax.annotation.CheckReturnValue;
import javax.annotation.CheckForNull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Provides standard access for resource bundles in a package.
 * <p>
 * Convention is to provide a subclass of this same name in each package,
 * working off the local resource bundle name, usually 'package.Bundle' stored
 * in a Bundle.properties file.
 * <p>
 * This is the root of a tree of classes that are chained through class-static
 * members so that they each do a search as a request works up the inheritance
 * tree.
 * <p>
 * Only package-scope methods exposed are from the class, forcing all requests
 * for strings to be a the package level.
 * <p>
 * To add this to a new package, copy exactly a subclass file such as
 * jmri.jmrit.Bundle, and change three places:
 * <ol>
 * <li>The import statement at the top
 * <li>The extends clause in the class definition statement
 * <li>The resource pathname assigned to the name variable, which must be set to
 * null if there are no local resources.
 * </ol>
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <p>
 *
 * @author Bob Jacobsen Copyright (C) 2012
 * @since 3.3.1
 */
@ParametersAreNonnullByDefault
@CheckReturnValue
@javax.annotation.concurrent.Immutable
public class Bundle {

    @CheckForNull
    private final static String name = "jmri.NamedBeanBundle";  // NOI18N

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

    /**
     * This method handles the inheritance tree. At lower levels, it reflects
     * upwards on failure. Once it reaches this root class, it will throw a
     * MissingResourceException in the key can't be found via the local
     * definition of retry().
     *
     * @param key Bundle key to be translated
     * @return Internationalized text
     * @throws MissingResourceException if message cannot be found
     */
    public String handleGetMessage(String key) {
        return this.handleGetMessage(Locale.getDefault(), key);
    }

    /**
     * This method handles the inheritance tree. At lower levels, it reflects
     * upwards on failure. Once it reaches this root class, it will throw a
     * MissingResourceException in the key can't be found via the local
     * definition of retry().
     *
     * @param locale The locale to be used
     * @param key    Bundle key to be translated
     * @return Internationalized text
     * @throws MissingResourceException if message cannot be found
     */
    public String handleGetMessage(Locale locale, String key) {
        log.trace("handleGetMessage for key {}", key);
        if (bundleName() != null) {
            ResourceBundle rb = ResourceBundle.getBundle(bundleName(), locale);
            if (rb.containsKey(key)) {
                return rb.getString(key);
            } else {
                return retry(locale, key);
            }
        } else {  // case of no local bundle
            return retry(locale, key);
        }
    }

    /**
     * Merges user data with a translated string for a given key from the
     * package resource bundle or parent.
     * <p>
     * Uses the transformation conventions of the Java MessageFormat utility.
     *
     * @see java.text.MessageFormat
     * @param key  Bundle key to be translated
     * @param subs Array of objects to be inserted into the message
     * @return Internationalized text
     */
    public String handleGetMessage(String key, Object[] subs) {
        return this.handleGetMessage(Locale.getDefault(), key, subs);
    }

    /**
     * Merges user data with a translated string for a given key in a given
     * locale from the package resource bundle or parent.
     * <p>
     * Uses the transformation conventions of the Java MessageFormat utility.
     *
     * @see java.text.MessageFormat
     * @param locale The locale to be used
     * @param key    Bundle key to be translated
     * @param subs   Array of objects to be inserted into the message
     * @return Internationalized text
     */
    public String handleGetMessage(Locale locale, String key, Object[] subs) {
        return MessageFormat.format(handleGetMessage(locale, key), subs);
    }

    // the following is different from the method in subclasses because
    // this is the root of the search tree
    protected String retry(Locale locale, String key) throws MissingResourceException {
        throw new MissingResourceException("Resource '" + key + "' not found", this.getClass().toString(), key); // NOI18N
    }

    private final static Bundle b = new Bundle();

    @CheckForNull
    protected String bundleName() {
        return name;
    }

    protected static jmri.Bundle getBundle() {
        return b;
    }

    // Can get pathname of ctor class (to auto-generate BundleName) via getClass().getPackage()
    // E.g. to cache a local bundle name via weak reference
    //        if (rbr == null) rbr = new java.lang.ref.SoftReference<ResourceBundle>(
    //                                   ResourceBundle.getBundle("jmri.NamedBeanBundle"));
    //        ResourceBundle rb = rbr.get();
    //        if (rb == null) {
    //           log.error("Failed to load defaults because of missing bundle");
    //           return;
    //        }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Bundle.class);

}
