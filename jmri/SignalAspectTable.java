// SignalAspectTable.java

package jmri;

/**
 * Represents the properties of various signal aspects.
 *<p>
 * At present, the signal aspects are denumerated by Strings,
 * not by specific objects; this table exists to attach properties
 * to those Strings.
 * <p>
 * Setting or getting the "state" of one of these will throw an error.
 * <p>
 * You'll have one of these objects for each signaling _system_ on
 * your railroad.  In turn, these will be used by 1 to N 
 * specific mappings to appearances, see {@link SignalAppearanceMap}.
 * <p>
 * Insertion order is preserved when retrieving keys.
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
 * @author			Bob Jacobsen Copyright (C) 2009
 * @version			$Revision: 1.2 $
 */
public interface SignalAspectTable extends NamedBean {

    public void setProperty(String aspect, String key, Object value);
    public Object getProperty(String aspect, String key);
  
    public java.util.Enumeration<String> getAspects();

    public java.util.Enumeration<String> getKeys();

    /**
     * Is this aspect known?
     */
    public boolean checkAspect(String aspect);
    
}


/* @(#)SignalAspectTable.java */
