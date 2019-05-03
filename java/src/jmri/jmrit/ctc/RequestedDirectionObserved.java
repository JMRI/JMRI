/*
 *  @author Gregory J. Bedlek Copyright (C) 2018, 2019
 */
package jmri.jmrit.ctc;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import jmri.jmrit.ctc.CTCConstants;

/**
 * This object ONLY maintains ONE property: _mRequestedDirection.  Ergo no need
 * to call "addPropertyChangeListener" form with the "propertyName" variant.
 */
public class RequestedDirectionObserved {
    private int _mRequestedDirection;
    private final PropertyChangeSupport _mPropertyChangeSupport = new PropertyChangeSupport(this);
    private final static String PROPERTY = "RequestedDirection";  // NOI18N  NEVER pass "null" for propertyName, there is a bug relating to this (for safety!)
    
    public RequestedDirectionObserved() { _mRequestedDirection = CTCConstants.OUTOFCORRESPONDENCE; }    // Obviously nothing could have registered with us YET!
    public void addPropertyChangeListener(PropertyChangeListener pcl) { _mPropertyChangeSupport.addPropertyChangeListener(pcl); }
    public void removePropertyChangeListener(PropertyChangeListener pcl) { _mPropertyChangeSupport.removePropertyChangeListener(pcl); }
    public void setRequestedDirection(int newRequestedDirection) {
        _mRequestedDirection = newRequestedDirection;   // In case user directly asks us instead of using the following values:
        _mPropertyChangeSupport.firePropertyChange(PROPERTY, this._mRequestedDirection, newRequestedDirection);  // Per documentation: NO event fired if NO change in value!
    }
    public int getRequestedDirection() { return _mRequestedDirection; }
}
