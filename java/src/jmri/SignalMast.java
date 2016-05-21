// SignalMast.java
package jmri;
import java.util.Vector;
/**
 * Represent a signal mast.  A signal mast is one or more signal heads
 * that are treated as a single signal.  (Imagine several heads 
 * attached to a single mast, though other implementations are possible)
 * <P>
 * A mast presents an Aspect, as that's a composite of the appearance
 * of the entire signal.
 * <P>
 * This class has three bound parameters:
 *<DL>
 *<DT>aspect<DD>The specific aspect being shown.
 * <p>
 * Aspects are named by a user defined String name.
 *
 *<DT>lit<DD>Whether the head's lamps are lit or left dark.
 *<P>
 * This differs from the DARK color defined for the appearance
 * parameter, in that it's independent of that.  Lit is 
 * intended to allow you to extinquish a signal head for 
 * approach lighting, while still allowing it's color to be
 * set to a definite value for e.g. display on a panel or
 * evaluation in higher level logic.
 *
 *<DT>held<DD>Whether the head's lamps should be forced to the RED position
 * in higher-level logic.
 *<P>
 * For use in signaling systems, this is a convenient
 * way of storing whether a higher-level of control (e.g. non-vital
 * system or dispatcher) has "held" the signal at stop. It does
 * not effect how this signal head actually works; any appearance can
 * be set and will be displayed even when "held" is set.
 *</dl>
 * The integer state (getState(), setState()) is the index of the
 * current aspect in the list of all defined aspects.
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
 * @author			Bob Jacobsen Copyright (C) 2002, 2008
 * @author			Pete Cressman Copyright (C) 2009
 * @version			$Revision$
 */
public interface SignalMast extends NamedBean {
    /**
     * Set aspect to a valid name in the current 
     * signal system definition.
     * @throws IllegalArgumentException if not a valid aspect name
     */
    public void setAspect(String aspect);
    
    /**
     * Get current aspect name. This is a bound property.
     *
     * @return null if not yet set
     */
    public String getAspect();
    
    public Vector<String> getValidAspects();
    
    public SignalSystem getSignalSystem();
    
    public SignalAppearanceMap getAppearanceMap();

    /**
     * Lit is a bound parameter. It controls
     * whether the signal head's lamps are lit or left dark.
     */
    public boolean getLit();
    public void setLit(boolean newLit);

    /**
     * Held is a bound parameter. It controls
     * what mechanisms can control the head's appearance.
     * The actual semantics are defined by those external mechanisms.
     */
    public boolean getHeld();
    public void setHeld(boolean newHeld);
    
    public boolean isAspectDisabled(String aspect);
    
    /**
    * Sets whether the Signal Mast is allowed or configured to show an
    * unlit aspect, or if it is always lit.
    * @param boo Set true to allow the UnLit to be used, set false it is not
    *            supported or allowed.
    */
    public void setAllowUnLit(boolean boo);
    
    public boolean allowUnLit();
}
/* @(#)SignalMast.java */
