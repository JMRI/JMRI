package jmri.managers;

import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.Meter;
import jmri.MeterManager;
import jmri.SystemConnectionMemo;

/**
 * Default implementation of a MeterManager.
 *
 * @author Dave Duchamp      Copyright (C) 2004
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class AbstractMeterManager extends AbstractManager<Meter>
        implements MeterManager {

    /**
     * Create a new MeterManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractMeterManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.METERS;
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
        return Bundle.getMessage(plural ? "BeanNameMeters" : "BeanNameMeter");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Meter> getNamedBeanClass() {
        return Meter.class;
    }

//    private final static Logger log = LoggerFactory.getLogger(AbstractMeterManager.class);

}
