package jmri.util.swing;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.SwingUtilities;

/**
 * Helper class for wrapping a PropertyChangeListener. This wrapper ensures that the events are
 * dispatched to the target listener only on the swing thread, and collapses multiple events into
 * a single call.
 * <p>
 * The property change events get de-duplicated and only the last one pending is
 * sent to the listener object; this means that the getOldValue() in the change event is not
 * guaranteed to be the last call's getNewValue(). Listeners that depend on exact sequencing of
 * oldValue and newValue objects should probably not be wrapped in this class.
 *
 * @author Balazs Racz Copyright (C) 2017
 */

public class DedupingPropertyChangeListener implements PropertyChangeListener {
    public DedupingPropertyChangeListener(PropertyChangeListener listener) {
        this.listener = listener;
    }

    @Override
    public synchronized void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        String key = propertyChangeEvent.getPropertyName();
        boolean hasPendingNotify = eventMap.containsKey(key);
        eventMap.put(key, propertyChangeEvent);
        if (hasPendingNotify) {
            return;
        }
        SwingUtilities.invokeLater(() -> invokePropertyChange(key));
    }

    private void invokePropertyChange(String key) {
        PropertyChangeEvent ev = null;
        synchronized (this) {
            ev = eventMap.get(key);
            eventMap.remove(key);
        }
        listener.propertyChange(ev);
    }

    final PropertyChangeListener listener;
    final HashMap<String, PropertyChangeEvent> eventMap = new HashMap<>();
}
