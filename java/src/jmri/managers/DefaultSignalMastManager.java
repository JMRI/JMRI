package jmri.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
import jmri.implementation.*;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.StringUtil;

/**
 * Default implementation of a SignalMastManager.
 * <p>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2020
 */
public class DefaultSignalMastManager extends AbstractManager<SignalMast>
        implements SignalMastManager {

    public DefaultSignalMastManager(InternalSystemConnectionMemo memo) {
        super(memo);
        repeaterList = new ArrayList<>();
        addListeners();
    }

    final void addListeners(){
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(TurnoutManager.class).addVetoableChangeListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public int getXMLOrder() {
        return Manager.SIGNALMASTS;
    }

    /** {@inheritDoc} */
    @Override
    public char typeLetter() {
        return 'F';
    }

    /**
     * {@inheritDoc}
     * Searches by UserName then SystemName.
     */
    @Override
    @CheckForNull
    public SignalMast getSignalMast(@Nonnull String name) {
        if (Objects.isNull(name) || name.length() == 0) {
            return null;
        }
        SignalMast t = getByUserName(name);
        return ( t != null ? t : getBySystemName(name));
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SignalMast provideSignalMast(@Nonnull String prefix, // nominally IF$shsm
                                        @Nonnull String signalSystem,
                                        @Nonnull String mastName,
                                        @Nonnull String[] heads) throws JmriException {
        StringBuilder name = new StringBuilder(prefix);
        name.append(":");
        name.append(signalSystem);
        name.append(":");
        name.append(mastName);
        for (String s : heads) {
            name.append("(");
            name.append(StringUtil.parenQuote(s));
            name.append(")");
        }
        return provideSignalMast(new String(name));
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public SignalMast provideSignalMast(@Nonnull String name) throws IllegalArgumentException {
        SignalMast m = getSignalMast(name);
        if (m == null) {
            // this should be replaced by a Service based approach,
            // perhaps along the lines of SignalMastAddPane, but
            // for now we manually check types
            if (name.startsWith("IF$shsm")) {
                m = new SignalHeadSignalMast(name);
            } else if (name.startsWith("IF$dsm")) {
                m = new DccSignalMast(name);
            } else if (name.startsWith("IF$vsm")) {
                m = new VirtualSignalMast(name);
            } else {
                // didn't recognize name, so trying to make it virtual
                log.warn("building stand-in VirtualSignalMast for {}", name);
                m = new VirtualSignalMast(name);
            }
            register(m);
        }
        return m;
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public SignalMast provideCustomSignalMast(@Nonnull String systemName, Class<? extends
            SignalMast> mastClass) throws JmriException {
        SignalMast m = getBySystemName(systemName);
        if (m != null) {
            if (!mastClass.isInstance(m)) {
                throw new JmriException("Could not create signal mast " + systemName + ", because" +
                        " the system name is already used by a different kind of mast. Expected "
                        + mastClass.getSimpleName() + ", actual " + m.getClass().getSimpleName()
                        + ".");
            }
            return m;
        }
        try {
            m = mastClass.getConstructor(String.class).newInstance(systemName);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException |
                InvocationTargetException e) {
            throw new JmriException(e);
        }
        register(m);
        return m;
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public SignalMast getBySystemName(@Nonnull String key) {
        return _tsys.get(key);
    }

    /** {@inheritDoc} */
    @Override
    @CheckForNull
    public SignalMast getByUserName(@Nonnull String key) {
        return _tuser.get(key);
    }

   @Override
   @Nonnull
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameSignalMasts" : "BeanNameSignalMast");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<SignalMast> getNamedBeanClass() {
        return SignalMast.class;
    }

    private final ArrayList<SignalMastRepeater> repeaterList;

    /**
     * Creates or retrieves a signal mast repeater.
     * @param master the mast for the master of the repeater.
     * @param slave the mast for the slave of the repeater.
     * @return newly created (and registered) or existing signal mast repeater.
     * @throws JmriException if the repeater already exists but the other direction.
     */
    public @Nonnull SignalMastRepeater provideRepeater(@Nonnull SignalMast master, @Nonnull SignalMast
            slave) throws JmriException {
        SignalMastRepeater rp = null;
        for (SignalMastRepeater currentRepeater : repeaterList) {
            if (currentRepeater.getMasterMast() == master && currentRepeater.getSlaveMast() == slave) {
                rp = currentRepeater;
            } else if (currentRepeater.getMasterMast() == slave
                    && currentRepeater.getSlaveMast() == master) {
                log.error("Signal repeater {}:{} already exists the wrong way", master, slave);
                throw new JmriException("Signal mast repeater already exists the wrong way");
            }
        }
        if (rp == null) {
            rp = new SignalMastRepeater(master, slave);
            repeaterList.add(rp);
        }
        firePropertyChange("repeaterlength", null, null);
        return rp;
    }

    public void addRepeater(SignalMastRepeater rp) throws JmriException {
        for (SignalMastRepeater rpeat : repeaterList) {
            if (rpeat.getMasterMast() == rp.getMasterMast()
                    && rpeat.getSlaveMast() == rp.getSlaveMast()) {
                log.error("Signal repeater already Exists");
                throw new JmriException("Signal mast Repeater already exists");
            } else if (rpeat.getMasterMast() == rp.getSlaveMast()
                    && rpeat.getSlaveMast() == rp.getMasterMast()) {
                log.error("Signal repeater already Exists");
                throw new JmriException("Signal mast Repeater already exists");
            }
        }
        repeaterList.add(rp);
        firePropertyChange("repeaterlength", null, null);
    }

    public void removeRepeater(SignalMastRepeater rp) {
        rp.dispose();
        repeaterList.remove(rp);
        firePropertyChange("repeaterlength", null, null);
    }

    public List<SignalMastRepeater> getRepeaterList() {
        return repeaterList;
    }

    public void initialiseRepeaters() {
        for (SignalMastRepeater smr : repeaterList) {
            smr.initialise();
        }
    }

    /** {@inheritDoc} */
    @Nonnull
    @Override
    public SignalMast provide(String name) throws IllegalArgumentException {
        return provideSignalMast(name);
    }

    /** {@inheritDoc} */
    @Override
    public void dispose(){
        InstanceManager.getDefault(SignalHeadManager.class).removeVetoableChangeListener(this);
        InstanceManager.getDefault(TurnoutManager.class).removeVetoableChangeListener(this);
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultSignalMastManager.class);
}
