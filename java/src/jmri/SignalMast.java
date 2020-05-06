package jmri;

import java.util.Vector;
import javax.annotation.Nonnull;

/**
 * Represent a signal mast. A signal mast is one or more signal heads that are
 * treated as a single signal. (Imagine several heads attached to a single mast,
 * though other implementations are possible)
 * <p>
 * A mast presents an Aspect, as that's a composite of the appearance(s) of the
 * entire signal.
 * <p>
 * This class has three bound parameters:
 * <DL>
 * <DT>Aspect<DD>The specific aspect being shown.
 * <p>
 * Aspects are named by a user defined String name.
 * <DT>Lit<DD>Whether the mast's lamps are lit or left dark.
 * This differs from the DARK color defined for the appearance parameter, in
 * that it's independent of that. Lit is intended to allow you to extinquish a
 * signal mast for approach lighting, while still allowing its color to be set
 * to a definite value for e.g. display on a panel or evaluation in higher level
 * logic.
 * <DT>Held<DD>Whether the mast's lamps should be forced to a specific aspect,
 * e.g. Stop, in higher-level logic.
 * <p>
 * For use in signaling systems, this is a convenient way of storing whether a
 * higher-level of control (e.g. non-vital system or dispatcher) has "held" the
 * signal at stop. It does not effect how this signal mast actually works; any
 * appearance can be set and will be displayed even when "held" is set.
 * </dl>
 * The integer state (getState(), setState()) is the index of the current aspect
 * in the list of all defined aspects.
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
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008
 * @author Pete Cressman Copyright (C) 2009
 */
public interface SignalMast extends NamedBean {  // to eventually be Signal

    /**
     * Set aspect to a valid name in the current signal system definition.
     *
     * @param aspect the new aspect shown
     * @throws IllegalArgumentException if not a valid aspect name
     */
    public void setAspect(@Nonnull String aspect);

    /**
     * Get current aspect name. Changes to this property can be listened to
     * using the property {@literal Aspect}.
     *
     * @return the current aspect or null if not set
     */
    public String getAspect();

    /**
     * Get an alphabetically sorted list of valid aspects that have not been disabled.
     *
     * @return sorted list of valid aspects; may be empty
     */
    @Nonnull
    public Vector<String> getValidAspects();

    public SignalSystem getSignalSystem();

    public SignalAppearanceMap getAppearanceMap();

    /**
     * Set the specific mast type for this mast. 
     * This is the
     * type that appears in the SystemName and filename, i.e. "SL-3-high"
     * for the 
     * <a href="http://jmri.org/xml/signals/AAR-1946/appearance-SL-3-high.xml">AAR-1946/appearance-SL-3-high.xml</a>
     * definition.
     */
    public void setMastType(@Nonnull String type);

    /**
     * Get the specific mast type for this mast.
     * This is the
     * type that appears in the SystemName and filename, i.e. "SL-3-high"
     * for the 
     * <a href="http://jmri.org/xml/signals/AAR-1946/appearance-SL-3-high.xml">AAR-1946/appearance-SL-3-high.xml</a>
     * definition.
     */
    public String getMastType();
    
    /**
     * Get if signal mast is lit or dark. Changes to this property can be
     * listened to using the property {@literal Lit}.
     *
     * @return true if lit; false if dark
     */
    public boolean getLit();

    public void setLit(boolean newLit);

    /**
     * Get the held state of the signal mast. It controls what mechanisms can
     * control the mast's appearance. The actual semantics are defined by those
     * external mechanisms. Changes to this property can be listened to using
     * the property {@literal Held}.
     *
     * @return true if held; false otherwise
     */
    public boolean getHeld();

    public void setHeld(boolean newHeld);

    public boolean isAspectDisabled(String aspect);

    /**
     * Sets whether the Signal Mast is allowed or configured to show an unlit
     * aspect, or if it is always lit.
     *
     * @param boo Set true to allow the UnLit to be used, set false it is not
     *            supported or allowed.
     */
    public void setAllowUnLit(boolean boo);

    public boolean allowUnLit();
}
