// AbstractQualifier.java

package jmri.jmrit.symbolicprog;

/**
 * Watches a specific Variable to qualify others.
 *
 * @author			Bob Jacobsen   Copyright (C) 2010
 * @version			$Revision: 1.1 $
 *
 */
public abstract class AbstractQualifier implements Qualifier, java.beans.PropertyChangeListener {

    public AbstractQualifier(AbstractValue qualifiedVal, AbstractValue watchedVal) {
        this.qualifiedVal = qualifiedVal;
        this.watchedVal = watchedVal;

        // set up listener
        watchedVal.addPropertyChangeListener(this);
    }
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        System.out.println(" change "+e.getPropertyName()+" to "+e.getNewValue());
        
        if (e.getPropertyName().equals("Value")) {
            boolean oldAvailableValue = qualifiedVal.getAvailable();
            // watched value changed, check
            boolean newAvailableValue = availableStateFromInputValue(e);
            System.out.println(" old "+oldAvailableValue+" new "+newAvailableValue);
            
            if (oldAvailableValue != newAvailableValue)
                setWatchedAvailable(newAvailableValue);
        }
    }
    
    abstract public boolean availableStateFromInputValue(java.beans.PropertyChangeEvent e);

    AbstractValue qualifiedVal;
    AbstractValue watchedVal;
    
    protected void setWatchedAvailable(boolean enable) {
        System.out.println("setAvailable "+enable);
        qualifiedVal.setAvailable(enable);
    }
    
}
