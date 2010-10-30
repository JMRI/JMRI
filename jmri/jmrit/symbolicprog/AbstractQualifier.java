// AbstractQualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Watches a specific Variable to qualify others.
 *
 * @author			Bob Jacobsen   Copyright (C) 2010
 * @version			$Revision: 1.3 $
 *
 */
public abstract class AbstractQualifier implements Qualifier, java.beans.PropertyChangeListener {

    public AbstractQualifier(AbstractValue qualifiedVal, AbstractValue watchedVal) {
        this.qualifiedVal = qualifiedVal;

        // set up listener
        watchedVal.addPropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("Value")) {
            boolean oldAvailableValue = qualifiedVal.getAvailable();
            // watched value changed, check
            boolean newAvailableValue = availableStateFromEvent(e);
            
            if (oldAvailableValue != newAvailableValue)
                setWatchedAvailable(newAvailableValue);
        }
    }
    
    abstract protected boolean availableStateFromEvent(java.beans.PropertyChangeEvent e);
    abstract protected boolean availableStateFromValue(int value);

    AbstractValue qualifiedVal;
    
    protected void setWatchedAvailable(boolean enable) {
        qualifiedVal.setAvailable(enable);
    }
    
    public void update(int value) {
        setWatchedAvailable(availableStateFromValue(value));
    }
}
