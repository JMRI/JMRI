package jmri.util.managers;

import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.jmrix.internal.InternalSensorManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 * 
 * The class name ends with 'Scaffold' to exclude it from the coverage statistics,
 * since it is part of the testing infrastructure.
 */
public class SensorManagerThrowExceptionScaffold extends InternalSensorManager {

    public SensorManagerThrowExceptionScaffold() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }
    
    /** {@inheritDoc} */
    @Override
    protected Sensor createNewSensor(String systemName, String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull
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
    public Sensor getByUserName(@Nonnull String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    @Nonnull
    public Sensor newSensor(@Nonnull String systemName, @CheckForNull String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
