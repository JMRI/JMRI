// QualifierCombiner.java

package jmri.jmrit.symbolicprog;

import java.util.List;

/**
 * Force a set of Qualifiers to work in an AND relationship
 *
 * @author			Bob Jacobsen   Copyright (C) 2011
 * @version			$Revision: -1$
 *
 */
public class QualifierCombiner implements Qualifier, java.beans.PropertyChangeListener {
    
    public QualifierCombiner(VariableValue qualifiedVal, List<Qualifier> qualifiers) {
        this.qualifiers = qualifiers;
        this.qualifiedVal = qualifiedVal;
        
        qualifiedVal.addPropertyChangeListener(this);
        
        setWatchedAvailable(currentDesiredState());

    }

    VariableValue qualifiedVal;
    List<Qualifier> qualifiers;
    
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (e.getPropertyName().equals("Available"))
            // this was a change, may want to change it back
            setWatchedAvailable(currentDesiredState());  // relies on non-propogation of null changes
    }

    protected void setWatchedAvailable(boolean enable) {
        qualifiedVal.setAvailable(enable);
    }

    public boolean currentDesiredState() {
        for (Qualifier q: qualifiers) {
            if (! q.currentDesiredState()) return false;
        }
        return true;
    }
       
    public void update() {
        setWatchedAvailable(currentDesiredState());
    } 
}
