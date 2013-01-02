// Bundle.java

package jmri.jmrit;

/**
 * Provides standard access for resource bundles in a package.
 * 
 * Convention is to provide a subclass of this name
 * in each package, working off the local resource bundle name.
 *
 * @author      Bob Jacobsen  Copyright (C) 2012
 * @version     $Revision: 17977 $
 * @since       3.3.1
 */
public class Bundle extends jmri.Bundle {

    protected static Bundle b = new Bundle("jmri.jmrit.Bundle"); // NOI18N

    //
    // below here is boilerplate to be copied exactly
    //
    
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
   
    protected Bundle(String name) { this.name = name;}   
    protected Bundle() {} 
    @Override
    protected jmri.Bundle getBundle() { return b; }
    @Override
    protected String bundleName() {return name; }
    @Override
    protected String retry(String key) { return super.getBundle().handleGetString(key); }
    private String name;
}

/* @(#)Bundle.java */
