// Bundle.java

package jmri;

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

    private static Bundle b = new Bundle("jmri.NamedBeanBundle");  // NOI18N

    /**
     * Provides access to a string for a given 
     * key from the package resource bundle or 
     * parent.
     *<p>
     * Note that this is intentionally package-local
     * access.
     */
    static String getString(String key) {
        return b.handleGetString(key);
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
    
    // the following is different from the method in subclasses because
    // this is the root of the search tree
    protected String retry(String key) { throw new java.util.MissingResourceException("Resource not found", this.getClass().toString(), key); } // NOI18N
    protected Bundle(String name) { this.name = name;}   
    protected Bundle() { this.name = null;} 
    protected jmri.Bundle getBundle() { return b; }
    protected String bundleName() {return name; }
    private String name;
    
}

/* @(#)Bundle.java */