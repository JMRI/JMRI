package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.Nonnull;

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
public class DefaultCabSignal implements CabSignal, PropertyChangeListener {

    private LocoAddress _address = null;
    @GuardedBy("this")
    private Block _currentBlock = null;
    private Block _nextBlock = null;
    private SignalMast _nextMast = null;
    private boolean _cabSignalActive = true;
    private boolean _masterPausedButtonActive = false;
    private PropertyChangeListener _cconSignalMastListener = null;

    public DefaultCabSignal(LocoAddress address){
       _address = address;
    }

    /**
     * A method for cleaning up the cab signal 
     */
    @Override
    @javax.annotation.OverridingMethodsMustInvokeSuper // to remove Signal Listener
    public void dispose(){
        if (_nextMast != null) {
            _nextMast.removePropertyChangeListener(_cconSignalMastListener);
        }
       _address = null;
       _currentBlock = null;
       _nextBlock = null;
       _nextMast = null;
       _cabSignalActive = true;
       _masterPausedButtonActive = false;
    }

    /**
     * Get the LocoAddress associated with the consist
     *
     * @return the cab signal address
     */
    @Override
    public LocoAddress getCabSignalAddress(){
        return _address;
    }

    /**
     * Set the Block of the locomotive
     *
     * @param position is a Block the locomotive is in.
     */
    @Override
    public synchronized void setBlock(Block position){
        log.debug("CabSignal for {} set block {}",getCabSignalAddress(),position);
        Block oldCurrentBlock = _currentBlock;
        if(_currentBlock!=null){
           _currentBlock.removePropertyChangeListener(this);
        }
        _currentBlock = position;
        if(_currentBlock!=null) {
           _currentBlock.addPropertyChangeListener(this);
           if(!_currentBlock.equals(oldCurrentBlock)) {
              firePropertyChange("CurrentBlock",_currentBlock,oldCurrentBlock);
           }
       } else {
           // currentblock is null, notify if old block was not.
           if(oldCurrentBlock!=null){
              firePropertyChange("CurrentBlock",_currentBlock,oldCurrentBlock);
           }
       }
       getNextBlock(); // calculate the next block and fire an appropriate property change.
       // calculate the next mast and fire an appropriate property change.
       forwardCabSignalToLayout();
    }

    /**
     * Set the Block of the locomotive by searching the block list.
     */
    @Override
    public synchronized void setBlock(){
        BlockManager bmgr = InstanceManager.getDefault(BlockManager.class);
        Set<Block> blockSet = bmgr.getNamedBeanSet();
        LocoAddress addr = getCabSignalAddress();
        for (Block blockVal : blockSet) {
            Object val = blockVal.getValue();
            if ( val != null ) {
                log.debug("CabSignal for {} searching block {} value {}",
                           addr,blockVal,val);
                if (val instanceof jmri.AddressedIdTag) {
                    if( ((jmri.AddressedIdTag)val).getLocoAddress().toString().equals( 
                         addr.toString())){
                       setBlock(blockVal); 
                       return;
                    }
                } else if ( val.equals(addr) ||
                    val.toString().equals(addr.toString()) || 
                    val.toString().equals("" + addr.getNumber())) {
                    setBlock(blockVal);
                    return;
                }
            }
        }
        // address not found in any block, set block to null
        setBlock(null);
    }

    /**
     * Get the Block position of the locomotive associated with the cab signal.
     *
     * @return The current Block position
     */
    @Override
    public synchronized Block getBlock(){
        return _currentBlock;
    }

    /**
     * Get the Next Block the locomotive is expected to enter.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next Block position
     */
    @Override
    public Block getNextBlock(){
        Block oldNextBlock = _nextBlock;
        if(getBlock()==null){
           _nextBlock = null; // no current block, so can't have a next block.
        } else {
          _nextBlock = nextBlockOnPath(getBlock());
        }
    
        if(_nextBlock!=null) {
            if(!_nextBlock.equals(oldNextBlock)) {
               firePropertyChange("NextBlock",_nextBlock,oldNextBlock);
            }
        } else {
            // currentNextBlock is null, notify if old next block was not.
            if(oldNextBlock!=null){
               firePropertyChange("NextBlock",_nextBlock,oldNextBlock);
            }
        }
        return _nextBlock;
    }

