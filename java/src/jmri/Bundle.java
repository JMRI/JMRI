// Bundle.java

package jmri;

import edu.umd.cs.findbugs.annotations.CheckReturnValue;
import edu.umd.cs.findbugs.annotations.DefaultAnnotation;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

@DefaultAnnotation({NonNull.class, CheckReturnValue.class})

@net.jcip.annotations.Immutable

/**
 * Provides standard access for resource bundles in a package.
 * 
 * Convention pattern is to provide a subclass of the same name
 * in each package, working off the local resource bundle name.
 * <p>
 * This is the root of a tree of classes that are chained 
 * through class-static members so that they each do a search
 * as a request works up the inheritance tree.
 *<p>
 * Only a single package-scope method is exposed from the class, 
 * forcing all requests for strings to be a the package level.
 *<p>
 * To add this to a new package, copy exactly a 
 * subclass file such as jmri.jmrit.Bundle, and change three places:
 *<OL>
 *<li>The import statement at the top
 *<li>The extends clause in the class definition statement
 *<li>The resource pathname assigned to the name variable, which
 *     must be set to null if there are no local resources.
 *</ol>
 *
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
 *
 * @author      Bob Jacobsen  Copyright (C) 2012
 * @version     $Revision: 17977 $
 * @since       3.3.1
 */
public class Bundle {

    private final static String name = "jmri.NamedBeanBundle";  // NOI18N

    /**
     * Provides access to a string for a given 
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Note that this is intentionally package-local
     * access.
     * @param key Bundle key to be translated
     * @return Internationalized text
     */
    static String getString(String key) {
        return b.handleGetString(key);
    }
    /**
     * Provides access to a string for a given 
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Note that this is intentionally package-local
     * access.
     * @param key Bundle key to be translated
     * @return Internationalized text
     */
    static String getMessage(String key) {
        return b.handleGetMessage(key);
    }
    /**
     * Merges user data with a string for a given 
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Note that this is intentionally package-local
     * access.
     * @param key Bundle key to be translated
     * @param subs One or more objects to be inserted into the message
     * @return Internationalized text
     */
    static String getMessage(String key, Object ... subs) {
        return b.handleGetMessage(key, subs);
    }

   /**
     * This method handles the inheritance tree.
     * At lower levels, it reflects upwards on failure.  Once
     * it reaches this root class, it will
     * throw a MissingResourceException in the key can't be found
     * via the local definition of retry().
     *
     * @throws MissingResourceException
     */
    public String handleGetString(String key) {
        if (bundleName() != null) {
            java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle(bundleName());
            if (rb.containsKey(key)) return rb.getString(key);
            else return retry(key);
        } else {  // case of no local bundle
            return retry(key);
        }
    }
    
    public String handleGetMessage(String key) {
        return handleGetString(key);
    }
    
    public String handleGetMessage(String key, Object[] subs) {
        return java.text.MessageFormat.format(handleGetString(key), subs);
    }
    
    // the following is different from the method in subclasses because
    // this is the root of the search tree
    protected String retry(String key) { throw new java.util.MissingResourceException("Resource not found", this.getClass().toString(), key); } // NOI18N

    private final static Bundle b = new Bundle();
    @Nullable protected String bundleName() {return name; }
    protected jmri.Bundle getBundle() { return b; }
    
    // Can get pathname of ctor class (to auto-generate BundleName) via getClass().getPackage()
}

/* @(#)Bundle.java */
