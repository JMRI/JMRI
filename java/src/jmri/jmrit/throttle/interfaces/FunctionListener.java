package jmri.jmrit.throttle.interfaces;

/**
 *
 * A Listener for a function state change.
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
 * 
 */

public interface FunctionListener extends java.util.EventListener {

    /**
     * Receive notification that a function has changed state.
     *
     * @param functionNumber The id of the function
     * @param isOn           True if the function is activated, false otherwise.
     */
    void notifyFunctionStateChanged(int functionNumber, boolean isOn);

    /**
     * Get notification that a function's lockable status has changed.
     *
     * @param functionNumber The function that has changed (0-9).
     * @param isLockable     True if the function is now Lockable (continuously
     *                       active).
     */
    void notifyFunctionLockableChanged(int functionNumber, boolean isLockable);
}
