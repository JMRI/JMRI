package jmri.jmrit.logix;

import java.util.List;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalMast;
import jmri.implementation.SignalSpeedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An SCWarrant is a warrant that is controlled by the signals on a layout. 
 * It will not run unless you have your layout fully covered with sensors and
 * signals.
 * 
 * @author  Karl Johan Lisby Copyright (C) 2016
 */
public class SCWarrant extends Warrant {

    private NamedBean _nextSignal = null; // The signal that we are currently looking at to determine speed.
    public static final float SPEED_STOP = 0.0f;
    public static final float SPEED_TO_PLATFORM = 0.2f;
    public static final float SPEED_UNSIGNALLED = 0.4f;
    private long timeToPlatform = 500;
    private boolean forward = true;
    
    /**
     * Create an object with no route defined. The list of BlockOrders is the
     * route from an Origin to a Destination
     */
    public SCWarrant(String sName, String uName, long TTP) {
        super(sName.toUpperCase(), uName);
        log.debug("new SCWarrant "+uName+" TTP="+TTP);
        timeToPlatform = TTP;
    }

    public long getTimeToPlatform() {
        return timeToPlatform;
    }
    
    public void setTimeToPlatform(long TTP) {
        timeToPlatform = TTP;
    }

    public void setForward(boolean set) {
        forward = set;
    }
    
    public boolean getForward() {
        return forward;
    }

    /**
     * This method has been overridden in order to avoid allocation of occupied blocks.
     */
     public String setRoute(int delay, List<BlockOrder> orders) {
        BlockOrder bo = getBlockOrderAt(0);
        OBlock block = bo.getBlock();
        String message = block.allocate(this);
        if (message != null) {
           log.info(_trainName+" START-block allocation failed "+ message);
           return message;
        }
        message = bo.setPath(this);
        if (message != null) {
           log.info(_trainName+" setting path in START-block failed "+ message);
           return message;
        }
        return null;
    }

    /**
     * Callback from acquireThrottle() when the throttle has become available.sync
     */
    public void notifyThrottleFound(DccThrottle throttle) {
        if (throttle == null) {
            abortWarrant("notifyThrottleFound: null throttle(?)!");
            firePropertyChange("throttleFail", null, Bundle.getMessage("noThrottle"));
            return;
        }
        if (_runMode == MODE_LEARN) {
            abortWarrant("notifyThrottleFound: No LEARN mode for SCWarrant");
            InstanceManager.throttleManagerInstance().releaseThrottle(throttle, this);
            firePropertyChange("throttleFail", null, Bundle.getMessage("noThrottle"));
            return;
        }
        log.debug(_trainName+" notifyThrottleFound address= " + throttle.getLocoAddress().toString() + " _runMode= " + _runMode);
        
        startupWarrant();
        getSpeedUtil().setThrottle(throttle);
        getSpeedUtil().setOrders(getBlockOrders());

        _engineer = new Engineer(this, throttle);
        firePropertyChange("runMode", Integer.valueOf(MODE_NONE), Integer.valueOf(_runMode));
        runSignalControlledTrain();
    }

    /**
     * Generate status message to show in warrant table
     **/
    synchronized protected String getRunningMessage() {
        if (_engineer == null) {
            // The warrant is not active
            return super.getRunningMessage();
        } else {
            String block = getBlockOrderAt(_idxCurrentOrder).getBlock().getDisplayName();
            String signal = "no signal";
            String aspect = "none";
            if (_nextSignal != null) {
                signal = _nextSignal.getDisplayName();
                if (_nextSignal instanceof SignalHead) {
                    int appearance = ((SignalHead) _nextSignal).getAppearance();
                    aspect = "appearance "+appearance;
                } else {
                    aspect = ((SignalMast) _nextSignal).getAspect();
                }
            }
            return Bundle.getMessage("SCWStatus", block, _idxCurrentOrder, _engineer._throttle.getSpeedSetting(),signal,aspect);
        }
    }

    /******************************************************************************************************
     * Use _engineer.setSpeed() to control the train, but do not let the Engineer run its normal set
     * of commands.
     *
     * Get notified of signals, block occupancy and take care of block allocation status to determine speed.
     *
     * We have three speeds: Stop == SPEED_STOP
     *                       Normal == SPEED_NORMAL
     *                       Anything else == SPEED_MID (Limited, Medium, Slow, Restricted)
     *
     * If you have blocks large enough to ramp speed nicely up and down and to have further control
     * of speed settings: Use a normal warrant and not a signal controlled one.
     *
     * This is "the main loop" for running a Signal Controlled Warrant
     ******************************************************************************************************/
    protected void runSignalControlledTrain () {
        waitForStartblockToGetOccupied();
        allocateBlocksAndSetTurnouts(0);
        setTrainDirection();
        SCTrainRunner thread = new SCTrainRunner(this);
        new Thread(thread).start();
    }
    
