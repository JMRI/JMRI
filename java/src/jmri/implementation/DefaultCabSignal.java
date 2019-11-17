package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.List;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;
import jmri.Block;
import jmri.BlockManager;
import jmri.CabSignal;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.SignalMast;
import jmri.Path;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    synchronized public void setBlock(Block position){
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
    synchronized public void setBlock(){
        BlockManager bmgr = jmri.InstanceManager.getDefault(jmri.BlockManager.class);
        Set<Block> blockSet = bmgr.getNamedBeanSet();
        LocoAddress addr = getCabSignalAddress();
        for (Block blockVal : blockSet) {
            if ( blockVal.getValue() != null ) {
                Object val = blockVal.getValue();
                log.debug("CabSignal for {} searching block {} value {}",
                           addr,blockVal,val);
                if (val instanceof jmri.AddressedIdTag) {
                    if( ((jmri.AddressedIdTag)val).getLocoAddress().toString().equals( 
                         addr.toString())){
                       setBlock(blockVal); 
                       return;
                    }
                } else if (blockVal.getValue().equals(addr) ||
                    blockVal.getValue().toString().equals(addr.toString()) || 
                    blockVal.getValue().toString().equals("" + addr.getNumber())) {
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
    synchronized public Block getBlock(){
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
          _nextBlock = nextBlockOnPath(getBlock(),getBlock().getDirection());
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
                if ((fromdirection & dirftTest) == 0) { // less reliable
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
    @Override
    public SignalMast getNextMast(){
        SignalMast oldNextMast = _nextMast;
        if (_nextMast != null) {
            _nextMast.removePropertyChangeListener(_cconSignalMastListener);
        }
        _nextMast=null;
        if(getBlock()!=null){
           LayoutBlockManager lbm = InstanceManager.getDefault(jmri.jmrit.display.layoutEditor.LayoutBlockManager.class);
        
            Block b = getBlock();
            Block nB = getNextBlock();
            while(_nextMast == null && nB !=null ) {
                _nextMast = lbm.getFacingSignalMast(b, nB);
                b = nB;
                nB = nextBlockOnPath(b,b.getDirection()); 
            }
            if ( _nextMast == null) {
                // use block b which is the last non-null block in the path
                _nextMast = lbm.getSignalMastAtEndBumper(b,null);
            }
           
            if ( _nextMast != null) {
                // add signal changelistener
                _nextMast.addPropertyChangeListener(_cconSignalMastListener = (PropertyChangeEvent e) -> {
                // aspect changed?, need to notify
                firePropertyChange("MastChanged",e.getNewValue(),e.getOldValue());
                forwardCabSignalToLayout();
                });
            }
        }
        if(_nextMast!=null) {
            if(!_nextMast.equals(oldNextMast)) {
                firePropertyChange("NextMast",_nextMast,oldNextMast);
            }
        } else {
            // currentNextMast is null, notify if old next mast was not.
            if(oldNextMast!=null){
               firePropertyChange("NextMast",_nextMast,oldNextMast);
            }
        }
        return _nextMast;
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


    PropertyChangeSupport pcs = new PropertyChangeSupport(this);

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

    private static final Logger log = LoggerFactory.getLogger(DefaultCabSignal.class);


}
