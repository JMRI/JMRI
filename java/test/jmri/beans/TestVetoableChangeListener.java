package jmri.beans;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.ArrayList;
import java.util.List;

public class TestVetoableChangeListener implements VetoableChangeListener {

    private String throwNext = null;
    private final List<PropertyChangeEvent> events = new ArrayList<>();
    
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        events.add(evt);
        if (throwNext != null) {
            String mess = throwNext;
            throwNext = null;
            throw new PropertyVetoException(mess, evt);
        }
    }

    public PropertyChangeEvent getLastEvent() {
        return events.isEmpty() ? null : events.get(events.size() - 1);
    }

    public List<PropertyChangeEvent> getEvents() {
        return new ArrayList<>(events);
    }

    public void clear() {
        events.clear();
    }

    /**
     * Throw on the next event.
     * 
     * @param mess message to throw; will not throw if null
     */
    public void throwNext(String mess) {
        throwNext = mess;
    }
    
    public boolean willThrowNext() {
        return throwNext != null;
    }
}
