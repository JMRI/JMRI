package jmri.managers;

import javax.annotation.Nonnull;
import jmri.StringIO;
import jmri.Manager;
import jmri.SystemConnectionMemo;
import jmri.StringIOManager;

/**
 * Abstract partial implementation of a StringIOManager.
 * <p>
 * Based on AbstractSignalHeadManager.java and AbstractSensorManager.java
 *
 * @author Dave Duchamp      Copyright (C) 2004
 * @author Daniel Bergqvist  Copyright (C) 2020
 */
public abstract class AbstractStringIOManager extends AbstractManager<StringIO>
        implements StringIOManager {

    /**
     * Create a new StringIOManager instance.
     * 
     * @param memo the system connection
     */
    public AbstractStringIOManager(SystemConnectionMemo memo) {
        super(memo);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.STRINGIOS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public char typeLetter() {
        return 'C';
    }

    /**
     * Get bean type handled.
     *
     * @return a string for the type of object handled by this manager
     */
    @Override
    @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameStringIOs" : "BeanNameStringIO");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<StringIO> getNamedBeanClass() {
        return StringIO.class;
    }

//    private final static Logger log = LoggerFactory.getLogger(AbstractStringIOManager.class);

}
