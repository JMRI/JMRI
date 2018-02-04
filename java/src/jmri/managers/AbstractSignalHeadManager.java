package jmri.managers;

import jmri.Manager;
import jmri.NamedBean;
import jmri.SignalHead;
import jmri.SignalHeadManager;

import javax.annotation.CheckForNull;
import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;

/**
 * Abstract partial implementation of a SignalHeadManager.
 * <P>
 * Not truly an abstract class, this might have been better named
 * DefaultSignalHeadManager. But we've got it here for the eventual need to
 * provide system-specific implementations.
 * <P>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class AbstractSignalHeadManager extends AbstractManager<SignalHead>
        implements SignalHeadManager, java.beans.PropertyChangeListener {

    public AbstractSignalHeadManager() {
        super();
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SIGNALHEADS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'H';
    }

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

    @Override
    public SignalHead getBySystemName(String name) {
        return _tsys.get(name);
    }

    @Override
    public SignalHead getByUserName(String key) {
        return _tuser.get(key);
    }

    /**
     * {@inheritDoc}
     * 
     * Forces upper case and trims leading and trailing whitespace.
     * Does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException.
     */
    @CheckReturnValue
    @Override
    public @Nonnull
    String normalizeSystemName(@Nonnull String inputName) throws NamedBean.BadSystemNameException {
        // does not check for valid prefix, hence doesn't throw NamedBean.BadSystemNameException
        return inputName.toUpperCase().trim();
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSignalHead");
    }
}