    /**
     * wait until there is a train in the start block.
     */
    protected boolean isStartBlockOccupied() {
        int blockState = getBlockOrderAt(0).getBlock().getState();
        if ((blockState & OBlock.UNOCCUPIED) == OBlock.UNOCCUPIED) {
            return false;
        } else {
            return true;
        }
    }
    synchronized protected void waitForStartblockToGetOccupied() {
        while (!isStartBlockOccupied()) {
            try {
                // We will not be woken up by goingActive, since we have not allocated the start block yet.
                // So do a timed wait.
                wait(2500);
            } catch (InterruptedException ie) {
                log.debug(_trainName+" InterruptedException "+ie);
            }
        }
    }
    
    /**
     * Set this train to run backwards or forwards as specified in the command list.
     */
    public void setTrainDirection () {
        _engineer._throttle.setIsForward(forward);
    }

    /**
     * Is the next block free or occupied, i.e do we risk to crash into an other train, if we proceed?
     * And is it allocated to us?
     */
    public boolean isNextBlockFreeAndAllocated() {
        BlockOrder bo = getBlockOrderAt(_idxCurrentOrder+1);
        if (bo == null) return false;
        int blockState = bo.getBlock().getState();
        if (blockState == (OBlock.UNOCCUPIED | OBlock.ALLOCATED)) {
            return getBlockOrderAt(_idxCurrentOrder+1).getBlock().isAllocatedTo(this);
        } else {
            return false;
        }
    }

    /**
     * Find the next signal along our route and setup subscription for status changes on that signal.
     */
    public void getAndGetNotifiedFromNextSignal() {
        if (_nextSignal != null) {
            log.debug(_trainName+" getAndGetNotifiedFromNextSignal removing property listener for signal "+_nextSignal.getDisplayName());
            _nextSignal.removePropertyChangeListener(this);
            _nextSignal = null;
        }
        for (int i = _idxCurrentOrder+1; i <= getBlockOrders().size()-1; i++) {
            BlockOrder bo = getBlockOrderAt(i);
            if (bo == null) {
                log.debug(_trainName+" getAndGetNotifiedFromNextSignal could not find a BlockOrder for index "+i);
            } else if (bo.getEntryName().equals("")) {
                log.debug(_trainName+" getAndGetNotifiedFromNextSignal could not find an entry to Block for index "+i);
            } else {
                log.debug(_trainName+" getAndGetNotifiedFromNextSignal examines block "+bo.getBlock().getDisplayName()+" with entryname = "+bo.getEntryName());
                _nextSignal = bo.getSignal();
                if (_nextSignal != null) {
                    log.debug(_trainName+" getAndGetNotifiedFromNextSignal found a new signal to listen to: "+_nextSignal.getDisplayName());
                    break;
                }
            }
        }
        if (_nextSignal != null) {
            _nextSignal.addPropertyChangeListener(this);
        }
    }

    /**
     * Move the train if _nextSignal permits. If there is no next signal, we will move forward with half speed.
     */
    SignalSpeedMap _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
    public void setSpeedFromNextSignal () {
        String speed = null;
        if (_nextSignal == null) {
            _engineer.setSpeed(SPEED_UNSIGNALLED);
        } else {
            if (_nextSignal instanceof SignalHead) {
                int appearance = ((SignalHead) _nextSignal).getAppearance();
                speed = _speedMap.getAppearanceSpeed(((SignalHead) _nextSignal).getAppearanceName(appearance));
                log.debug("SignalHead "+((SignalHead) _nextSignal).getDisplayName()+" shows appearance "+appearance+" which maps to speed "+speed);
            } else {
                String aspect = ((SignalMast) _nextSignal).getAspect();
                speed = _speedMap.getAspectSpeed(aspect, ((SignalMast) _nextSignal).getSignalSystem());
                log.debug("SignalMast "+((SignalMast) _nextSignal).getDisplayName()+" shows aspect "+aspect+" which maps to speed "+speed);
            }
            float speed_f = (float) (_speedMap.getSpeed(speed) / 125.);
            // Ease the speed, if we are approaching the destination block
            if ((_idxCurrentOrder == getBlockOrders().size()-2) && (speed_f > SPEED_UNSIGNALLED)) {
                speed_f = SPEED_UNSIGNALLED;
            }
            _engineer.setSpeed(speed_f);
        }
    }
    
