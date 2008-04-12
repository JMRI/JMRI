// AbstractVariableLight.java

package jmri;
import javax.swing.Timer;
import java.util.Date;

import jmri.jmrix.powerline.SerialTrafficController;

 /**
 * Abstract class providing partial implementation of the logic
 * of the Light interface when the Intensity is variable.
 * <p>
 * Eventually, this class will include transition code, but 
 * it isn't here yet, so the default setTransitionRate()
 * implementation is inherited from AbstractLight
 *
 * @author	Dave Duchamp Copyright (C) 2004
 * @author	Ken Cameron Copyright (C) 2008
 * @author	Bob Jacobsen Copyright (C) 2008
 * @version     $Revision: 1.5 $
 */
public abstract class AbstractVariableLight extends AbstractLight
    implements java.io.Serializable {

    public AbstractVariableLight(String systemName, String userName) {
        super(systemName, userName);
        if (internalClock == null) {
            initClocks();
        }
    }

    public AbstractVariableLight(String systemName) {
        super(systemName);
        if (internalClock == null) {
            initClocks();
        }
    }

    /**
     * Variables needed for saved values
     */
    protected double mTransitionDuration = 0.0;
    
    /**
     * Variables needed but not saved to files/panels
     */
    protected double mTransitionTargetIntensity = 0.0;
    protected Date mLastTransitionDate = null;
    protected long mNextTransitionTs = 0;
    protected Timebase internalClock = null;
    protected javax.swing.Timer alarmSyncUpdate = null;
    protected java.beans.PropertyChangeListener minuteChangeListener = null;
    
    /**
     * setup internal clock, start minute listener
     */
    private void initClocks(){
        // Create a Timebase listener for the Minute change events
        internalClock = InstanceManager.timebaseInstance();
        if (internalClock == null){
            log.error("No Timebase Instance");
        }
        minuteChangeListener = new java.beans.PropertyChangeListener() {
                public void propertyChange(java.beans.PropertyChangeEvent e) {
                    newInternalMinute();
                }
            } ;
        if (minuteChangeListener == null){
            log.error("No minuteChangeListener");
        }
        internalClock.addMinuteChangeListener(minuteChangeListener);
    }
    
    private void newInternalMinute() {
    	double origCurrent = mCurrentIntensity;
    	int origState = mState;
    	if ((Math.abs(mCurrentIntensity - mTransitionTargetIntensity) > 0.009) && (mTransitionDuration > 0)) {
    		if (log.isDebugEnabled()) {
    			log.debug("before Target: " + mTransitionTargetIntensity + " Current: " + mCurrentIntensity);
    		}
    		Date now = internalClock.getTime();
    		int steps = SerialTrafficController.instance().maxX10DimStep();
        	double stepsPerMinute = steps / mTransitionDuration;
        	double minutesPerStep = 1 / stepsPerMinute;
        	double timeUntilMinute = now.getSeconds() / 60.0;
        	double absIntensityDiff = Math.abs(mTransitionTargetIntensity - mCurrentIntensity);
        	double stepSize = 1 / (double)steps;
        	double stepsNeeded = absIntensityDiff / stepSize;
        	double intensityDiffPerMinute = stepSize * stepsPerMinute;
//        	if (log.isDebugEnabled()) {
//        		log.debug("step/min " + stepsPerMinute + " min/step " + minutesPerStep + " absDiff " + absIntensityDiff + " step " + stepSize + " steps " + stepsNeeded + " diff/min " + intensityDiffPerMinute);
//        	}
			if (mTransitionTargetIntensity > mCurrentIntensity) {
				mState = TRANSITIONINGHIGHER;
				mCurrentIntensity = mCurrentIntensity + intensityDiffPerMinute;
				if (mCurrentIntensity > mTransitionTargetIntensity) {
					mCurrentIntensity = mTransitionTargetIntensity;
					mState = INTERMEDIATE;
				}
			} else {
				mState = TRANSITIONINGLOWER;
				mCurrentIntensity = mCurrentIntensity - intensityDiffPerMinute;
				if (mCurrentIntensity < mTransitionTargetIntensity) {
					mCurrentIntensity = mTransitionTargetIntensity;
					mState = INTERMEDIATE;
				}
			}
    		if (log.isDebugEnabled()){
        		log.debug("after Target: " + mTransitionTargetIntensity + " Current: " + mCurrentIntensity);
    		}
    	}
    	if (origCurrent != mCurrentIntensity) {
            firePropertyChange("IntensityChange", new Double(origCurrent), new Double(mCurrentIntensity));
    		if (log.isDebugEnabled()){
        		log.debug("firePropertyChange intensity " + origCurrent + " -> " + mCurrentIntensity);
    		}
    	}
    	if (origState != mState) {
            firePropertyChange("StateChange", new Integer(origState), new Integer(mState));
    		if (log.isDebugEnabled()){
        		log.debug("firePropertyChange intensity " + origCurrent + " -> " + mCurrentIntensity);
    		}
    	}
    }
    /** Check if this object can handle variable intensity.
     * <P>
     * @return true, as this abstract class implements
     * variable intensity.
    */
    public boolean isIntensityVariable() {
        return true;
    }
    
    /**
     * Method for further implementation of 
     * setTargetIntensity between min and max
     */
    protected void updateIntensityIntermediate(double intensity) {
        double oldValue = mCurrentIntensity;
        // got to intermediate state
        mTransitionTargetIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("TargetIntensity", new Double(oldValue), new Double(intensity));
        // must manually set state to setState(INTERMEDIATE), which is not allowed.
        int oldState = getState();
        mState = INTERMEDIATE;
        if (oldState!= INTERMEDIATE)
            firePropertyChange("KnownState", new Integer(oldState), new Integer(mState));
        if (log.isDebugEnabled()) {
        	log.debug("change dim: " + oldValue + " to " + intensity);
        }
        if (mTransitionDuration != 0) {
        	mLastTransitionDate = internalClock.getTime();
        	newInternalMinute();
        } else {
        	mCurrentIntensity = intensity;
        }
    }

    /** 
     * Can the Light change it's intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the 
     * transitionTime property, which controls how long the Light
     * will take to change from one intensity level to another.
     * <p>
     * Unbound property
     */
    public boolean isTransitionAvailable() { return true; }
    
    /** 
     * Set the fast-clock duration for a 
     * transition from full ON to full OFF or vice-versa.
     * <P>
     * Bound property
     * <p>
     * @throws IllegalArgumentException if minutes is not valid
     */
    public void setTransitionTime(double minutes) {
        if (minutes < 0.0) throw new IllegalArgumentException("Invalid transition time: " + minutes);
        mTransitionDuration = minutes;
    }
    
    /** 
     * Get the number of fastclock minutes taken by a transition from
     * full ON to full OFF or vice versa.
     * <p>
     * @return 0.0 if the output intensity transition is instantaneous
     */
    public double getTransitionTime() { return mTransitionDuration; }
    
    /** 
     * Convenience method for checking if the intensity of the light is currently
     * changing due to a transition.
     * <p>
     * Bound property so that listeners can conveniently learn when the transition is over.
     */
    public boolean isTransitioning() {
    	if (mTransitionTargetIntensity != mCurrentIntensity) {
    		return true;
    	} else {
    		return false;
    	}
    }

}

/* @(#)AbstractVariableLight.java */
