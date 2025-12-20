package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectString;

/**
 * Reads a local variable.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class AnalogExpressionLocalVariable extends AbstractAnalogExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectString _selectNamedBean =
            new LogixNG_SelectString(this, this);

    public AnalogExpressionLocalVariable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {

        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        AnalogExpressionManager manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AnalogExpressionLocalVariable copy = new AnalogExpressionLocalVariable(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerExpression(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectString getSelectNamedBean() {
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
        String localVariable = _selectNamedBean.evaluateValue(getConditionalNG());
        if (localVariable == null) return 0.0;

        Object value = getConditionalNG().getSymbolTable().getValue(localVariable);
        if (value != null) {
            return jmri.util.TypeConversionUtil.convertToDouble(value, false);
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
        return Bundle.getMessage(locale, "AnalogExpressionLocalVariable_Short");
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "AnalogExpressionLocalVariable_Long", _selectNamedBean.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        // Do nothing
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
        log.debug("getUsageReport :: AnalogExpressionLocalVariable: bean = {}, report = {}", cdl, report);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionLocalVariable.class);

}
