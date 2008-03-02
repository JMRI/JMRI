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
 * @version     $Revision: 1.1 $
 */
public abstract class AbstractVariableLight extends AbstractLight
    implements java.io.Serializable {

    public AbstractVariableLight(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractVariableLight(String systemName) {
        super(systemName);
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
        mCurrentIntensity = intensity;
        if (oldValue != intensity)
            firePropertyChange("TargetIntensity", new Double(oldValue), new Double(intensity));
        // must manually set state to setState(INTERMEDIATE), which is not allowed.
        int oldState = getState();
        mState = INTERMEDIATE;
        if (oldState!= INTERMEDIATE)
            firePropertyChange("KnownState", new Integer(oldState), new Integer(mState));
    }

}

/* @(#)AbstractVariableLight.java */
