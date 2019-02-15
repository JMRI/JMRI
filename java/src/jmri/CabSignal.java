package jmri;

/**
 * Interface for a Cab Signal Object, describing the state of the track ahead
 * relative to a locomotive with a given address.  This is effectively a mobile
 * signal mast.
 *
 * <hr>
 * This file is part of JMRI.
 * <P>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <P>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * <P>
 *
 * @author Steve Young Copyright (C) 2018
 * @author Paul Bender Copyright (C) 2019
 */
public interface CabSignal {

    /**
     * A method for cleaning up the cab signal 
     */
    public void dispose();

    /**
     * Get the LocoAddress associated with the consist
     *
     * @return the cab signal address
     */
    public LocoAddress getCabSignalAddress();

    /**
     * Direction the locomotive is running.
     *
     * @return 1 for Forward 0 for Reverse.
     */
    public int getLocoDirection();

    /**
     * Set the Block of the locomotive
     *
     * @param position is a Block the locomotive is in.
     */
    public void setBlock(Block position);

    /**
     * Get the Block position of the locomotive associated with the cab signal.
     *
     * @return The current Block position
     */
    public Block getBlock();

    /**
     * Get the Next Block the locomotive is expected to enter.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next Block position
     */
    public Block getNextBlock();

    /**
     * Get the Next Signal Mast the locomotive is expected to pass.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next SignalMast position
     */
    public SignalMast getNextMast();

    /**
     * Add a listener for consist events
     *
     * @param listener is a PropertyChangeListener object
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener);

    /**
     * Remove a listener for cab signal events
     *
     * @param listener is a PropertyChangeListener object
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener);

}
