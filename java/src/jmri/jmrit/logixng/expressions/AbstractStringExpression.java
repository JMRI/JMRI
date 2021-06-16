package jmri.jmrit.logixng.expressions;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Manager;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.StringExpressionBean;
import jmri.jmrit.logixng.StringExpressionManager;

/**
 *
 */
public abstract class AbstractStringExpression extends AbstractBase
        implements StringExpressionBean {

    private Base _parent = null;
    private int _state = StringExpressionBean.UNKNOWN;
    boolean _triggerOnChange = true;    // By default, trigger on change


    public AbstractStringExpression(String sys, String user) throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        
        // Do this test here to ensure all the tests are using correct system names
        Manager.NameValidity isNameValid = InstanceManager.getDefault(StringExpressionManager.class).validSystemNameFormat(mSystemName);
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
        return Bundle.getMessage("BeanNameStringExpression");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in AbstractStringExpression.");  // NOI18N
        _state = s;
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in AbstractStringExpression.");  // NOI18N
        return _state;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setTriggerOnChange(boolean triggerOnChange) {
        _triggerOnChange = triggerOnChange;
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean getTriggerOnChange() {
        return _triggerOnChange;
    }
    
    public String getNewSocketName() {
        int x = 1;
        while (x < 10000) {     // Protect from infinite loop
            boolean validName = true;
            for (int i=0; i < getChildCount(); i++) {
                String name = "E" + Integer.toString(x);
                if (name.equals(getChild(i).getName())) {
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
    

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractStringExpression.class);
}