     /**
     * Do what the title says. But make sure not to set the turnouts if already done, since that 
     * would just cause all signals to go to Stop aspects and thus cause a jerky train movement.
     */
    protected void allocateBlocksAndSetTurnouts(int startIndex) {
        log.debug(_trainName+" allocateBlocksAndSetTurnouts startIndex="+startIndex+" _orders.size()="+getBlockOrders().size());
        for (int i = startIndex; i < getBlockOrders().size(); i++) {
            log.debug(_trainName+" allocateBlocksAndSetTurnouts for loop #"+i);
            BlockOrder bo = getBlockOrderAt(i);
            OBlock block = bo.getBlock();
            String pathAlreadySet = block.isPathSet(bo.getPathName());
            if (pathAlreadySet == null) {
                String message = null;
                if ((block.getState() & OBlock.OCCUPIED) != 0) {
                    log.info(_trainName+" block allocation failed "+block.getDisplayName() + " not allocated, but Occupied.");
                    message = " block allocation failed ";
                }
                if (message == null) {
                    message = block.allocate(this);
                    if (message != null) {
                        log.info(_trainName+" block allocation failed "+ message);
                    }
                }
                if (message == null) {
                    message = bo.setPath(this);
                }
                if (message != null) {
                    log.debug(_trainName+" path setting failed for "+this.getDisplayName()+" at block "+block.getDisplayName()+"  "+message);
                    if (_stoppingBlock != null) {
                        _stoppingBlock.removePropertyChangeListener(this);
                    }
                    _stoppingBlock = block;
                    _stoppingBlock.addPropertyChangeListener(this);
                    // This allocation failed. Do not attempt to allocate the rest of the route.allocation
                    // That would potentially lead to deadlock situations where two warrants are competing
                    // and each getting every second block along the same route.
                    return;
                }
            } else if (pathAlreadySet.equals(this.getDisplayName())) {
                log.debug(_trainName+" Path "+bo.getPathName()+" already set (and thereby block allocated) for "+pathAlreadySet);
            } else {
                log.info(_trainName+" Block allocation failed: Path "+bo.getPathName()+" already set (and thereby block allocated) for "+pathAlreadySet);
                return;
            }
        }
    }
    
    /**
     * Block in the route going active.
     * Make sure to allocate the rest of the route, update our present location and then tell
     * the main loop to find a new throttle setting.
     */
    protected void goingActive(OBlock block) {
        if (_runMode != MODE_RUN) {
            // if we are not running, we must not think that we are going to the next block - it must be another train
            return;
        }
        if (_engineer == null || _engineer._throttle.getSpeedSetting() == SPEED_STOP) {
            // if we are not running, we must not think that we are going to the next block - it must be another train
            return;
        }
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        log.debug(_trainName+" **Block \"" + block.getDisplayName() + "\" goingActive. activeIdx= "
                    + activeIdx + ", _idxCurrentOrder= " + _idxCurrentOrder
                    + " - warrant= " + getDisplayName());
        if (activeIdx <= 0) {
            // The block going active is not part of our route ahead
            log.debug(_trainName+" Block going active is not part of this trains route forward");
        } else if (activeIdx == _idxCurrentOrder) {
            // Unusual case of current block losing detection, then regaining it.  i.e. dirty track, derail etc.
            log.debug(_trainName+" Current block becoming active - ignored");
        } else if (activeIdx == _idxCurrentOrder+1) {
            // not necessary: It is done in the main loop in SCTrainRunner.run:  allocateBlocksAndSetTurnouts(_idxCurrentOrder+1);
            // update our present location
            _idxCurrentOrder++;
            // fire property change (entered new block)
            firePropertyChange("blockChange", getBlockAt(_idxCurrentOrder-1), getBlockAt(_idxCurrentOrder));
            // now let the main loop adjust speed.
            synchronized(this) {
                notify();
            }
        } else {
            log.debug(_trainName+" Rogue occupation of block.");
            // now let the main loop stop for a train that is coming in our immediate way.
            synchronized(this) {
                notify();
            }
        }
    }

