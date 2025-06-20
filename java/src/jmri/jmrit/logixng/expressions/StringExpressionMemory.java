package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;

/**
 * Reads a Memory.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class StringExpressionMemory extends AbstractStringExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Memory> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    public StringExpressionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {

        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        StringExpressionManager manager = InstanceManager.getDefault(StringExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        StringExpressionMemory copy = new StringExpressionMemory(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<Memory> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public String evaluate() throws JmriException {
        Memory memory = _selectNamedBean.evaluateNamedBean(getConditionalNG());
        if (memory != null) {
            return jmri.util.TypeConversionUtil.convertToString(memory.getValue(), false);
        } else {
            return "";
        }
    }

    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringExpressionMemory_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "StringExpressionMemory_Long", _selectNamedBean.getDescription(locale));
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
        log.debug("getUsageReport :: StringExpressionMemory: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringExpressionMemory.class);

}
