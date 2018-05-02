package jmri.managers;

import java.util.ArrayList;
import java.util.List;
import jmri.Manager;
import jmri.SignalMast;
import jmri.SignalMastManager;
import jmri.implementation.SignalMastRepeater;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default implementation of a SignalMastManager.
 * <P>
 * Note that this does not enforce any particular system naming convention at
 * the present time. They're just names...
 *
 * @author Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalMastManager extends AbstractManager<SignalMast>
        implements SignalMastManager, java.beans.PropertyChangeListener {

    public DefaultSignalMastManager() {
        super();
        jmri.InstanceManager.getDefault(jmri.SignalHeadManager.class).addVetoableChangeListener(this);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.SIGNALMASTS;
    }

    @Override
    public String getSystemPrefix() {
        return "I";
    }

    @Override
    public char typeLetter() {
        return 'F';
    }

    @Override
    public SignalMast getSignalMast(String name) {
        if (name == null || name.length() == 0) {
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
            name.append(jmri.util.StringUtil.parenQuote(s));
            name.append(")");
        }
        return provideSignalMast(new String(name));
    }

    @Override
    public SignalMast provideSignalMast(String name) {
        SignalMast m = getSignalMast(name);
        if (m == null) {
            m = new jmri.implementation.SignalHeadSignalMast(name);
            register(m);
        }
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
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameSignalMast");
    }

    ArrayList<SignalMastRepeater> repeaterList = new ArrayList<SignalMastRepeater>();

    public void addRepeater(SignalMastRepeater rp) throws jmri.JmriException {
        for (SignalMastRepeater rpeat : repeaterList) {
            if (rpeat.getMasterMast() == rp.getMasterMast()
                    && rpeat.getSlaveMast() == rp.getSlaveMast()) {
                log.error("Signal repeater already Exists");
                throw new jmri.JmriException("Signal mast Repeater already exists");
            } else if (rpeat.getMasterMast() == rp.getSlaveMast()
                    && rpeat.getSlaveMast() == rp.getMasterMast()) {
                log.error("Signal repeater already Exists");
                throw new jmri.JmriException("Signal mast Repeater already exists");
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

    private final static Logger log = LoggerFactory.getLogger(DefaultSignalMastManager.class);
}
