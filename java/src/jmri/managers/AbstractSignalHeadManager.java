package jmri.managers;

import jmri.Manager;
import jmri.SignalHead;
import jmri.SignalHeadManager;

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

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSignalHead");
    }
}
