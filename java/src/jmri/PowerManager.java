package jmri;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

import jmri.beans.PropertyChangeProvider;

/**
 * Provide controls for layout power.
 * <p>
 * The PowerManager handles three states:
 * <ul>
 * <li>On/Off which controls electrical power to the track
 * <li>an optional "Idle" state, where track power is alive but track-connected
 *     decoders may be un-controllable
 * </ul>
 * A layout may not have control over these, in which case attempts to change
 * them should return an exception. If the state cannot be sensed, that should
 * also return an exception.
 * <p>
 * Some connections, including some LocoNet-based connections, implement the "Idle"
 * state.  For these LocoNet-based connections, when the Power state is "Idle", the
 * track power is alive and the command station is broadcasting "stop" to all mobile
 * decoders. Other systems may implement different interpretation of the "Idle" state.
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
 * @author Bob Jacobsen Copyright (C) 2001
 */
public interface PowerManager extends PropertyChangeProvider {

    int UNKNOWN = NamedBean.UNKNOWN;
    int ON = 0x02;
    int OFF = 0x04;
    int IDLE = 0x08; // not supported by some connection types

    /**
     * {@link java.beans.PropertyChangeEvent}s are fired with this property name.
     * <p>
     * {@value #POWER}
     */
    String POWER = "power"; // as recommended in JavaBeans Spec // NOI18N

    /**
     * Set the Power.
     * @param v the new power status.
     * @throws jmri.JmriException if unable to send request.
     */
    void setPower(int v) throws JmriException;

    /**
     * Get the current power state.
     * @return int value of state.
     */
    @CheckReturnValue
    int getPower();

    /**
     * Free resources when no longer used.
     * @throws jmri.JmriException if unable to dispose.
     */
    void dispose() throws JmriException;

    /**
     * Check if the connection supports the Idle power state.
     * By default the Power Manager does not implement the IDLE power state.
     * @return true if the connection implements Idle, else false.
     */
    default boolean implementsIdle() {
        return false;
    }

    /**
     * Request Track Power Status Update.
     * Default implementation does nothing.
     */
    default void requestUpdateFromLayout() {}

    /**
     * Get the PowerManager UserName.
     * @return a nonNull userName.
     */
    @CheckReturnValue
    @Nonnull String getUserName();
}