    private Block nextBlockOnPath(Block current){
        int fromdirection = current.getDirection();
        List<Path> thispaths = current.getPaths();
        for (final Path testpath : thispaths) {
            if (testpath.checkPathSet()) {
                Block blockTest = testpath.getBlock();
                int dirftTest = testpath.getFromBlockDirection();
                int dirtoTest = testpath.getToBlockDirection();
                if (directionMatch(fromdirection, dirtoTest)) { // most reliable
                    blockTest.setDirection(dirtoTest);
                    return blockTest;
                }
                if ((fromdirection & dirftTest) == 0) { // less reliable
                    blockTest.setDirection(dirtoTest);
                    return blockTest;
                }
                if ((fromdirection != dirftTest)) { // least reliable but copes with 180 degrees 
                    blockTest.setDirection(dirtoTest);
                    return blockTest;
                }
            }
        }
      return null;
    }

    private boolean directionMatch(int fromDirection, int toDirection ) {
        return
            (((fromDirection & Path.NORTH) != 0) && ((toDirection & Path.NORTH) != 0)) ||
            (((fromDirection & Path.SOUTH) != 0) && ((toDirection & Path.SOUTH) != 0)) ||
            (((fromDirection & Path.EAST) != 0) && ((toDirection & Path.EAST) != 0)) ||
            (((fromDirection & Path.WEST) != 0) && ((toDirection & Path.WEST) != 0)) ||
            (((fromDirection & Path.CW) != 0) && ((toDirection & Path.CW) != 0)) ||
            (((fromDirection & Path.CCW) != 0) && ((toDirection & Path.CCW) != 0)) ||
            (((fromDirection & Path.LEFT) != 0) && ((toDirection & Path.LEFT) != 0)) ||
            (((fromDirection & Path.RIGHT) != 0) && ((toDirection & Path.RIGHT) != 0)) ||
            (((fromDirection & Path.UP) != 0) && ((toDirection & Path.UP) != 0)) ||
            (((fromDirection & Path.DOWN) != 0) && ((toDirection & Path.DOWN) != 0));
    }

    /**
     * Get the Next Signal Mast the locomotive is expected to pass.
     * This value is calculated from the current block and direction 
     * of travel.
     *
     * @return The next SignalMast position
     */
    @Override
    public SignalMast getNextMast(){
        SignalMast oldNextMast = _nextMast;
        if (_nextMast != null) {
            _nextMast.removePropertyChangeListener(_cconSignalMastListener);
        }
        _nextMast=null;
        if( getBlock() != null ) {
            LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        
            Block b = getBlock();
            Block nB = getNextBlock();
            while(_nextMast == null && nB !=null ) {
                _nextMast = lbm.getFacingSignalMast(b, nB);
                b = nB;
                nB = nextBlockOnPath(b); 
            }
            if ( _nextMast == null) {
                // use block b which is the last non-null block in the path
                _nextMast = lbm.getSignalMastAtEndBumper(b,null);
            }
           
            if ( _nextMast != null) {
                // add signal changelistener
                _cconSignalMastListener = (PropertyChangeEvent e) -> {
                    // aspect changed?, need to notify
                    firePropertyChange("MastChanged",e.getNewValue(),e.getOldValue());
                    forwardCabSignalToLayout();
                };
                _nextMast.addPropertyChangeListener(_cconSignalMastListener);
            }
        }
        if( _nextMast != null ) {
            if ( ! _nextMast.equals(oldNextMast)) {
                firePropertyChange("NextMast",_nextMast,oldNextMast);
            }
        } else {
            // currentNextMast is null, notify if old next mast was not.
            if ( oldNextMast != null ) {
               firePropertyChange("NextMast",_nextMast,oldNextMast);
            }
        }
        return _nextMast;
    }

