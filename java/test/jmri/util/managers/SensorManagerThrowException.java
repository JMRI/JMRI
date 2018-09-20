package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jmri.Sensor;
import jmri.jmrix.internal.InternalSensorManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 */
public class SensorManagerThrowException extends InternalSensorManager {

    public SensorManagerThrowException() {
        super("I");
    }
    
    /** {@inheritDoc} */
    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor provideSensor(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor getSensor(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor getBySystemName(@Nonnull String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Sensor newSensor(@Nonnull String systemName, @Nullable String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
