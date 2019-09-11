package jmri.jmrit.logixng.digital.implementation;

import java.util.Locale;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.digital.expressions.AbstractDigitalExpression;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.DigitalExpressionBean;

/**
 * Adapter for expression plugins.
 * Every expression needs to have a configurator class that delivers a JPanel
 * used for configuration. Since plugin expressions has 
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DigitalExpressionPluginAdapter extends AbstractDigitalExpression {

    private final DigitalExpressionBean _pluginExpression;
    
    public DigitalExpressionPluginAdapter(String sys, DigitalExpressionBean pluginExpression)
            throws BadSystemNameException {
        
        super(sys);
        
        _pluginExpression = pluginExpression;
    }

    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return _pluginExpression.getNewObjectBasedOnTemplate();
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return _pluginExpression.getCategory();
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        return _pluginExpression.evaluate();
    }
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        _pluginExpression.reset();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getShortDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getLongDescription(Locale locale) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
        _pluginExpression.dispose();
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        _pluginExpression.setup();
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _pluginExpression.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _pluginExpression.unregisterListeners();
    }

}
