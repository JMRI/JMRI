// SignalAspectTable.java

package jmri;

/**
 * Represents a mapping between the appearances of one or 
 * more SignalHead objects on a single SignalMast, and 
 * a series of named Signal Aspects.
 *<p>
 * At present, the signal aspects are described by Strings,
 * not by specific objects.
 * <p>
 * Setting or getting the "state" of one of these will throw an error.
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
 * @author			Bob Jacobsen Copyright (C) 2009
 * @version			$Revision: 1.1 $
 */
public interface SignalAspectTable extends NamedBean {

    public void setAppearances(String aspect, SignalHead[] heads); 
    
    public void addAspect(String aspect, int[] appearances);
    
    public java.util.Enumeration<String> getAspects();
    
    public void loadDefaults();
    
}


/* @(#)SignalAspectTable.java */
