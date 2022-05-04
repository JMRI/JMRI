package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Returns the last result of a digital expression.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class LastResultOfDigitalExpression extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<MaleDigitalExpressionSocket> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, MaleDigitalExpressionSocket.class, InstanceManager.getDefault(DigitalExpressionManager.class), this);

    public LastResultOfDigitalExpression(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _selectNamedBean.setOnlyDirectAddressingAllowed();
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        LastResultOfDigitalExpression copy = new LastResultOfDigitalExpression(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<MaleDigitalExpressionSocket> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        MaleDigitalExpressionSocket expression =
                _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (expression != null) return expression.getLastResult();

        return false;
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
        return Bundle.getMessage(locale, "LastResultOfDigitalExpression_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String beanName = _selectNamedBean.getDescription(locale);

        return Bundle.getMessage(locale, "LastResultOfDigitalExpression_Long",
                beanName);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        _selectNamedBean.setup();
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _selectNamedBean.addPropertyChangeListener(PROPERTY_LAST_RESULT_CHANGED, this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener(PROPERTY_LAST_RESULT_CHANGED, this);
            _selectNamedBean.unregisterListeners();
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LastResultOfDigitalExpression.class);

}
