package jmri.implementation;

import jmri.Block;
import jmri.CabSignal;
import jmri.LocoAddress;
import jmri.SignalMast;

/**
 * Default implementation of a Cab Signal Object, describing the state of the 
 * track ahead relative to a locomotive with a given address.  This is 
 * effectively a mobile signal mast.
 *
 * @author Steve Young Copyright (C) 2018
 * @author Paul Bender Copyright (C) 2019
 */
public class DefaultCabSignal implements CabSignal {

    private LocoAddress _address;
    private int _direction;
    private Block _currentBlock;
    private Block _nextBlock;
    private SignalMast _nextMast;

    public DefaultCabSignal(LocoAddress address){
       _address = address;
    }

    /**
     * A method for cleaning up the cab signal 
     */
    public void dispose(){
       _address = null;
       _currentBlock = null;
       _nextBlock = null;
       _nextMast = null;
    }

    /**
     * Get the LocoAddress associated with the consist
     *
     * @return the cab signal address
     */
    public LocoAddress getCabSignalAddress(){
        return _address;
    }

    /**
     * Direction the locomotive is running.
     *
     * @return 1 for forward, 0 for reverse.
     */
    public int getLocoDirection(){
          return _direction;
    }

    /**
     * Set the Block of the locomotive
     *
     * @param position is a Block the locomotive is in.
     */
    public void setBlock(Block position){
         _currentBlock = position;
    }

    /**
     * Get the Block position of the locomotive associated with the cab signal.
     *
     * @return The current Block position
     */
    public Block getBlock(){
        return _currentBlock;
    }

    /**
     * Get the Next Block the locomotive is expected to enter.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next Block position
     */
    public Block getNextBlock(){
        return _nextBlock;
    }

    /**
     * Get the Next Signal Mast the locomotive is expected to pass.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next SignalMast position
     */
    public SignalMast getNextMast(){
        return _nextMast;
    }

    /**
     * Add a listener for consist events
     *
     * @param listener is a PropertyChangeListener object
     */
    public void addPropertyChangeListener(java.beans.PropertyChangeListener listener){
    }

    /**
     * Remove a listener for cab signal events
     *
     * @param listener is a PropertyChangeListener object
     */
    public void removePropertyChangeListener(java.beans.PropertyChangeListener listener){
    }

}
