package jmri.jmrit.logixng.analog.expressions;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.AnalogExpressionBean;
import jmri.jmrit.logixng.AnalogExpressionManager;

/**
 *
 */
public abstract class AbstractAnalogExpression extends AbstractBase
        implements AnalogExpressionBean {

    private Base _parent = null;
    private Lock _lock = Lock.NONE;
    private int _state = AnalogExpressionBean.UNKNOWN;


    public AbstractAnalogExpression(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(AnalogExpressionManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
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

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameAnalogExpression");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in AbstractAnalogExpression.");  // NOI18N
        _state = s;
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in AbstractAnalogExpression.");  // NOI18N
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
    

    private final static Logger log = LoggerFactory.getLogger(AbstractAnalogExpression.class);
}
