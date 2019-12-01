package jmri.managers;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Abstract partial implementation of a SignalHeadManager.
 * <p>
 * Not truly an abstract class, this might have been better named
 * DefaultSignalHeadManager. But we've got it here for the eventual need to
 * provide system-specific implementations.
 * <p>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class AbstractSignalHeadManager extends AbstractManager<SignalHead>
        implements SignalHeadManager {

    public AbstractSignalHeadManager(InternalSystemConnectionMemo memo) {
        super(memo);
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.SIGNALHEADS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'H';
    }

    /** {@inheritDoc} */
    @Override
    public SignalHead getSignalHead(String name) {
        if (name == null || name.length() == 0) {
            return null;
        }
        SignalHead t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    /** {@inheritDoc} */
    @Override
    public SignalHead getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public SignalHead getByUserName(String key) {
        return _tuser.get(key);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSignalHeads" : "BeanNameSignalHead");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SignalHead> getNamedBeanClass() {
        return SignalHead.class;
    }
}
