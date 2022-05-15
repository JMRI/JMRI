package jmri.jmrit.logixng.expressions;

import java.text.NumberFormat;
import java.util.Locale;

import java.util.Map;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;

/**
 * Constant value.
 * This can be useful for example by the ActionThrottle.
 * 
 * @author Daniel Bergqvist Copyright 2019
 */
public class AnalogExpressionConstant extends AbstractAnalogExpression {

    private double _value;
    
    public AnalogExpressionConstant(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        
        super(sys, user);
    }
    
    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        AnalogExpressionManager manager = InstanceManager.getDefault(AnalogExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        AnalogExpressionConstant copy = new AnalogExpressionConstant(sysName, userName);
        copy.setComment(getComment());
        copy.setValue(_value);
        return manager.registerExpression(copy);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }
    
    public void setValue(double value) {
        assertListenersAreNotRegistered(log, "setValue");
        _value = value;
    }
    
    public double getValue() {
        return _value;
    }
    
    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        return _value;
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
        return Bundle.getMessage(locale, "AnalogExpressionConstant_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        NumberFormat numberFormat = NumberFormat.getInstance(locale);
        return Bundle.getMessage(locale, "AnalogExpressionConstant_Long", numberFormat.format(_value));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        // This class does not have any listeners registered, but we still don't
        // want a caller to change the value then listeners are registered.
        // So we set this property to warn the caller when the caller is using
        // the class in the wrong way.
        _listenersAreRegistered = true;
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _listenersAreRegistered = false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionConstant.class);
    
}