    /**
     * Get Block List to the end of Path or Signal Mast Stop, whichever first.
     * The first Block in the list ( if any ), will be the current Block.
     * @return list of Blocks that the loco address is expected to traverse.
     */
    @Nonnull
    @Override
    public List<Block> getBlockList() {
        java.util.ArrayList<Block> blockList = new java.util.ArrayList<>();
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        Block thisBlock = getBlock();
        if ( thisBlock == null ) {
            return blockList;
        }
        blockList.add(thisBlock);
        Block nextBlock = nextBlockOnPath(thisBlock); 
        SignalMast mast = ( nextBlock == null ? null : lbm.getFacingSignalMast(thisBlock, nextBlock));
        while ( okToProceedAfterMast(mast) && nextBlock !=null ) {
            blockList.add(nextBlock);
            mast = lbm.getFacingSignalMast(thisBlock, nextBlock);
            thisBlock = nextBlock;
            nextBlock = nextBlockOnPath(thisBlock); 
        }
        return blockList;
    }

    private boolean okToProceedAfterMast( @CheckForNull SignalMast m ) {
        if ( m == null ) {
            return true;
        }
        return !m.isAtStop();
    }

    /**
     * Forward the current cab signal value to the layout.
     */
    @Override
    public void forwardCabSignalToLayout() {
        if (!isCabSignalActive() ) {
            return;
        }
        if (_masterPausedButtonActive) {
            return;
        }

        LocoAddress locoaddr = getCabSignalAddress();
        SignalMast mast = getNextMast();

        if (mast != null) {
            log.debug("cab {} aspect {}",locoaddr,mast.getAspect());
        }
        // and forward the message on to the layout.
        forwardAspectToLayout();
    }

    /**
     * Forward the command to the layout that sets the displayed signal
     * aspect for this address
     */
    protected void forwardAspectToLayout(){
        // this method is to be over-written by subclasses that actually
        // talk to layout hardware.
    }


    /*
     * get whether this cab signal is on or off
     *
     * @return true if on, false if off
     */
    @Override
    public boolean isCabSignalActive(){
        return _cabSignalActive;
    }

    /*
     * set whether this cab signal is on or off
     *
     * @param active true if on, false if off
     */
    @Override
    public void setCabSignalActive(boolean active){
        _cabSignalActive = active;
        if(_cabSignalActive) {
           getNextMast(); // refreshes block, mast, and sends if master button not paused
        }
        else {
            resetLayoutCabSignal(); // send data invalid to layout
        }
    }
    
    /*
     * Set when initialised and when Master PAUSED button is toggled
     *
     * @param active true if paused, false if resumed
     */
    @Override
    public void setMasterCabSigPauseActive (boolean active) {
        _masterPausedButtonActive = active;
        if ( !isCabSignalActive() ){
            return; // if cabsig has already been disabled no action needed
        }
        if ( _masterPausedButtonActive ) {
            log.debug("master paused");
            resetLayoutCabSignal(); // send data invalid to layout
        }
        else {
            log.debug("master not paused");
            getNextMast(); // refreshes block, mast, and sends if single cabsig enabled
        }
    }

    /**
     * Forward the command to the layout that clears any displayed signal
     * for this address
     */
    protected void resetLayoutCabSignal(){
        // this method is to be over-written by subclasses that actually
        // talk to layout hardware.
    }

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    /**
     * Add a listener for consist events
     *
     * @param l is a PropertyChangeListener object
     */
    @Override
    public synchronized void addPropertyChangeListener(PropertyChangeListener l) {
        pcs.addPropertyChangeListener(l);
    }

    /**
     * Remove a listener for cab signal events
     *
     * @param l is a PropertyChangeListener object
     */
    @Override
    public synchronized void removePropertyChangeListener(PropertyChangeListener l) {
        pcs.removePropertyChangeListener(l);
    }

    protected void firePropertyChange(String p, Object old, Object n) {
        log.debug("sending property {} new value {} old value {}",p,old,n);
        pcs.firePropertyChange(p, old, n);
    }

    //PropertyChangeListener interface
    @Override
    public void propertyChange(PropertyChangeEvent event){
        if(event.getSource() instanceof Block) {
            if (event.getPropertyName().equals("value")){
                setBlock(); // change the block.
            }

            // block value is changed before direction is set
            if ((event.getPropertyName().equals("state")) || (event.getPropertyName().equals("direction"))) {
                // update internal state to cascade changes.
                getNextBlock();
                forwardCabSignalToLayout();
            }
        } else if(event.getSource() instanceof SignalMast) {
            // update internal state to cascade changes.
            forwardCabSignalToLayout();
        }
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName()+" "+this.getCabSignalAddress();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultCabSignal.class);

}
