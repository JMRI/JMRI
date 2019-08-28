package jmri.jmrit.logixng.digital.expressions;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.DigitalExpressionManager;

/**
 * Always evaluates to True.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class True extends AbstractDigitalExpression {

    private True _template;
    
    /**
     * Create a new instance of ActionIfThen and generate a new system name.
     */
    public True() {
        super(InstanceManager.getDefault(DigitalExpressionManager.class).getNewSystemName());
    }
    
    public True(String sys) throws BadSystemNameException {
        super(sys);
    }

    public True(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }
    
    public True(String sys, List<String> childrenSystemNames) throws BadSystemNameException {
        super(sys);
    }

    public True(String sys, String user, List<String> childrenSystemNames)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    private True(True template, String sys) {
        super(sys);
        _template = template;
        if (_template == null) throw new NullPointerException();    // Temporary solution to make variable used.
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate(String sys) {
        return new True(this, sys);
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
        return true;
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
    public String getShortDescription() {
        return Bundle.getMessage("True_Short");
    }
    
    @Override
    public String getLongDescription() {
        return Bundle.getMessage("True_Long");
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
