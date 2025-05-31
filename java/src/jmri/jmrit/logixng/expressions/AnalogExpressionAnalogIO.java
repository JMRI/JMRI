package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;

/**
 * Reads a AnalogIO.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogExpressionAnalogIO extends AbstractAnalogExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<AnalogIO> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, AnalogIO.class, InstanceManager.getDefault(AnalogIOManager.class), this);

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
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<AnalogIO> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate() throws JmriException {
        AnalogIO analogIO = _selectNamedBean.evaluateNamedBean(getConditionalNG());
        if (analogIO != null) {
            return jmri.util.TypeConversionUtil.convertToDouble(analogIO.getKnownAnalogValue(), false);
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
        return Bundle.getMessage(locale, "AnalogExpressionAnalogIO_Long", _selectNamedBean.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _selectNamedBean.addPropertyChangeListener("value", this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener("value", this);
            _selectNamedBean.unregisterListeners();
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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionAnalogIO.class);

}
