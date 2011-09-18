// AbstractQualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Watches a specific Variable to qualify others.
 *
 * @author			Bob Jacobsen   Copyright (C) 2010
 * @version			$Revision$
 *
 */
public abstract class AbstractQualifier implements Qualifier, java.beans.PropertyChangeListener {

    public AbstractQualifier(VariableValue qualifiedVal, VariableValue watchedVal) {
        this.qualifiedVal = qualifiedVal;

        // set up listener
        watchedVal.addPropertyChangeListener(this);
        
        // subclass ctors are required to qualify on initial value of variable        
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("Value"))
            processValueChangeEvent(e);
    }
    
    void processValueChangeEvent(java.beans.PropertyChangeEvent e) {
        boolean oldAvailableValue = qualifiedVal.getAvailable();
        // watched value changed, check
        boolean newAvailableValue = availableStateFromEvent(e);
        
        if (oldAvailableValue != newAvailableValue)
            setWatchedAvailable(newAvailableValue);
    }
    
    protected boolean availableStateFromEvent(java.beans.PropertyChangeEvent e) {
        return availableStateFromObject(e.getNewValue());
    }
    
    abstract protected boolean availableStateFromObject(Object o);
    abstract protected boolean availableStateFromValue(int value);
    abstract boolean currentDesiredState();

    VariableValue qualifiedVal;
    
    protected void setWatchedAvailable(boolean enable) {
        qualifiedVal.setAvailable(enable);
    }
    
    public void update(int value) {
        setWatchedAvailable(availableStateFromValue(value));
    }
}
