// Bundle.java

package jmri.jmrit;

import java.util.ResourceBundle;

/**
 * Provides standard access for resource bundles in a package.
 * 
 * Convention pattern is to provide a subclass of this name
 * in each package, working off the local resource bundle name.
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
public class Bundle extends jmri.Bundle {

    protected static Bundle b = new Bundle("jmri.jmrit.JmritToolsBundle");

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