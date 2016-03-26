package jmri.jmrit.logix;

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
    public static final float SPEED_NORMAL = 0.8f;
    public static final float SPEED_MID = 0.4f;
    private long timeToPlatform = 500;
    
    /**
     * Create an object with no route defined. The list of BlockOrders is the
     * route from an Origin to a Destination
     */
    public SCWarrant(String sName, String uName, long TTP) {
        super(sName.toUpperCase(), uName);
        log.debug("new SCWarrant "+uName);
        timeToPlatform = TTP;
    }

    public long getTimeToPlatform() {
        return timeToPlatform;
    }

    /**
     * Callback from acquireThrottle() when the throttle has become available.
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
        if (_debug) {
            log.debug(_trainName+" notifyThrottleFound address= " + throttle.getLocoAddress().toString() + " _runMode= " + _runMode);
        }

        startupWarrant();
        _engineer = new Engineer(this, throttle);
        firePropertyChange("runMode", Integer.valueOf(MODE_NONE), Integer.valueOf(_runMode));
        runSignalControlledTrain();
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
        for (int i = 0; i < _commands.size(); i++) {
            ThrottleSetting ts = _commands.get(i);
            if (ts.getCommand().toUpperCase().equals("FORWARD")) {
                boolean isForward = Boolean.parseBoolean(ts.getValue());
                _engineer._throttle.setIsForward(isForward);
                log.debug(_trainName+" setTrainDirection - forward="+isForward);
                return;
            }
            log.debug(_trainName+" setTrainDirection could not determine direction.");
        }   
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
        for (int i = _idxCurrentOrder+1; i <= _orders.size()-1; i++) {
            BlockOrder bo = getBlockOrderAt(i);
            if (bo == null) {
                log.debug(_trainName+" getAndGetNotifiedFromNextSignal could not find a BlockOrder for index "+i);
            } else if (bo.getEntryName() == "") {
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
    public void setSpeedFromNextSignal () {
        String speed = null;
        if (_nextSignal == null) {
            _engineer.setSpeed(SPEED_MID);
        } else {
            if (_nextSignal instanceof SignalHead) {
                int appearance = ((SignalHead) _nextSignal).getAppearance();
                speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAppearanceSpeed(((SignalHead) _nextSignal).getAppearanceName(appearance));
            } else {
                String aspect = ((SignalMast) _nextSignal).getAspect();
                speed = jmri.InstanceManager.getDefault(SignalSpeedMap.class).getAspectSpeed(aspect, ((SignalMast) _nextSignal).getSignalSystem());
            }
            if (speed.equals("Stop")) {
                _engineer.setSpeed(SPEED_STOP);
            } else if (speed.equals("Normal")) {
                _engineer.setSpeed(SPEED_NORMAL);
            } else {
                _engineer.setSpeed(SPEED_MID);
            }
        }
    }
    
     /**
     * Do what the title says. But make sure not to set the turnouts if already done, since that 
     * would just cause all signals to go to Stop aspects and thus cause a jerky train movement.
     */
    protected void allocateBlocksAndSetTurnouts(int startIndex) {
        log.debug(_trainName+" allocateBlocksAndSetTurnouts startIndex="+startIndex+" _orders.size()="+_orders.size());
        ensureRouteConsecutivity();
        for (int i = startIndex; i < _orders.size(); i++) {
            log.debug(_trainName+" allocateBlocksAndSetTurnouts for loop #"+i);
            BlockOrder bo = getBlockOrderAt(i);
            OBlock block = bo.getBlock();
            String pathAlreadySet = block.isPathSet(bo.getPathName());
            if (pathAlreadySet == null) {
                String message = null;
                if ((block.getState() & OBlock.OCCUPIED) > 0) {
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
                log.debug(_trainName+" Path already set (and thereby block allocated) for "+bo.getPathName());
            } else {
                log.info(_trainName+" Block allocation failed: Path already set (and thereby block allocated) for "+bo.getPathName());
                return;
            }
        }
    }
    
    /**
     * This should not be necessary, but it turns out that if three or more trains are running on a small layout, 
     * at some point in time a deadlock will occur where more trains want to run on the same line in the same direction,
     * but the train behind have allocated a block ahead of the front train.
     * The task of this function is therefore to deallocate any such block.
     **/
    protected void ensureRouteConsecutivity () {
        boolean deAllocateRestOfRoute = false;
        for (int i = _idxCurrentOrder+1; i < _orders.size(); i++) {
            log.debug(_trainName+" ensureRouteConsecutivity for loop #"+i);
            BlockOrder bo = getBlockOrderAt(i);
            OBlock block = bo.getBlock();
            if (!block.isAllocatedTo(this) || (block.getState() & OBlock.OCCUPIED) > 0) {
                deAllocateRestOfRoute = true;
            }
            if (deAllocateRestOfRoute) {
                if (block.isAllocatedTo(this)) {
                    log.info(_trainName+" deallocating "+block.getDisplayName()+" due to risk of deadlock");
                    block.deAllocate(this);
                }
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
            return;
        }
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        if (_debug) {
            log.debug(_trainName+" **Block \"" + block.getDisplayName() + "\" goingActive. activeIdx= "
                    + activeIdx + ", _idxCurrentOrder= " + _idxCurrentOrder
                    + " - warrant= " + getDisplayName());
        }
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
        if (_debug) {
            log.debug(_trainName+" Block \"" + block.getDisplayName() + "\" goingInactive. idx= "
                    + idx + ", _idxCurrentOrder= " + _idxCurrentOrder
                    + " - warrant= " + getDisplayName());
        }
        if (idx < _idxCurrentOrder) {
            deallocateUpToBlock(idx);
        } else if (idx == _idxCurrentOrder) {
            // train is lost
            if (_debug) {
                log.debug(_trainName+" LOST TRAIN firePropertyChange(\"blockChange\", " + block.getDisplayName()
                                + ", null) - warrant= " + getDisplayName());
            }
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
                for (int j= _idxCurrentOrder; j<_orders.size(); j++) {
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
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            if (_debug) log.debug(_trainName+" propertyChange \""+evt.getPropertyName()+
                                                "\" old= "+evt.getOldValue()+" new= "+evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
        if (_debug) {
            log.debug(_trainName+" propertyChange \"" + property + "\" new= " + evt.getNewValue()
            		+ " source= " + ((NamedBean) evt.getSource()).getDisplayName()
                    + " - warrant= " + getDisplayName());
        }
        if (_nextSignal != null && _nextSignal == evt.getSource()) {
            if (property.equals("Aspect") || property.equals("Appearance")) {
                // The signal controlling this warrant has changed. Adjust the speed (in runSignalControlledTrain)
                synchronized(this) {
                    notify();
                }
                return;
            }
        }
        if (_stoppingBlock != null) {
            log.debug(_trainName+" CHECKING STOPPINGBLOCKEVENT ((NamedBean) evt.getSource()).getDisplayName() = '"+((NamedBean) evt.getSource()).getDisplayName()+"'");
            if (((NamedBean) evt.getSource()).getDisplayName().equals(_stoppingBlock.getDisplayName()) &&
                    evt.getPropertyName().equals("state") &&
                    (((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) == OBlock.UNOCCUPIED) {
                log.debug(_trainName+" being aware that Block "+((NamedBean) evt.getSource()).getDisplayName()+" has become free");
                _stoppingBlock.removePropertyChangeListener(this);
                _stoppingBlock = null;
                // we might be waiting for this block to become free
                synchronized(this) {
                    // Give the warrant that now has _stoppingBlock allocated a little time to deallocate it
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                    }
                    // And then let our main loop continue
                    notify();
                }
                return;
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
                while (_warrant._idxCurrentOrder < _orders.size()-1) {
                    log.debug(_warrant._trainName+" runSignalControlledTrain entering while loop. _idxCurrentOrder="+_idxCurrentOrder+" _orders.size()="+_orders.size());
                    if (_engineer == null) {
                        // Warrant was stopped
                        return;
                    }
                    allocateBlocksAndSetTurnouts(_warrant._idxCurrentOrder);
                    if (isNextBlockFreeAndAllocated()) {
                        getAndGetNotifiedFromNextSignal();
                        if (_debug) {
                            log.debug(_warrant._trainName+" runSignalControlledTrain lets train run according to signal ");
                            if (_nextSignal == null) {
                                log.debug(_warrant._trainName+" _nextSignal == null");
                            } else {
                                log.debug(_warrant._trainName+" _nextSignal = "+_nextSignal.getDisplayName());
                            }   
                        }
                        setSpeedFromNextSignal();
                    } else {
                        log.debug(_warrant._trainName+" runSignalControlledTrain stops train due to block not free.");
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
                                           _idxCurrentOrder+" _orders.size()="+_orders.size()+
                                           "  waiting for train to clear block "+getBlockAt(_orders.size()-2).getDisplayName());
                _engineer.setSpeed(SPEED_MID/2);
                while (!getBlockAt(_orders.size()-2).isFree() && getBlockAt(_orders.size()-2).isAllocatedTo(_warrant)) {
                    log.debug(_warrant._trainName+" runSignalControlledTrain entering wait. Block "+
                                     getBlockAt(_orders.size()-2).getDisplayName()+
                                     "   free: "+getBlockAt(_orders.size()-2).isFree()+
                                     "   allocated to this warrant: "+getBlockAt(_orders.size()-2).isAllocatedTo(_warrant));
                    try {
                        // This does not need to be a timed wait, since we will get interrupted once the block is free
                        // However, the functionality is more robust with a timed wait.
                        _warrant.wait(500);
                    } catch (InterruptedException ie) {
                        log.debug(_warrant._trainName+" InterruptedException "+ie);
                    }
                    log.debug(_warrant._trainName+" runSignalControlledTrain woken after last wait.... _orders.size()="+_orders.size());
                }
                if (timeToPlatform > 100) {
                    log.debug(_warrant._trainName+" runSignalControlledTrain is now fully into the stopping block. Proceeding for "+timeToPlatform+" miliseconds");
                    long timeWhenDone = System.currentTimeMillis() + timeToPlatform;
                    long remaining;
                    while ((remaining = timeWhenDone - System.currentTimeMillis()) > 0) {
                        try {
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
    static Logger log = LoggerFactory.getLogger(SCWarrant.class.getName());
}
