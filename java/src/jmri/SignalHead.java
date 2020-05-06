package jmri;

/**
 * Represent a single signal head. (Try saying that ten times fast!) A signal
 * may have more than one of these (e.g. a signal mast consisting of several
 * heads) when needed to represent more complex aspects, e.g. Diverging Approach
 * etc.
 * <p>
 * This allows access to explicit appearance information. We don't call this an
 * Aspect, as that's a composite of the appearance of several heads.
 * <p>
 * This class has three bound parameters:
 * <DL>
 * <DT>Appearance<DD>The specific color being shown. Values are the various
 * color constants defined in the class.
 * <p>
 * The appearance constants form a bit mask, so they can be used with hardware
 * that can display e.g. more than one lit color at a time. Individual
 * implementations may not be able to handle that, however; most of the early
 * ones probably won't. If a particular implementation can't display a commanded
 * color, it doesn't try to replace it, but rather just leaves that color off
 * the resulting display.
 * <DT>Lit<DD>Whether the head's lamps are lit or left dark.
 * <p>
 * This differs from the DARK color defined for the appearance parameter, in
 * that it's independent of that. Lit is intended to allow you to extinguish a
 * signal head for approach lighting, while still allowing it's color to be set
 * to a definite value for e.g. display on a panel or evaluation in higher level
 * logic.
 *
 * <DT>Held<DD>Whether the head's lamps should be forced to a specific
 * appearance, e.g. RED in higher-level logic.
 * <p>
 * For use in signaling systems, this is a convenient way of storing whether a
 * higher-level of control (e.g. non-vital system or dispatcher) has "held" the
 * signal at stop. It does not effect how this signal head actually works; any
 * appearance can be set and will be displayed even when "held" is set.
 * </dl>
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
 *
 * @author Bob Jacobsen Copyright (C) 2002, 2008
 */
public interface SignalHead extends Signal {

    public static final int DARK = 0x00;
    public static final int RED = 0x01;
    public static final int FLASHRED = 0x02;
    public static final int YELLOW = 0x04;
    public static final int FLASHYELLOW = 0x08;
    public static final int GREEN = 0x10;
    public static final int FLASHGREEN = 0x20;
    public static final int LUNAR = 0x40;
    public static final int FLASHLUNAR = 0x80;
    public static final int HELD = 0x100;

    /**
     * Get the Signal Head Appearance.
     * Changes in this value can be listened to using the
     * {@literal Appearance} property.
     *
     * @return the appearance
     */
    public int getAppearance();

    /**
     * Set the Signal Head Appearance.
     *
     * @param newAppearance integer representing a valid Appearance for this head
     */
    public void setAppearance(int newAppearance);

    public String getAppearanceKey();

    public String getAppearanceKey(int appearance);

    public String getAppearanceName();

    public String getAppearanceName(int appearance);

    /**
     * Get whether the signal head is lit or dark. Changes to this value can be
     * listened to using the {@literal Lit} property.
     *
     * @return true if lit; false if dark
     */
    @Override
    public boolean getLit();

    @Override
    public void setLit(boolean newLit);

    /**
     * Get whether the signal head is held. Changes to this value can be listened to
     * using the {@literal Held} property. It controls what mechanisms can
     * control the head's appearance. The actual semantics are defined by those
     * external mechanisms.
     *
     * @return true if held; false otherwise
     */
    @Override
    public boolean getHeld();

    @Override
    public void setHeld(boolean newHeld);

    /**
     * Get an array of appearance indexes valid for the mast type.
     *
     * @return array of appearance state values available on this mast type
     */
    public int[] getValidStates();

    /**
     * Get an array of non-localized appearance keys valid for the mast type.
     * For GUI application consider using (capitalized) {@link #getValidStateNames()}
     *
     * @return array of translated appearance names available on this mast type
     */
    public String[] getValidStateKeys();

    /**
     * Get an array of localized appearance descriptions valid for the mast type.
     * For persistance and comparison consider using {@link #getValidStateKeys()}
     *
     * @return array of translated appearance names
     */
    public String[] getValidStateNames();

}
