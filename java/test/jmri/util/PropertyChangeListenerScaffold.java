package jmri.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * Test utility class that allows verification for beans PropertyChange events.
 * <p>
 * @author Balazs Racz Copyright (C) 2017
 * @author Paul Bender Copyright (C) 2018
 */

public class PropertyChangeListenerScaffold implements PropertyChangeListener {

    private boolean propertyChanged;
    private int callCount;

    public PropertyChangeListenerScaffold() {
       propertyChanged = false;
       callCount = 0;
    }

    public void resetPropertyChanged(){
       propertyChanged = false;
       callCount = 0;
    }

    public boolean getPropertyChanged(){
       return propertyChanged;
    }

    public int getCallCount(){
       return callCount;
    }

    public void onChange(String property, Object newValue){
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
       propertyChanged = true;
       onChange(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
       callCount++;
    }
}