    /**
     * Block in the route is going Inactive. 
     * Release the blocks that we have left.
     * Check if current block has been left (i.e. we have left our route) and stop the train in that case.
     */
    protected void goingInactive(OBlock block) {
        if (_runMode != MODE_RUN) {
            return;
        }

        int idx = getIndexOfBlock(block, 0);  // if idx >= 0, it is in this warrant
        log.debug(_trainName+" Block \"" + block.getDisplayName() + "\" goingInactive. idx= "
                    + idx + ", _idxCurrentOrder= " + _idxCurrentOrder
                    + " - warrant= " + getDisplayName());
        if (idx < _idxCurrentOrder) {
            deallocateUpToBlock(idx);
        } else if (idx == _idxCurrentOrder) {
            // train is lost
            log.debug(_trainName+" LOST TRAIN firePropertyChange(\"blockChange\", " + block.getDisplayName()
                                + ", null) - warrant= " + getDisplayName());
//            firePropertyChange("blockChange", block, null);
            if (_engineer != null) {
                _engineer.setSpeed(SPEED_STOP);
            } 
//            controlRunTrain(ABORT);
        }
        // now let the main loop stop our train if this means that the train is now entirely within the last block.
        // Or let the train continue if an other train that was in its way has now moved.
        synchronized(this) {
            notify();
        }
    }
    
    /**
     * Deallocate all blocks up to and including idx, but only on these conditions in order to ensure that only a consecutive list of blocks are allocated at any time:
     *     1. Only if our train has left not only this block, but also all previous blocks.
     *     2. Only if the block shall not be re-used ahead and all block up until the block are allocated.
     */
    protected void deallocateUpToBlock(int idx) {
        for (int i=0; i<=idx; i++) {
            OBlock block_i = getBlockOrderAt(i).getBlock();
            if (block_i.isAllocatedTo(this)) {
                if ((block_i.getState() & OBlock.UNOCCUPIED) != OBlock.UNOCCUPIED) {
                    //Do not deallocate further blocks, since this one is still allocated to us and not free.
                    return;
                }
                boolean deAllocate = true;
                // look ahead to see if block_i is reused in the remaining part of the route.
                for (int j= _idxCurrentOrder; j<getBlockOrders().size(); j++) {
                    OBlock block_j = getBlockOrderAt(j).getBlock();
                    if (!block_j.isAllocatedTo(this)) {
                        // There is an unallocated block ahead before we have found block_i is re-used. So deallocate block_i
                        deAllocate = true;
                        break;
                    }
                    if (block_i == block_j) {
                        // clock_i is re-used, and we have no "holes" in the string of allocated blocks before it. So do not deallocate.
                        deAllocate = false;
                        break;
                    }
                }
                if (deAllocate) {
                    block_i.deAllocate(this);
                }
            }
        }
    }


