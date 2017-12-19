package jmri.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import static org.mockito.Mockito.mock;

/**
 * Test utility class that allows running Mockito verifications for beans PropertyChange events.
 * <p>
 * @author Balazs Racz Copyright (C) 2017
 */

public class MockPropertyChangeListener implements PropertyChangeListener {
    public interface MockInterface {
        void onChange(String property, Object newValue);
    }

    public MockInterface m;

    public MockPropertyChangeListener() {
        m = mock(MockInterface.class);
    }

    @Override
    public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
        m.onChange(propertyChangeEvent.getPropertyName(), propertyChangeEvent.getNewValue());
    }
}
