// AbstractSignalMast.java

package jmri.implementation;

import jmri.*;

 /**
 * Abstract class providing the basic logic of the SignalMast interface.
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 * @version     $Revision: 1.5 $
 */
public abstract class AbstractSignalMast extends AbstractNamedBean
    implements SignalMast, java.io.Serializable {

    public AbstractSignalMast(String systemName, String userName) {
        super(systemName, userName);
    }

    public AbstractSignalMast(String systemName) {
        super(systemName);
    }
      
    public void setAspect(String aspect) { 
        String oldAspect = this.aspect;
        this.aspect = aspect;
        this.speed = (String)getSignalSystem().getProperty(aspect, "speed");
        firePropertyChange("Aspect", oldAspect, aspect);
    }
    public String getAspect() { return aspect; }
    String aspect = null;
    
    public String getSpeed() { return speed; }
    String speed = null;

    /**
     * The state is the index of the current aspect
     * in the list of possible aspects.
     */
    public int getState() {
        return -1;
    }
    public void setState(int i) {
    }
}

/* @(#)AbstractSignalMast.java */
