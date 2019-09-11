package jmri.jmrit.logixng.digital.expressions;

import java.util.List;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.DigitalExpressionManager;

/**
 * Always evaluates to False.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class False extends AbstractDigitalExpression {

    private False _template;
    
    public False(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public False(String sys, String user, List<String> childrenSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    private False(False template) {
        super(InstanceManager.getDefault(DigitalExpressionManager.class).getNewSystemName(), null);
        _template = template;
        if (_template == null) throw new NullPointerException();    // Temporary solution to make variable used.
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new False(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.COMMON;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean evaluate() {
        return false;
    }
    
    /** {@inheritDoc} */
    @Override
    public void reset() {
        // Do nothing
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
        return Bundle.getMessage(locale, "False_Short");
    }
    
    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "False_Long");
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

}
