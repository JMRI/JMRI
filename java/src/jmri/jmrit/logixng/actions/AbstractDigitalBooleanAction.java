package jmri.jmrit.logixng.actions;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.DigitalBooleanActionBean;

/**
 * The base class for LogixNG Boolean Actions
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public abstract class AbstractDigitalBooleanAction extends AbstractBase
        implements DigitalBooleanActionBean {

    private Base _parent = null;
    private Lock _lock = Lock.NONE;
    private int _state = DigitalBooleanActionBean.UNKNOWN;
    
    
    public AbstractDigitalBooleanAction(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(DigitalBooleanActionManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid: "+mSystemName);
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
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            for (int i=0; i < getChildCount(); i++) {
                String name = "A" + Integer.toString(x);
                if (name.equals(getChild(i).getName())) {
                    validName = false;
                    break;
                }
            }
            if (validName) {
                return "A" + Integer.toString(x);
            }
            x++;
        }
        throw new RuntimeException("Unable to find a new socket name");
    }

    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameDigitalBooleanAction");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in AbstractDigitalBooleanAction.");  // NOI18N
        _state = s;
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in AbstractDigitalBooleanAction.");  // NOI18N
        return _state;
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractDigitalBooleanAction.class);
}
