package jmri.jmrix.loconet.ds64;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;
import java.util.Locale;

@DefaultAnnotation({NonNull.class, CheckReturnValue.class})
@SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS",justification="Desired pattern is repeated class names with package-level access to members")

@net.jcip.annotations.Immutable

/**
 * Provides standard access for resource bundles in a package.
 * <P>
 * Convention is to provide a subclass of this name
 * in each package, working off the local resource bundle name.
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under 
 * the terms of version 2 of the GNU General Public License as published 
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License 
 * for more details.
 * <P>
 * @author      Bob Jacobsen  Copyright (C) 2012
 * @version     $Revision: 0 $
 */
public class Bundle extends jmri.jmrix.loconet.Bundle {

    @Nullable
    private static final String name = "jmri.jmrix.loconet.ds64.Bundle"; // NOI18N
    private final static Bundle b = new Bundle();
    //
    // below here is boilerplate to be copied exactly
    //
    /**
     * Provides a translated string for a given
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Note that this is intentionally package-local
     * access.
     * 
     * @param key Bundle key to be translated
     * @return Internationalized text
     */
    static String getMessage(String key) {
        return b.handleGetMessage(key);
    }
    /**
     * Merges user data with a translated string for a given
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Uses the transformation conventions of 
     * the Java MessageFormat utility.
     *<p>
     * Note that this is intentionally package-local
     * access.
     *
     * @see java.text.MessageFormat
     * @param key Bundle key to be translated
     * @param subs One or more objects to be inserted into the message
     * @return Internationalized text
     */
    static String getMessage(String key, Object ... subs) {
        return b.handleGetMessage(key, subs);
    }
    @Override@Nullable
 protected String bundleName() {return name; }

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

    @Override
    protected jmri.Bundle getBundle() {
        return b;
    }

    @Override
    protected String retry(Locale locale, String key) {
        return super.getBundle().handleGetMessage(locale,key);
    }

}