    /**
     * Something has fired a property change event.
     * React if:
     *     - it is a warrant that we need to synchronize with. And then again: Why?
     *     - it is _nextSignal
     * Do not worry about sensors and blocks. They are handled by goingActive and goingInactive.
     */
 @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "UW_UNCOND_WAIT", justification = "Unconditional wait is give the warrant that now has _stoppingBlock allocated a little time to deallocate it.  This occurs after this method sets _stoppingBlock to null.")
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            log.debug(_trainName+" propertyChange \""+evt.getPropertyName()+
                                                "\" old= "+evt.getOldValue()+" new= "+evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
        log.debug(_trainName+" propertyChange \"" + property + "\" new= " + evt.getNewValue()
              + " source= " + ((NamedBean) evt.getSource()).getDisplayName()
                    + " - warrant= " + getDisplayName());
        if (_nextSignal != null && _nextSignal == evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // The signal controlling this warrant has changed. Adjust the speed (in runSignalControlledTrain)
                synchronized(this) {
                    notify();
                }
                return;
            }
        }
        synchronized(this) {
            if (_stoppingBlock != null) {
                log.debug(_trainName+" CHECKING STOPPINGBLOCKEVENT ((NamedBean) evt.getSource()).getDisplayName() = '"+((NamedBean) evt.getSource()).getDisplayName()+"'");
                if (((NamedBean) evt.getSource()).getDisplayName().equals(_stoppingBlock.getDisplayName()) &&
                        evt.getPropertyName().equals("state") &&
                        (((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) == OBlock.UNOCCUPIED) {
                    log.debug(_trainName+" being aware that Block "+((NamedBean) evt.getSource()).getDisplayName()+" has become free");
                    _stoppingBlock.removePropertyChangeListener(this);
                    _stoppingBlock = null;
                    // we might be waiting for this block to become free
                    // Give the warrant that now has _stoppingBlock allocated a little time to deallocate it
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                    }
                    // And then let our main loop continue
                    notify();
                    return;
                }
            }
        }
    }
    
    
    /**
     * Make sure to free up additional resources for a running SCWarrant.
     */
    synchronized public void stopWarrant(boolean abort) {
        if (_nextSignal != null) {
            _nextSignal.removePropertyChangeListener(this);
            _nextSignal = null;
        }
        super.stopWarrant(abort);
    }

    
    /*******************************************************************************************************************************
     * The waiting for event must happen in a separate thread.
     * Therefore the main code of runSignalControlledTrain is put in this class.
     *******************************************************************************************************************************/
    private class SCTrainRunner implements Runnable {
        Warrant _warrant = null;
        SCTrainRunner(Warrant warrant) {
            _warrant = warrant;
        }
        
        public void run() {
            synchronized(_warrant) {
                // Do not include the stopping block in this while loop. It will be handled after the loop.
                List<BlockOrder> orders = getBlockOrders();
                while (_warrant._idxCurrentOrder < orders.size()-1) {
                    log.debug(_warrant._trainName+" runSignalControlledTrain entering while loop. _idxCurrentOrder="+_idxCurrentOrder+" _orders.size()="+orders.size());
                    if (_engineer == null) {
                        // Warrant was stopped
                        return;
                    }
                    allocateBlocksAndSetTurnouts(_warrant._idxCurrentOrder);
                    if (isNextBlockFreeAndAllocated()) {
                        getAndGetNotifiedFromNextSignal();
                        setSpeedFromNextSignal();
                        log.debug(_warrant._trainName+" "+_warrant.getDisplayName()+" "+_warrant.getRunningMessage());
                    } else {
                        log.debug(_warrant._trainName+" runSignalControlledTrain stops train due to block not free: "+getBlockOrderAt(_idxCurrentOrder+1).getBlock().getDisplayName());
                        if (_engineer == null) { // If the warrant is already aborted, _engineer is gone too
                            return;
                        } else {
                            _engineer.setSpeed(SPEED_STOP);
                        }
                        getBlockOrderAt(_idxCurrentOrder+1).getBlock().addPropertyChangeListener(_warrant);
                    }
                    try {
                        // We do a timed wait for the sake of robustness, even though we will be woken up by all relevant events.
                        _warrant.wait(2000);
                    } catch (InterruptedException ie) {
                        log.debug(_warrant._trainName+" InterruptedException "+ie);
                    }
                }
                // We are now in the stop block. Move forward for half a second with half speed until the block before the stop block is free.
                log.debug(_warrant._trainName+" runSignalControlledTrain out of while loop, i.e. train entered stop block _idxCurrentOrder="+
                                           _idxCurrentOrder+" _orders.size()="+orders.size()+
                                           "  waiting for train to clear block "+getBlockAt(orders.size()-2).getDisplayName());
                _engineer.setSpeed(SPEED_TO_PLATFORM);
                while (!getBlockAt(orders.size()-2).isFree() && getBlockAt(orders.size()-2).isAllocatedTo(_warrant)) {
                    log.debug(_warrant._trainName+" runSignalControlledTrain entering wait. Block "+
                                     getBlockAt(orders.size()-2).getDisplayName()+
                                     "   free: "+getBlockAt(orders.size()-2).isFree()+
                                     "   allocated to this warrant: "+getBlockAt(orders.size()-2).isAllocatedTo(_warrant));
                    try {
                        // This does not need to be a timed wait, since we will get interrupted once the block is free
                        // However, the functionality is more robust with a timed wait.
                        _warrant.wait(500);
                    } catch (InterruptedException ie) {
                        log.debug(_warrant._trainName+" InterruptedException "+ie);
                    }
                    log.debug(_warrant._trainName+" runSignalControlledTrain woken after last wait.... _orders.size()="+orders.size());
                }
                if (timeToPlatform > 100) {
                    log.debug(_warrant._trainName+" runSignalControlledTrain is now fully into the stopping block. Proceeding for "+timeToPlatform+" miliseconds");
                    long timeWhenDone = System.currentTimeMillis() + timeToPlatform;
                    long remaining;
                    while ((remaining = timeWhenDone - System.currentTimeMillis()) > 0) {
                        try {
                            log.debug(_warrant._trainName+" running slowly to platform for "+remaining+" miliseconds");
                            _warrant.wait(remaining);
                        } catch (InterruptedException e) {
                            log.debug(_warrant._trainName+" InterruptedException "+e);
                        }
                    }
                }
                log.debug(_warrant._trainName+" runSignalControlledTrain STOPPING TRAIN IN STOP BLOCK");
                _engineer.setSpeed(SPEED_STOP);
                stopWarrant(false);
            }
        }
    }
    
    
    /**
     * 
     */
    private final static Logger log = LoggerFactory.getLogger(SCWarrant.class);
}
