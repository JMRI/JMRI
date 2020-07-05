package jmri.jmrit.logixng.implementation;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.digital.actions.IfThenElse;
import jmri.jmrit.logixng.digital.actions.Many;
import jmri.managers.AbstractManager;
import jmri.util.*;

/**
 * Class providing the basic logic of the ConditionalNG_Manager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultConditionalNGManager extends AbstractManager<ConditionalNG>
        implements ConditionalNG_Manager {

    
    public DefaultConditionalNGManager() {
        // LogixNGPreferences class may load plugins so we must ensure
        // it's loaded here.
        InstanceManager.getDefault(LogixNGPreferences.class);
    }

    @Override
    public int getXMLOrder() {
        return LOGIXNG_CONDITIONALNGS;
    }

    @Override
    public char typeLetter() {
        return 'Q';
    }

    /**
     * Test if parameter is a properly formatted system name.
     *
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     */
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        if (systemName.matches(getSubSystemNamePrefix()+"(:AUTO:)?\\d+")) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    /**
     * Method to create a new ConditionalNG if the ConditionalNG does not exist.
     * <p>
     * Returns null if
     * a Logix with the same systemName or userName already exists, or if there
     * is trouble creating a new ConditionalNG.
     */
    @Override
    public ConditionalNG createConditionalNG(String systemName, String userName)
            throws IllegalArgumentException {
        
        // Check that ConditionalNG does not already exist
        ConditionalNG x;
        if (userName != null && !userName.equals("")) {
            x = getByUserName(userName);
            if (x != null) {
                log.error("username {} already exists", userName);
                return null;
            }
        }
        x = getBySystemName(systemName);
        if (x != null) {
            log.error("systemname {} already exists", systemName);
            return null;
        }
        // Check if system name is valid
        if (this.validSystemNameFormat(systemName) != NameValidity.VALID) {
            throw new IllegalArgumentException("SystemName " + systemName + " is not in the correct format");
        }
        // ConditionalNG does not exist, create a new ConditionalNG
        x = new DefaultConditionalNG(systemName, userName);
        // save in the maps
        register(x);
        
        // Keep track of the last created auto system name
        updateAutoNumber(systemName);
        
//        if (setupTree) {
            // Setup initial tree for the ConditionalNG
//            setupInitialConditionalNGTree(x);
//            throw new UnsupportedOperationException("Throw exception for now until this is fixed");
//        }
        
        return x;
    }

    @Override
    public ConditionalNG createConditionalNG(String userName) throws IllegalArgumentException {
        return createConditionalNG(getAutoSystemName(), userName);
    }

    @Override
    public void setupInitialConditionalNGTree(ConditionalNG conditionalNG) {
        try {
            DigitalActionManager digitalActionManager =
                    InstanceManager.getDefault(DigitalActionManager.class);
            
            FemaleSocket femaleSocket = conditionalNG.getFemaleSocket();
            MaleDigitalActionSocket actionManySocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new Many(digitalActionManager.getAutoSystemName(), null));
            femaleSocket.connect(actionManySocket);
            femaleSocket.setLock(Base.Lock.HARD_LOCK);

            femaleSocket = actionManySocket.getChild(0);
            MaleDigitalActionSocket actionIfThenSocket =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new IfThenElse(digitalActionManager.getAutoSystemName(), null, IfThenElse.Type.TRIGGER_ACTION));
            femaleSocket.connect(actionIfThenSocket);
            
            /* FOR TESTING ONLY */
            /* FOR TESTING ONLY */
            /* FOR TESTING ONLY */
            /* FOR TESTING ONLY */
/*            
            femaleSocket = actionIfThenSocket.getChild(0);
            MaleDigitalExpressionSocket expressionAndSocket =
                    InstanceManager.getDefault(DigitalExpressionManager.class)
                            .registerExpression(new And(femaleSocket.getConditionalNG()));
            femaleSocket.connect(expressionAndSocket);
            
            femaleSocket = actionIfThenSocket.getChild(1);
            MaleDigitalActionSocket actionIfThenSocket2 =
                    InstanceManager.getDefault(DigitalActionManager.class)
                            .registerAction(new IfThenElse(femaleSocket.getConditionalNG(), IfThenElse.Type.CONTINOUS_ACTION));
            femaleSocket.connect(actionIfThenSocket2);
*/            
            /* FOR TESTING ONLY */
            /* FOR TESTING ONLY */
            /* FOR TESTING ONLY */
            /* FOR TESTING ONLY */

        } catch (SocketAlreadyConnectedException e) {
            // This should never be able to happen.
            throw new RuntimeException(e);
        }
    }
    
    @Override
    public ConditionalNG getConditionalNG(String name) {
        ConditionalNG x = getByUserName(name);
        if (x != null) {
            return x;
        }
        return getBySystemName(name);
    }

    @Override
    public ConditionalNG getByUserName(String name) {
        return _tuser.get(name);
    }

    @Override
    public ConditionalNG getBySystemName(String name) {
        return _tsys.get(name);
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameConditionalNGs" : "BeanNameConditionalNG");
    }

    /** {@inheritDoc} */
    @Override
    public void resolveAllTrees() {
        for (ConditionalNG conditionalNG : _tsys.values()) {
            conditionalNG.setParentForAllChildren();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public void setupAllConditionalNGs() {
        for (ConditionalNG conditionalNG : _tsys.values()) {
            conditionalNG.setup();
        }
    }

    @Override
    public void deleteConditionalNG(ConditionalNG x) {
        // delete the ConditionalNG
        deregister(x);
        x.dispose();
    }

    @Override
    public void setLoadDisabled(boolean s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
    static volatile DefaultConditionalNGManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultConditionalNGManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultConditionalNGManager();
        }
        return (_instance);
    }

    @Override
    public Class<ConditionalNG> getNamedBeanClass() {
        return ConditionalNG.class;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultConditionalNGManager.class);
}
