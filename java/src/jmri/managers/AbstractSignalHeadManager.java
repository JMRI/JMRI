package jmri.managers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

import java.util.Objects;

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
        init();
    }
    
    final void init(){
        turnoutManager = InstanceManager.getDefault(TurnoutManager.class);
        turnoutManager.addVetoableChangeListener(this);
    }

    /**
     * The TurnoutManager this manager listens to for deletions, so that
     * {@link #dispose()} stops listening to that one, not whatever the
     * InstanceManager holds by then.
     */
    private TurnoutManager turnoutManager;

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
    @CheckForNull
    public SignalHead getSignalHead(@Nonnull String name) {
        Objects.requireNonNull(name, "SignalHead name cannot be null.");  // NOI18N
        if (name.trim().length() == 0) {
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
    @Nonnull
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
    
    @Override
    public void dispose(){
        turnoutManager.removeVetoableChangeListener(this);
        super.dispose();
    }

}
