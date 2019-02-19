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
    private boolean _direction;
    private Block _currentBlock;
    private Block _nextBlock;
    private SignalMast _nextMast;
    private boolean _cabSignalActive;

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
       _cabSignalActive = true;
       _sendCabSignal = false;
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
     * @param isForward true for Forward false for Reverse.
     */
    public void setLocoDirection(boolean isForward){
          _direction = isForward;
    }

    /**
     * Direction the locomotive is running.
     *
     * @return true for Forward false for Reverse.
     */
    public boolean getLocoDirection(){
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
     * Set the Next Signal Mast the locomotive is expected to pass.
     * This value may be calculated from the current block and direction 
     * of travel.
     *
     * @param mast The next SignalMast position
     */
    public void setNextMast(SignalMast mast){
        _nextMast = mast;
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

    /*
     * get whether this cab signal is on or off
     *
     * @return true if on, false if off
     */
    public boolean isCabSignalActive(){
        return _cabSignalActive;
    }

    /*
     * set whether this cab signal is on or off
     *
     * @param active true if on, false if off
     */
    public void setCabSignalActive(boolean active){
        _cabSignalActive = active;
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
