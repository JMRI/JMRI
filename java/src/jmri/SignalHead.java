package jmri;

import javax.annotation.Nonnull;

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

    int DARK = 0x00;
    int RED = 0x01;
    int FLASHRED = 0x02;
    int YELLOW = 0x04;
    int FLASHYELLOW = 0x08;
    int GREEN = 0x10;
    int FLASHGREEN = 0x20;
    int LUNAR = 0x40;
    int FLASHLUNAR = 0x80;
    int HELD = 0x100;

    /**
     * Get the Signal Head Appearance.
     * Changes in this value can be listened to using the
     * {@literal Appearance} property.
     *
     * @return the appearance, e.g. SignalHead.YELLOW
     */
    int getAppearance();

    /**
     * Set the Signal Head Appearance.
     *
     * @param newAppearance integer representing a valid Appearance for this head
     */
    void setAppearance(int newAppearance);

    
    /**
     * Get the current Signal Head Appearance Key.
     * @return Key, or empty String if no valid appearance set.
     */
    @Nonnull
    String getAppearanceKey();

    /**
     * Get the Appearance Key for a particular Appearance.
     * @param appearance id for the key, e.g. SignalHead.GREEN
     * @return the Appearance Key, e.g. "Green" or empty String if unknown.
     * The key can be used as a Bundle String, e.g.
     * Bundle.getMessage(getAppearanceKey(SignalHead.RED))
     */
    @Nonnull
    String getAppearanceKey(int appearance);

    /**
     * Get the current appearance name.
     * @return Name of the Appearance, e.g. "Dark" or "Flashing Red"
     */
    @Nonnull
    String getAppearanceName();

    /**
     * Get the Appearance Name for a particular Appearance.
     * @param appearance id for the Name.
     * @return the Appearance Name, or empty String if unknown.
     */
    @Nonnull
    String getAppearanceName(int appearance);

    /**
     * {@inheritDoc}
     */
    @Override
    boolean getLit();

    /**
     * {@inheritDoc}
     */
    @Override
    void setLit(boolean newLit);

    /**
     * {@inheritDoc}
     */
    @Override
    boolean getHeld();

    /**
     * {@inheritDoc}
     */
    @Override
    void setHeld(boolean newHeld);

    /**
     * Get an array of appearance indexes valid for the mast type.
     *
     * @return array of appearance state values available on this mast type
     */
    int[] getValidStates();

    /**
     * Get an array of non-localized appearance keys valid for the mast type.
     * For GUI application consider using (capitalized) {@link #getValidStateNames()}
     *
     * @return array of translated appearance names available on this mast type
     */
    String[] getValidStateKeys();

    /**
     * Get an array of localized appearance descriptions valid for the mast type.
     * For persistance and comparison consider using {@link #getValidStateKeys()}
     *
     * @return array of translated appearance names
     */
    String[] getValidStateNames();

}
