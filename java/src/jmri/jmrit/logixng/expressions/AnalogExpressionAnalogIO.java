package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * Reads a AnalogIO.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogExpressionAnalogIO extends AbstractAnalogExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanHandle<AnalogIO> _analogIOHandle;

    public AnalogExpressionAnalogIO(String sys, String user)
            throws BadUserNameException, BadSystemNameException {

        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        AnalogExpressionManager manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AnalogExpressionAnalogIO copy = new AnalogExpressionAnalogIO(sysName, userName);
        copy.setComment(getComment());
        if (_analogIOHandle != null) copy.setAnalogIO(_analogIOHandle);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    /** {@inheritDoc} */
    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof AnalogIO) {
                if (evt.getOldValue().equals(getAnalogIO().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("AnalogIO_AnalogIOInUseAnalogIOExpressionVeto", getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof AnalogIO) {
                if (evt.getOldValue().equals(getAnalogIO().getBean())) {
                    removeAnalogIO();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    public void setAnalogIO(@Nonnull String analogIOName) {
        assertListenersAreNotRegistered(log, "setAnalogIO");
        AnalogIO analogIO = InstanceManager.getDefault(AnalogIOManager.class).getNamedBean(analogIOName);
        if (analogIO != null) {
            setAnalogIO(analogIO);
        } else {
            removeAnalogIO();
            log.error("analogIO \"{}\" is not found", analogIOName);
        }
    }

    public void setAnalogIO(@Nonnull NamedBeanHandle<AnalogIO> handle) {
        assertListenersAreNotRegistered(log, "setAnalogIO");
        _analogIOHandle = handle;
        InstanceManager.getDefault(AnalogIOManager.class).addVetoableChangeListener(this);
    }

    public void setAnalogIO(@Nonnull AnalogIO analogIO) {
        assertListenersAreNotRegistered(log, "setAnalogIO");
        setAnalogIO(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(analogIO.getDisplayName(), analogIO));
    }

    public void removeAnalogIO() {
        assertListenersAreNotRegistered(log, "setAnalogIO");
        if (_analogIOHandle != null) {
            InstanceManager.getDefault(AnalogIOManager.class).removeVetoableChangeListener(this);
            _analogIOHandle = null;
        }
    }

    public NamedBeanHandle<AnalogIO> getAnalogIO() {
        return _analogIOHandle;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        if (_analogIOHandle != null) {
            return jmri.util.TypeConversionUtil.convertToDouble(_analogIOHandle.getBean().getKnownAnalogValue(), false);
        } else {
            return 0.0;
        }
    }

    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "AnalogExpressionAnalogIO_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        if (_analogIOHandle != null) {
            return Bundle.getMessage(locale, "AnalogExpressionAnalogIO_Long", _analogIOHandle.getBean().getDisplayName());
        } else {
            return Bundle.getMessage(locale, "AnalogExpressionAnalogIO_Long", "none");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if ((! _listenersAreRegistered) && (_analogIOHandle != null)) {
            _analogIOHandle.getBean().addPropertyChangeListener("value", this);
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _analogIOHandle.getBean().removePropertyChangeListener("value", this);
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: AnalogExpressionAnalogIO: bean = {}, report = {}", cdl, report);
        if (getAnalogIO() != null && bean.equals(getAnalogIO().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionAnalogIO.class);

}
