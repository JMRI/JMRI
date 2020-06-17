package jmri.managers;

import javax.annotation.Nonnull;
import jmri.AnalogIO;
import jmri.Manager;
import jmri.SystemConnectionMemo;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
import jmri.AnalogIOManager;

/**
 * Abstract partial implementation of a AnalogIOManager.
 * <p>
 * Based on AbstractSignalHeadManager.java and AbstractSensorManager.java
 *
 * @author Dave Duchamp      Copyright (C) 2004
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public abstract class AbstractAnalogIOManager extends AbstractManager<AnalogIO>
        implements AnalogIOManager {

    /**
     * Create a new AnalogIOManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractAnalogIOManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.ANALOGIOS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char typeLetter() {
        return 'V';
    }

    /**
     * Get bean type handled.
     *
     * @return a string for the type of object handled by this manager
     */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameAnalogIOs" : "BeanNameAnalogIO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<AnalogIO> getNamedBeanClass() {
        return AnalogIO.class;
    }

//    private final static Logger log = LoggerFactory.getLogger(AbstractAnalogIOManager.class);

}
