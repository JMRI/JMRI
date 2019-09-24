package jmri.jmrit.logixng.digital.actions_with_change;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.DigitalActionWithChangeManager;
import jmri.jmrit.logixng.DigitalActionWithChangeBean;

/**
 * The base class for LogixNG Actions
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public abstract class AbstractDigitalActionWithChange extends AbstractBase
        implements DigitalActionWithChangeBean {

    private Base _parent = null;
    private Lock _lock = Lock.NONE;
    private int _state = DigitalActionWithChangeBean.UNKNOWN;
    
    
    public AbstractDigitalActionWithChange(String sys) throws BadSystemNameException {
        super(sys);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(DigitalActionWithChangeManager.class).validSystemNameFormat(mSystemName);
        if (isNameValid != Manager.NameValidity.VALID) {
            throw new IllegalArgumentException("system name is not valid");
        }
    }

    public AbstractDigitalActionWithChange(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(DigitalActionWithChangeManager.class).validSystemNameFormat(mSystemName);
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
        return Bundle.getMessage("BeanNameDigitalActionWithChange");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in AbstractDigitalActionWithChange.");  // NOI18N
        _state = s;
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in AbstractDigitalActionWithChange.");  // NOI18N
        return _state;
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(AbstractDigitalActionWithChange.class);
}
