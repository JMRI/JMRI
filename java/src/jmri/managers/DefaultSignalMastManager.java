package jmri.managers;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.SignalHead;
import jmri.SignalHeadManager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.implementation.SignalHeadSignalMast;
import jmri.implementation.SignalMastRepeater;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalMastManager.
 * <p>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalMastManager extends AbstractManager<SignalMast>
        implements SignalMastManager {

    public DefaultSignalMastManager(InternalSystemConnectionMemo memo) {
        super(memo);
        InstanceManager.getDefault(SignalHeadManager.class).addVetoableChangeListener(this);
        InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SIGNALMASTS;
    }

    @Override
    public char typeLetter() {
        return 'F';
    }

    @Override
    public SignalMast getSignalMast(String name) {
        if (Objects.isNull(name) || name.length() == 0) {
            return null;
        }
        SignalMast t = getByUserName(name);
        if (t != null) {
            return t;
        }

        return getBySystemName(name);
    }

    @Override
    public SignalMast provideSignalMast(String prefix, // nominally IF$shsm
            String signalSystem,
            String mastName,
            String[] heads) {
        StringBuilder name = new StringBuilder(prefix);
        name.append(":");
        name.append(signalSystem);
        name.append(":");
        for (String s : heads) {
            name.append("(");
            name.append(StringUtil.parenQuote(s));
            name.append(")");
        }
        return provideSignalMast(new String(name));
    }

    @Override
    public SignalMast provideSignalMast(String name) {
        SignalMast m = getSignalMast(name);
        if (m == null) {
            m = new SignalHeadSignalMast(name);
            register(m);
        }
        return m;
    }

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

    @Override
    public SignalMast getBySystemName(String key) {
        return _tsys.get(key);
    }

    @Override
    public SignalMast getByUserName(String key) {
        return _tuser.get(key);
    }

    @Override
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

    ArrayList<SignalMastRepeater> repeaterList = new ArrayList<>();

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

    @Override
    public SignalMast provide(String name) throws IllegalArgumentException {
        return provideSignalMast(name);
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManager.class);
}
