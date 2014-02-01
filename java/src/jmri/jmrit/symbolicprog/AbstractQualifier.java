// AbstractQualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Watches a specific Variable to qualify another object, e.g.
 * another Variable or a Pane.
 *
 * @author			Bob Jacobsen   Copyright (C) 2010, 2014
 * @version			$Revision$
 *
 */
public abstract class AbstractQualifier implements Qualifier, java.beans.PropertyChangeListener {

    public AbstractQualifier(VariableValue watchedVal) {
        this.watchedVal = watchedVal;

        // set up listener
        watchedVal.addPropertyChangeListener(this);
        
        // subclass ctors are required to qualify on initial value of variable   
        // to get initial qualification state right after listener was added.   
    }
    
    VariableValue watchedVal;
    
    /**
     * Process property change from the variable being watched
     */
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("Value"))
            processValueChangeEvent(e);
    }
    
    /**
     * Process Value property change from the variable being watched
     */
    void processValueChangeEvent(java.beans.PropertyChangeEvent e) {
        // watched value change, check if this changes state of qualified (output) object
        boolean oldAvailableValue = currentAvailableState();
        boolean newAvailableValue = availableStateFromEvent(e);
        
        if (oldAvailableValue != newAvailableValue)
            setWatchedAvailable(newAvailableValue);
    }
    
    /**
     * Calculate whether this PropertyChangeEvent
     * means that the qualified object should be set Available or not.
     */
    protected boolean availableStateFromEvent(java.beans.PropertyChangeEvent e) {
        return availableStateFromObject(e.getNewValue());
    }
    
    /**
     * Calculate whether the current value of watched Variable
     * means that the qualified object should be set Available or not.
     */
    abstract protected boolean availableStateFromObject(Object o);
    
    abstract protected boolean availableStateFromValue(int value);
    abstract protected boolean currentAvailableState();
    abstract public boolean currentDesiredState();
    
    /**
     * Drive the available or not state of the qualified object.
     *<p>Subclasses implement this to control a specific type
     * of qualified object, like a Variable or Pane.
     */
    abstract public void setWatchedAvailable(boolean enable);
    
}
