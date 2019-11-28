package jmri.jmrit.logix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.PowerManager;
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
    private float speedFactor = 0.8f;
    private boolean forward = true;
    private final boolean _allowShallowAllocation = false;
    private DccThrottle _throttle = null;
    /**
     * Create an object with no route defined. The list of BlockOrders is the
     * route from an Origin to a Destination
     */
    public SCWarrant(String sName, String uName, long TTP) {
        super(sName, uName);
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
    
    public void setSpeedFactor(float factor) {
        if (factor > 1.0) {
            speedFactor = 1.0f;
        } else if (factor < 0.1) {
            speedFactor = 0.1f;
        } else {
            speedFactor = factor;
        }
    }
    
    public float getSpeedFactor() {
        return speedFactor;
    }
    
    float _maxBlockLength = 0;
    float getMaxBlockLength() {
        return _maxBlockLength;
    }
    void setMaxBlockLength() {
        float blockLength;
        for (int i=0; i <= getBlockOrders().size()-1; i++) {
            blockLength = getBlockOrderAt(i).getBlock().getLengthCm();
            if (blockLength > _maxBlockLength) {
                _maxBlockLength = blockLength;
            }
        }
    }
    
    private String allocateStartBlock() {
        BlockOrder bo = getBlockOrderAt(0);
        OBlock block = bo.getBlock();
        String message = block.allocate(this);
        if (message != null) {
           log.info("{} START-block allocation failed {} ",_trainName,message);
           return message;
        }
        message = bo.setPath(this);
        if (message != null) {
           log.info("{} setting path in START-block failed {}",_trainName,message);
           return message;
        }
        return null;
    }

    /**
     * This method has been overridden in order to avoid allocation of occupied blocks.
     */
    @Override
     public String setRoute(boolean delay, List<BlockOrder> orders) {
        return allocateStartBlock();
    }

    boolean allTurnoutsSet() {
        for (int i=0; i<getBlockOrders().size(); i++) {
            OBlock block_i = getBlockOrderAt(i).getBlock();
            OPath  path_i  = getBlockOrderAt(i).getPath();
            if (!path_i.checkPathSet()) {
                log.debug("{}: turnouts at block "+block_i.getDisplayName()+" are not set yet (in allTurnoutsSet).",_trainName);
                return false;
            }
        }
        return true;
    }

    public boolean isRouteFree() {
        for (int i=0; i<getBlockOrders().size(); i++) {
            OBlock block_i = getBlockOrderAt(i).getBlock();
            if ((block_i.getState() & OBlock.ALLOCATED) == OBlock.ALLOCATED) {
                log.debug("{}: block "+block_i.getDisplayName()+" is allocated to "+block_i.getAllocatingWarrantName()+" (in isRouteFree).",_trainName);
                if (!block_i.isAllocatedTo(this)) {
                    return false;
                }
            }
            if ( ((block_i.getState() & OBlock.OCCUPIED) == OBlock.OCCUPIED) && (i>0) ) {
                log.debug("{}: block "+block_i.getDisplayName()+" is not free (in isRouteFree).",_trainName);
                return false;
            }
        }
        return true;
    }

    boolean isRouteAllocated() {
        for (int i=0; i<getBlockOrders().size(); i++) {
            OBlock block_i = getBlockOrderAt(i).getBlock();
            if (!block_i.isAllocatedTo(this)) {
                log.debug("{}: block {} is not allocated to this warrant (in isRouteAllocated).",_trainName,block_i.getDisplayName());
                return false;
            }
        }
        return true;
    }
    
    /**
     * Callback from acquireThrottle() when the throttle has become available.sync
     */
    @Override
    public void notifyThrottleFound(DccThrottle throttle) {
        _throttle = throttle;
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
        log.debug("{} notifyThrottleFound address= {} _runMode= {}",_trainName,throttle.getLocoAddress(),_runMode);
        
        startupWarrant();

        firePropertyChange("runMode", Integer.valueOf(MODE_NONE), Integer.valueOf(_runMode));
        runSignalControlledTrain();
    }

    /**
     * Generate status message to show in warrant table
     **/
    @Override
    synchronized protected String getRunningMessage() {
        if (_throttle == null) {
            // The warrant is not active
            return super.getRunningMessage();
        } else if (_runMode != MODE_RUN) {
            return ("Idle");
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
            return Bundle.getMessage("SCWStatus", block, _idxCurrentOrder, _throttle.getSpeedSetting(),signal,aspect);
        }
    }

    /******************************************************************************************************
     * Use _throttle to control the train.
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
            log.debug("{} waiting for start block {} to become occupied",_trainName,getBlockOrderAt(0).getBlock().getDisplayName());
            try {
                // We will not be woken up by goingActive, since we have not allocated the start block yet.
                // So do a timed wait.
                wait(2500);
            } catch (InterruptedException ie) {
                log.debug("{} waitForStartblockToGetOccupied InterruptedException {}",_trainName,ie,ie);
            }
            catch(Exception e){
                log.debug("{} waitForStartblockToGetOccupied unexpected exception {}",_trainName,e,e);
            }
        }
    }
    
    /**
     * Set this train to run backwards or forwards as specified in the command list.
     */
    public void setTrainDirection () {
        _throttle.setIsForward(forward);
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
            log.debug("{} getAndGetNotifiedFromNextSignal removing property listener for signal {}",_trainName,_nextSignal.getDisplayName());
            _nextSignal.removePropertyChangeListener(this);
            _nextSignal = null;
        }
        for (int i = _idxCurrentOrder+1; i <= getBlockOrders().size()-1; i++) {
            BlockOrder bo = getBlockOrderAt(i);
            if (bo == null) {
                log.debug("{} getAndGetNotifiedFromNextSignal could not find a BlockOrder for index {}",_trainName,i);
            } else if (bo.getEntryName().equals("")) {
                log.debug("{} getAndGetNotifiedFromNextSignal could not find an entry to Block for index {}",_trainName,i);
            } else {
                log.debug("{} getAndGetNotifiedFromNextSignal examines block {} with entryname = {}",_trainName,bo.getBlock().getDisplayName(),bo.getEntryName());
                _nextSignal = bo.getSignal();
                if (_nextSignal != null) {
                    log.debug("{} getAndGetNotifiedFromNextSignal found a new signal to listen to: {}",_trainName,_nextSignal.getDisplayName());
                    break;
                }
            }
        }
        if (_nextSignal != null) {
            _nextSignal.addPropertyChangeListener(this);
        }
    }
    
    /**
     * Are we still in the start block?
     */
    boolean inStartBlock() {
        if (_idxCurrentOrder == 0) {
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Are we close to the destination block?
     */
    boolean approchingDestination() {
        float distance = 0;
        float blockLength;
        if (_idxCurrentOrder == getBlockOrders().size()-2) {
            // We are in the block just before destination
            return true;
        }
        // Calculate the distance to destination
        for (int i = _idxCurrentOrder; i <= getBlockOrders().size()-2; i++) {
            blockLength = getBlockOrderAt(i).getBlock().getLengthCm();
            if (blockLength < 1) {
                // block length not set for at least one block
                return false;
            }
            distance += blockLength;
        }
        if (distance < 1.5*getMaxBlockLength()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Move the train if _nextSignal permits. If there is no next signal, we will move forward with half speed.
     */
    SignalSpeedMap _speedMap = jmri.InstanceManager.getDefault(SignalSpeedMap.class);
    public void setSpeedFromNextSignal () {
        String speed = null;
        if (_nextSignal == null) {
            _throttle.setSpeedSetting(speedFactor*SPEED_UNSIGNALLED);
        } else {
            if (_nextSignal instanceof SignalHead) {
                int appearance = ((SignalHead) _nextSignal).getAppearance();
                speed = _speedMap.getAppearanceSpeed(((SignalHead) _nextSignal).getAppearanceName(appearance));
                log.debug("{} SignalHead {} shows appearance {} which maps to speed {}",_trainName,((SignalHead) _nextSignal).getDisplayName(),appearance,speed);
            } else {
                String aspect = ((SignalMast) _nextSignal).getAspect();
                speed = _speedMap.getAspectSpeed(aspect, ((SignalMast) _nextSignal).getSignalSystem());
                log.debug(" SignalMast {} shows aspect {} which maps to speed ",_trainName,((SignalMast) _nextSignal).getDisplayName(),aspect,speed);
            }
            float speed_f = (float) (_speedMap.getSpeed(speed) / 125.);
            // Ease the speed, if we are approaching the destination block
            if ((approchingDestination() || inStartBlock()) && (speed_f > SPEED_UNSIGNALLED)) {
                speed_f = SPEED_UNSIGNALLED;
            }
            _throttle.setSpeedSetting(speedFactor*speed_f);
        }
    }
    
     /**
     * Do what the title says. But make sure not to set the turnouts if already done, since that 
     * would just cause all signals to go to Stop aspects and thus cause a jerky train movement.
     */
    protected void allocateBlocksAndSetTurnouts(int startIndex) {
        log.debug("{} allocateBlocksAndSetTurnouts startIndex={} _orders.size()={}",_trainName,startIndex,getBlockOrders().size());
        for (int i = startIndex; i < getBlockOrders().size(); i++) {
            log.debug("{} allocateBlocksAndSetTurnouts for loop #{}",_trainName,i);
            BlockOrder bo = getBlockOrderAt(i);
            OBlock block = bo.getBlock();
            String pathAlreadySet = block.isPathSet(bo.getPathName());
            if (pathAlreadySet == null) {
                String message = null;
                if ((block.getState() & OBlock.OCCUPIED) != 0) {
                    log.info("{} block allocation failed {} not allocated, but Occupied.",_trainName,block.getDisplayName());
                    message = " block allocation failed ";
                }
                if (message == null) {
                    message = block.allocate(this);
                    if (message != null) {
                        log.info("{} block allocation failed {}",_trainName,message);
                    }
                }
                if (message == null) {
                    message = bo.setPath(this);
                }
                if (message != null) {
                    log.debug("{} path setting failed for {} at block {} {}",_trainName,getDisplayName(),block.getDisplayName(),message);
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
                log.debug("{} Path {} already set (and thereby block allocated) for {}",_trainName,bo.getPathName(),pathAlreadySet);
            } else {
                log.info("{} Block allocation failed: Path {} already set (and thereby block allocated) for {}",_trainName,bo.getPathName(),pathAlreadySet);
                return;
            }
        }
    }
    
    /**
     * Block in the route going active.
     * Make sure to allocate the rest of the route, update our present location and then tell
     * the main loop to find a new throttle setting.
     */
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="NotifyAll call triggers recomputation")
    protected void goingActive(OBlock block) {
        int activeIdx = getIndexOfBlock(block, _idxCurrentOrder);
        log.debug("{} **Block \"{}\" goingActive. activeIdx= {}"
                    + ", _idxCurrentOrder= {}" 
                    + " - warrant= {} _runMode = {} _throttle==null: {}",_trainName,block.getDisplayName(),activeIdx,_idxCurrentOrder,getDisplayName(),_runMode,(_throttle==null));
        if (_runMode != MODE_RUN) {
            // if we are not running, we must not think that we are going to the next block - it must be another train
            return;
        }
        if (_throttle == null || _throttle.getSpeedSetting() == SPEED_STOP) {
            // if we are not running, we must not think that we are going to the next block - it must be another train
            return;
        }
        if (activeIdx <= 0) {
            // The block going active is not part of our route ahead
            log.debug("{} Block going active is not part of this trains route forward",_trainName);
        } else if (activeIdx == _idxCurrentOrder) {
            // Unusual case of current block losing detection, then regaining it.  i.e. dirty track, derail etc.
            log.debug("{} Current block becoming active - ignored",_trainName);
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
            log.debug("{} Rogue occupation of block.",_trainName);
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
    @Override
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "NN_NAKED_NOTIFY", justification="See comment above notify call")
    protected void goingInactive(OBlock block) {
        int idx = getIndexOfBlock(block, 0);  // if idx >= 0, it is in this warrant
        log.debug("{} Block \"{}\" goingInactive. idx= {}"
                    + ", _idxCurrentOrder= {}"
                    + " - warrant= {}",_trainName,block.getDisplayName(),idx,_idxCurrentOrder,getDisplayName());
        if (_runMode != MODE_RUN) {
            return;
        }
        if (idx < _idxCurrentOrder) {
            if (_allowShallowAllocation) {
                deallocateUpToBlock(idx);
            }
        } else if (idx == _idxCurrentOrder) {
            // train is lost
            log.debug("{} LOST TRAIN firePropertyChange(\"blockChange\", {}"
                                + ", null) - warrant= {}",_trainName,block.getDisplayName(),getDisplayName());
//            firePropertyChange("blockChange", block, null);
//            emergencyStop();
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
                    log.debug("{} Block {} occupied. Not de-allocating any further",_trainName,block_i.getDisplayName());
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
                    log.debug("{} De-allocating block {}",_trainName,block_i.getDisplayName());
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
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = {"UW_UNCOND_WAIT", "NN_NAKED_NOTIFY"}, 
            justification = "Unconditional wait is give the warrant that now has _stoppingBlock allocated a little time to deallocate it.  This occurs after this method sets _stoppingBlock to null. NotifyAll passing event, not state.")
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent evt) {
        if (!(evt.getSource() instanceof NamedBean)) {
            log.debug("{} propertyChange \"{}\" old= {} new= {}",_trainName,evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
            return;
        }
        String property = evt.getPropertyName();
        log.debug("{} propertyChange \"{}\" new= {} source= {} - warrant= {}",_trainName,property,evt.getNewValue(),((NamedBean) evt.getSource()).getDisplayName(),getDisplayName());
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
                log.debug("{} CHECKING STOPPINGBLOCKEVENT ((NamedBean) evt.getSource()).getDisplayName() = '{}' evt.getPropertyName() = '{}' evt.getNewValue() = {} _throttle==null: {}",_trainName,((NamedBean) evt.getSource()).getDisplayName(),evt.getPropertyName(),evt.getNewValue(),(_throttle==null));
                if (((NamedBean) evt.getSource()).getDisplayName().equals(_stoppingBlock.getDisplayName()) &&
                        evt.getPropertyName().equals("state") &&
                        (((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) == OBlock.UNOCCUPIED) {
                    log.debug("{} being aware that Block {} has become free",_trainName,((NamedBean) evt.getSource()).getDisplayName());
                    _stoppingBlock.removePropertyChangeListener(this);
                    _stoppingBlock = null;
                    // we might be waiting for this block to become free
                    // Give the warrant that now has _stoppingBlock allocated a little time to deallocate it
                    try {
                        wait(100);
                    } catch (InterruptedException e) {
                    } catch(Exception e){
                        log.debug("{} wait unexpected exception {}",_trainName,e,e);
                    }
                    // And then let our main loop continue
                    notify();
                    return;
                }
                if (((NamedBean) evt.getSource()).getDisplayName().equals(getBlockOrderAt(0).getBlock().getDisplayName()) &&
                        evt.getPropertyName().equals("state") &&
                        (((Number) evt.getOldValue()).intValue() & OBlock.UNOCCUPIED) == OBlock.UNOCCUPIED &&
                        (((Number) evt.getNewValue()).intValue() & OBlock.UNOCCUPIED) != OBlock.UNOCCUPIED &&
                        _throttle==null && _runMode==MODE_RUN) {
                    // We are waiting for the train to arrive at the starting block, and that has just happened now.
                    log.debug("{} has arrived at starting block",_trainName);
                    String msg = null;
                    msg = acquireThrottle();
                    if (msg != null) {
                        log.warn("propertyChange of \"{}\" has message: {}", property, msg);
                        _message = msg;
                        abortWarrant(msg);
                    }
                }
            }
        }
    }
    
    
    /**
     * Make sure to free up additional resources for a running SCWarrant.
     */
    @Override
    synchronized public void stopWarrant(boolean abort) {
        if (_nextSignal != null) {
            _nextSignal.removePropertyChangeListener(this);
            _nextSignal = null;
        }
        super.stopWarrant(abort, false);
        _message = null;
    }
    
    /**
     * If we think we might have a runaway train - take the power of the entire layout.
     */
    private void emergencyStop() {
        PowerManager manager = InstanceManager.getNullableDefault(jmri.PowerManager.class);
        if (manager == null) {
            log.debug("{} EMERGENCY STOP IMPOSSIBLE: NO POWER MANAGER",_trainName);
            return;
        }
        try {
            manager.setPower(PowerManager.OFF);
        } catch (Exception e) {
            log.debug("{} EMERGENCY STOP FAILED WITH EXCEPTION: {}",_trainName,e,e);
        }
        log.debug("{} EMERGENCY STOP",_trainName);
    }
    
    /*******************************************************************************************************************************
     * The waiting for event must happen in a separate thread.
     * Therefore the main code of runSignalControlledTrain is put in this class.
     *******************************************************************************************************************************/
    static LinkedBlockingQueue<SCWarrant> waitToRunQ = new LinkedBlockingQueue<>();
    private class SCTrainRunner implements Runnable {
        SCWarrant _warrant = null;
        SCTrainRunner(SCWarrant warrant) {
            _warrant = warrant;
        }
        
        /**
         * When not using shallow allocation, warrants will have to wait until the entire route
         * is free and allocated to that particular warrant, before strting to run the train.
         * This method uses the waitToRunQ to ensure that warrants do not just compete about
         * resources, but waits in line until their route is free and unallocated.
         */
        boolean isItOurTurn() {
            for (SCWarrant e : waitToRunQ) {
                try { // using another SCWarrant might be dangerous - it might no longer exist
                    log.debug("{} isItOurTurn is checking {}",_trainName,e.getDisplayName());
                    if (e.isRouteFree()) {
                        if (e == _warrant) {
                            log.debug("{} isItOurTurn: We are first in line",_trainName);
                            return true;
                        } else {
                            log.debug("{} isItOurTurn: An other warrant is before us",_trainName);
                            return false;
                        }
                    } else {
                        if (e == _warrant) {
                            log.debug("{} isItOurTurn: our route is not free - keep waiting",_trainName);
                            return false;
                        }
                    }
                } catch (Exception ex) {
                    log.debug("{} isItOurTurn exception ignored: {}",_trainName,ex,ex);
                }
            }
            // we should not reach this point, but if we do, we should try to run
            log.debug("{} isItOurTurn: No warrant with a free route is waiting. Let us try our luck, so that we are not all waiting for each other.",_trainName);
            return true;
        }

        @Override
        public void run() {
            synchronized(_warrant) {

                // Make sure the entire route is allocated before attemting to start the train
                if (!_allowShallowAllocation) {
                    boolean AllocationDone = false;
                    log.debug("{} ENTERING QUEUE ",_trainName);
                    try {
                        waitToRunQ.put(_warrant);
                    } catch (InterruptedException ie) {
                        log.debug("{} waitToRunQ.put InterruptedException {}",_trainName,ie,ie);
                    }

                    while (!AllocationDone) {
                        log.debug("{} Route is not allocated yet..... ",_trainName);
                        while (!isItOurTurn()) {
                            deAllocate();
                            log.debug("{} Waiting for route to become free ....",_trainName);
                            try {
                                _warrant.wait(2500 + Math.round(1000*Math.random()));
                            } catch (InterruptedException ie) {
                                log.debug("{} _warrant.wait InterruptedException {}",_trainName,ie,ie);
                            }
                            catch(Exception e){
                                log.debug("{} _warrant.wait unexpected exception {}",_trainName,e,e);
                            }
                        }
                        allocateStartBlock();
                        allocateBlocksAndSetTurnouts(1);
                        AllocationDone = isRouteAllocated();
                        if (!AllocationDone) {
                            deAllocate();
                            try {
                                _warrant.wait(10000 + Math.round(1000*Math.random()));
                            } catch (InterruptedException ie) {
                                log.debug("{} _warrant.wait !AllocationDone InterruptedException {}",_trainName,ie,ie);
                            }
                            catch(Exception e){
                                log.debug("{} _warrant.wait !AllocationDone unexpected exception {}",_trainName,e,e);
                            }
                        }
                    }

                    log.debug("{} LEAVING QUEUE ",_trainName);
                    waitToRunQ.remove(_warrant);

                    while (!allTurnoutsSet()) {
                        log.debug("{} Waiting for turnouts to settle ....",_trainName);
                        try {
                            _warrant.wait(2500);
                        } catch (InterruptedException ie) {
                            log.debug("{} _warrant.wait InterruptedException {}",_trainName,ie,ie);
                        }
                        catch(Exception e){
                            log.debug("{} _warrant.wait unexpected exception {}",_trainName,e,e);
                        }
                    }
                    // And then wait another 3 seconds to make the last turnout settle - just in case the command station is not giving correct feedback
                    try {
                        _warrant.wait(3000);
                    } catch (InterruptedException ie) {
                        log.debug("{} InterruptedException {}",_trainName,ie,ie);
                    }
                    catch(Exception e){
                        log.debug("{} wait unexpected exception {}",_trainName,e,e);
                    }
                }

                // Do not include the stopping block in this while loop. It will be handled after the loop.
                List<BlockOrder> orders = getBlockOrders();
                while (_warrant._idxCurrentOrder < orders.size()-1 && _runMode == MODE_RUN) {
                    log.debug("{} runSignalControlledTrain entering while loop. _idxCurrentOrder={} _orders.size()={}",_warrant._trainName,_idxCurrentOrder,orders.size());
                    if (_throttle == null) {
                        // We lost our throttle, so we might have a runaway train
                        emergencyStop();
                    }
                    if (_allowShallowAllocation) {
                        allocateBlocksAndSetTurnouts(_warrant._idxCurrentOrder);
                    }
                    if (isNextBlockFreeAndAllocated()) {
                        getAndGetNotifiedFromNextSignal();
                        setSpeedFromNextSignal();
                    } else {
                        try {
                            _throttle.setSpeedSetting(SPEED_STOP);
                            getBlockOrderAt(_idxCurrentOrder+1).getBlock().addPropertyChangeListener(_warrant);
                            log.debug("{} runSignalControlledTrain stops train due to block not free: {}",_warrant._trainName,getBlockOrderAt(_idxCurrentOrder+1).getBlock().getDisplayName());
                        } catch (Exception e) {
                            emergencyStop();
                            log.debug("{} exception trying to stop train due to block not free: {}",_warrant._trainName,e,e);
                        }
                    }
                    log.debug("{} {} before wait {} _idxCurrentOrder: {} orders.size(): {}",_warrant._trainName,_warrant.getDisplayName(),_warrant.getRunningMessage(),_warrant._idxCurrentOrder,orders.size());
                    try {
                        // We do a timed wait for the sake of robustness, even though we will be woken up by all relevant events.
                        _warrant.wait(2000);
                    } catch (InterruptedException ie) {
                        log.debug("{} InterruptedException {}",_warrant._trainName,ie,ie);
                    }
                    catch(Exception e){
                        log.debug("{} wait unexpected exception {}",_trainName,e,e);
                    }
                    log.debug("{} {} after wait {} _idxCurrentOrder: {} orders.size(): {}",_warrant._trainName,_warrant.getDisplayName(),_warrant.getRunningMessage(),_warrant._idxCurrentOrder,orders.size());
                }
                // We are now in the stop block. Move forward for half a second with half speed until the block before the stop block is free.
                log.debug("{} runSignalControlledTrain out of while loop, i.e. train entered stop block _idxCurrentOrder={}"
                          + " orders.size()={} waiting for train to clear block {}",
                          _warrant._trainName,_idxCurrentOrder,orders.size(),getBlockAt(orders.size()-2).getDisplayName());
                if (_throttle==null) {
                    emergencyStop();
                    log.debug("Throttle lost at stop block");
                } else {
                    _throttle.setSpeedSetting(speedFactor*SPEED_TO_PLATFORM);
                }
                while ((getBlockAt(orders.size()-2).getState()&OBlock.OCCUPIED)==OBlock.OCCUPIED && getBlockAt(orders.size()-2).isAllocatedTo(_warrant)) {
                    log.debug(" runSignalControlledTrain entering wait. Block {}" 
                              +"   free: {}   allocated to this warrant: {}",
                              _warrant._trainName,getBlockAt(orders.size()-2).getDisplayName(),getBlockAt(orders.size()-2).isFree(),getBlockAt(orders.size()-2).isAllocatedTo(_warrant));
                    try {
                        // This does not need to be a timed wait, since we will get interrupted once the block is free
                        // However, the functionality is more robust with a timed wait.
                        _warrant.wait(500);
                    } catch (InterruptedException ie) {
                        log.debug("{} InterruptedException {}",_warrant._trainName,ie,ie);
                    }
                    catch(Exception e){
                        log.debug("{} wait unexpected exception {}",_trainName,e,e);
                    }
                    log.debug("{} runSignalControlledTrain woken after last wait.... _orders.size()={}",_warrant._trainName,orders.size());
                }
                if (timeToPlatform > 100) {
                    log.debug("{} runSignalControlledTrain is now fully into the stopping block. Proceeding for {} miliseconds",_warrant._trainName,timeToPlatform);
                    long timeWhenDone = System.currentTimeMillis() + timeToPlatform;
                    long remaining;
                    while ((remaining = timeWhenDone - System.currentTimeMillis()) > 0) {
                        try {
                            log.debug("{} running slowly to platform for {} miliseconds",_warrant._trainName,remaining);
                            _warrant.wait(remaining);
                        } catch (InterruptedException e) {
                            log.debug("{} InterruptedException {}",_warrant._trainName,e,e);
                        }
                    }
                }
                log.debug("{} runSignalControlledTrain STOPPING TRAIN IN STOP BLOCK",_warrant._trainName);
                if (_throttle==null) {
                    emergencyStop();
                    log.debug("Throttle lost after stop block");
                } else {
                    _throttle.setSpeedSetting(SPEED_STOP);
                }
                stopWarrant(false);
            }
        }
    }
    
    /* What super does currently is fine. But FindBug reports EQ_DOESNT_OVERRIDE_EQUALS
     * FindBug wants us to duplicate and override anyway
     */
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    /* What super does currently is fine. But FindBug reports HE_EQUALS_NO_HASHCODE
     * FindBug wants us to duplicate and override anyway
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    /**
     * 
     */
    private final static Logger log = LoggerFactory.getLogger(SCWarrant.class);
}
