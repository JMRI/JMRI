package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.VetoableChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;

/**
 * An action that lets a SignalMast that follow the state of another SignalMast.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionSignalMastFollow extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<SignalMast> _selectPrimaryMast =
            new LogixNG_SelectNamedBean<>(
                    this, SignalMast.class, InstanceManager.getDefault(SignalMastManager.class), this);

    private final LogixNG_SelectNamedBean<SignalMast> _selectSecondaryMast =
            new LogixNG_SelectNamedBean<>(
                    this, SignalMast.class, InstanceManager.getDefault(SignalMastManager.class), this);

    private final Map<String, String> _aspectMap = new HashMap<>();


    public ActionSignalMastFollow(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _selectPrimaryMast.setOnlyDirectAddressingAllowed();
        _selectSecondaryMast.setOnlyDirectAddressingAllowed();
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSignalMastFollow copy = new ActionSignalMastFollow(sysName, userName);
        copy.setComment(getComment());
        _selectPrimaryMast.copy(copy._selectPrimaryMast);
        _selectSecondaryMast.copy(copy._selectSecondaryMast);
        for (Map.Entry<String, String> entry : _aspectMap.entrySet()) {
            copy._aspectMap.put(entry.getKey(), entry.getValue());
        }
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<SignalMast> getSelectPrimaryMast() {
        return _selectPrimaryMast;
    }

    public LogixNG_SelectNamedBean<SignalMast> getSelectSecondaryMast() {
        return _selectSecondaryMast;
    }

    public Map<String, String> getAspectMap() {
        return _aspectMap;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        final ConditionalNG conditionalNG = getConditionalNG();

        SignalMast primaryMast = _selectPrimaryMast.evaluateNamedBean(conditionalNG);
        SignalMast secondaryMast = _selectSecondaryMast.evaluateNamedBean(conditionalNG);

        if (primaryMast == null) return;
        if (secondaryMast == null) return;

        String sourceAspect = primaryMast.getAspect();

        // This might happen when JMRI starts
        if (sourceAspect == null) return;

        String destAspect = _aspectMap.get(sourceAspect);
        if (destAspect == null || destAspect.isEmpty()) {
            throw new JmriException(String.format(
                    "Aspect \"%s\" of primary mast %s has no mapping for the secondary mast %s",
                    primaryMast.getAspect(), primaryMast.getDisplayName(), secondaryMast.getDisplayName()));
        }

        AtomicReference<IllegalArgumentException> ref = new AtomicReference<>();
        jmri.util.ThreadingUtil.runOnLayoutWithJmriException(() -> {
            try {
                secondaryMast.setAspect(destAspect);
            } catch (IllegalArgumentException e) {
                ref.set(e);
            }
        });
        if (ref.get() != null) throw ref.get();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "SignalMastFollow_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String primaryMast = _selectPrimaryMast.getDescription(locale);
        String secondaryMast = _selectSecondaryMast.getDescription(locale);

        return Bundle.getMessage(locale, "SignalMastFollow_Long", primaryMast, secondaryMast);
    }

    @Override
    public String getDetailedLongDescription(Locale locale) {
        String primaryMast = _selectPrimaryMast.getDescription(locale);
        String secondaryMast = _selectSecondaryMast.getDescription(locale);

        StringBuilder map = new StringBuilder();
        for (Map.Entry<String, String> entry : _aspectMap.entrySet()) {
            if (map.length() > 0) map.append(", ");
            map.append("[").append(entry.getKey()).append(",").append(entry.getValue()).append("]");
        }

        return Bundle.getMessage(locale, "SignalMastFollow_LongDetailed", primaryMast, secondaryMast, map.toString());
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        SignalMast signalMast = _selectPrimaryMast.getNamedBeanIfDirectAddressing();

        if (!_listenersAreRegistered && (signalMast != null)) {
            signalMast.addPropertyChangeListener("Aspect", this);
            _selectPrimaryMast.registerListeners();
            _selectSecondaryMast.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        SignalMast signalMast = _selectPrimaryMast.getNamedBeanIfDirectAddressing();

        if (_listenersAreRegistered && (signalMast != null)) {
            signalMast.removePropertyChangeListener("Aspect", this);
            _selectPrimaryMast.unregisterListeners();
            _selectSecondaryMast.unregisterListeners();
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ActionSignalMastFollow: bean = {}, report = {}", cdl, report);
        _selectPrimaryMast.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectSecondaryMast.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMastFollow.class);

}
