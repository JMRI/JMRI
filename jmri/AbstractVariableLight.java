// AbstractVariableLight.java

package jmri;
import javax.swing.Timer;
import java.util.Date;

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
 * @version     $Revision: 1.2 $
 */
public abstract class AbstractVariableLight extends AbstractLight
    implements java.io.Serializable {

    public AbstractVariableLight(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractVariableLight(String systemName) {
        super(systemName);
    }

    /**
     * Variables needed for saved values
     */
    protected double mTransitionDuration = 0.0;
    
    /**
     * Variables needed but not saved to files/panels
     */
    protected double mTransitionTargetIntensity = 0.0;
    
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
        mCurrentIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("TargetIntensity", new Double(oldValue), new Double(intensity));
        // must manually set state to setState(INTERMEDIATE), which is not allowed.
        int oldState = getState();
        mState = INTERMEDIATE;
        if (oldState!= INTERMEDIATE)
            firePropertyChange("KnownState", new Integer(oldState), new Integer(mState));
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
