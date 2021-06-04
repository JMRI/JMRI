package jmri.jmrit.logixng.expressions;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager.NameValidity;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.DigitalExpressionManager;

/**
 *
 */
public abstract class AbstractDigitalExpression extends AbstractBase
        implements DigitalExpressionBean {

    private Base _parent = null;
    private int _state = DigitalExpressionBean.UNKNOWN;
    
    
    public AbstractDigitalExpression(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        NameValidity isNameValid = InstanceManager.getDefault(DigitalExpressionManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyChangedResult(boolean oldResult, boolean newResult) {
        firePropertyChange(Base.PROPERTY_LAST_RESULT_CHANGED, oldResult, newResult);
    }

    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return _parent;
    }

    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameDigitalExpression");
    }

    /** {@inheritDoc} */
    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in AbstractDigitalExpression.");  // NOI18N
        _state = s;
    }

    /** {@inheritDoc} */
    @Override
    public int getState() {
        log.warn("Unexpected call to getState in AbstractDigitalExpression.");  // NOI18N
        return _state;
    }
    
    /** {@inheritDoc} */
    @Override
    public Lock getLock() {
        return _lock;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setLock(Lock lock) {
        _lock = lock;
    }
    
    public String getNewSocketName() {
        String[] names = new String[getChildCount()];
        for (int i=0; i < getChildCount(); i++) {
            names[i] = getChild(i).getName();
        }
        return getNewSocketName(names);
    }
    
    public static String getNewSocketName(String[] names) {
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            for (int i=0; i < names.length; i++) {
                String name = "E" + Integer.toString(x);
                if (name.equals(names[i])) {
                    validName = false;
                    break;
                }
            }
            if (validName) {
                return "E" + Integer.toString(x);
            }
            x++;
        }
        throw new RuntimeException("Unable to find a new socket name");
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractDigitalExpression.class);
}
