package jmri.managers;

import javax.annotation.Nonnull;
import jmri.Manager;
import jmri.MeterGroup;
import jmri.MeterGroupManager;
import jmri.SystemConnectionMemo;
import jmri.implementation.DefaultMeterGroup;

/**
 * Default implementation of a MeterManager.
 *
 * @author Dave Duchamp      Copyright (C) 2004
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public class DefaultMeterGroupManager extends AbstractManager<MeterGroup>
        implements MeterGroupManager {

    /**
     * Create a new AnalogIOManager instance.
     * 
     * @param memo the system connection
     */
    public DefaultMeterGroupManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.METERGROUPS;
    }

    /** {@inheritDoc} */
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
        return Bundle.getMessage(plural ? "BeanNameMeterGroups" : "BeanNameMeterGroup");
    }

    /** {@inheritDoc} */
    @Override
    public Class<MeterGroup> getNamedBeanClass() {
        return MeterGroup.class;
    }

    /** {@inheritDoc} */
    @Override
    public MeterGroup provide(@Nonnull String name) throws IllegalArgumentException {
        return new DefaultMeterGroup(name);
    }

//    private final static Logger log = LoggerFactory.getLogger(AbstractAnalogIOManager.class);

}
