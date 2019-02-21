package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import jmri.Block;
import jmri.BlockManager;
import jmri.CabSignal;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SignalMast;
import jmri.Path;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;

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
    private PropertyChangeListener _cconSignalMastListener = null;

    public DefaultCabSignal(LocoAddress address){
       _address = address;
    }

    /**
     * A method for cleaning up the cab signal 
     */
    public void dispose(){
        if (_nextMast != null) {
            _nextMast.removePropertyChangeListener(_cconSignalMastListener);
        }
       _address = null;
       _currentBlock = null;
       _nextBlock = null;
       _nextMast = null;
       _cabSignalActive = true;
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
        if(getBlock()==null){
           return null; // no current block, so can't have a next block.
        }
        _nextBlock = nextBlockOnPath(getBlock(),getBlock().getDirection());
        return _nextBlock;
    }

    private Block nextBlockOnPath(Block current, int fromdirection){
        List<Path> thispaths = current.getPaths();
        for (final Path testpath : thispaths) {
            if (testpath.checkPathSet()) {
                Block blockTest = testpath.getBlock();
                int dirftTest = testpath.getFromBlockDirection();
                int dirtoTest = testpath.getToBlockDirection();
                if ((((fromdirection & Path.NORTH) != 0) && ((dirtoTest & Path.NORTH) != 0)) ||
                    (((fromdirection & Path.SOUTH) != 0) && ((dirtoTest & Path.SOUTH) != 0)) ||
                    (((fromdirection & Path.EAST) != 0) && ((dirtoTest & Path.EAST) != 0)) ||
                    (((fromdirection & Path.WEST) != 0) && ((dirtoTest & Path.WEST) != 0)) ||
                    (((fromdirection & Path.CW) != 0) && ((dirtoTest & Path.CW) != 0)) ||
                    (((fromdirection & Path.CCW) != 0) && ((dirtoTest & Path.CCW) != 0)) ||
                    (((fromdirection & Path.LEFT) != 0) && ((dirtoTest & Path.LEFT) != 0)) ||
                    (((fromdirection & Path.RIGHT) != 0) && ((dirtoTest & Path.RIGHT) != 0)) ||
                    (((fromdirection & Path.UP) != 0) && ((dirtoTest & Path.UP) != 0)) ||
                    (((fromdirection & Path.DOWN) != 0) && ((dirtoTest & Path.DOWN) != 0)))
                { // most reliable
                    blockTest.setDirection(dirtoTest);
                    return blockTest;
                }
                if (((fromdirection & dirftTest)) == 0) { // less reliable
                    blockTest.setDirection(dirtoTest);
                    return blockTest;
                }
                if ((fromdirection != dirftTest)){ // least reliable but copes with 180 degrees 
                    blockTest.setDirection(dirtoTest);
                    return blockTest;
                }
            }
        }
      return null;
    }

    /**
     * Get the Next Signal Mast the locomotive is expected to pass.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next SignalMast position
     */
    public SignalMast getNextMast(){
        if (_nextMast != null) {
            _nextMast.removePropertyChangeListener(_cconSignalMastListener);
        }
        _nextMast=null;
        if(getBlock()==null){
           return null; // no current block, so can't have a next signal.
        }
        LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        
        Block b = getBlock();
        Block nB = getNextBlock();
        while(_nextMast == null && nB !=null ) {
           _nextMast = lbm.getFacingSignalMast(b, nB);
           b = nB;
           nB = nextBlockOnPath(b,b.getDirection()); 
        }
        if ( _nextMast == null) {
           _nextMast = lbm.getSignalMastAtEndBumper(getBlock(),null);
        }
        if ( _nextMast != null) {
           // add signal changelistener
           _nextMast.addPropertyChangeListener(_cconSignalMastListener = (PropertyChangeEvent e) -> {
              //updateblocksforrow(row);  // aspect changed?, need to notify
            });
        }
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
