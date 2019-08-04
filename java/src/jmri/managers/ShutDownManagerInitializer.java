package jmri.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceInitializer;
import jmri.ShutDownManager;
import jmri.implementation.AbstractInstanceInitializer;

/**
 * An initializer for the {@link jmri.ShutDownManager} that allows the
 * ShutDownManager to be used to be specified as a Java property.
 * <p>
 * This InstanceInitializer provides a
 * {@link jmri.managers.DefaultShutDownManager} unless the name of the class to
 * use as the ShutDownManager is specified in the {@code jmri.shutdownmanager}
 * Java System Property. If the property is specified, it must be a complete
 * name of a class that implements jmri.ShutDownManager and has a public default
 * constructor.
 */
@ServiceProvider(service = InstanceInitializer.class)
public class ShutDownManagerInitializer extends AbstractInstanceInitializer {

    private static final Logger log = LoggerFactory.getLogger(ShutDownManagerInitializer.class);

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Object getDefault(Class<T> type) throws IllegalArgumentException {
        if (type.equals(ShutDownManager.class)) {
            String property = System.getProperty("jmri.shutdownmanager");
            if (property != null) {
                try {
                    Class<?> c = Class.forName(property);
                    if (ShutDownManager.class.isAssignableFrom(c)) {
                        return c.getConstructor().newInstance();
                    }
                    log.error("Specified jmri.shutdownmanager value {} is not a jmri.ShutDownManager subclass", property);
                } catch (
                        ClassNotFoundException |
                        InstantiationException |
                        IllegalAccessException |
                        InvocationTargetException |
                        NoSuchMethodException |
                        SecurityException e) {
                    log.error("Unable to instanciate ShutDownManager class {} with default constructor", property);
                }
            }
            return new DefaultShutDownManager();
        }
        return super.getDefault(type);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<Class<?>> getInitalizes() {
        Set<Class<?>> set = super.getInitalizes();
        set.add(ShutDownManager.class);
        return set;
    }

}