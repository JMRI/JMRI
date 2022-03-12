package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.util.*;

/**
 * Class providing the basic logic of the DigitalExpressionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultDigitalExpressionManager extends AbstractBaseManager<MaleDigitalExpressionSocket>
        implements DigitalExpressionManager, InstanceManagerAutoDefault {

    private final Map<Category, List<Class<? extends Base>>> expressionClassList = new HashMap<>();
    private MaleSocket _lastRegisteredBean;

    
    public DefaultDigitalExpressionManager() {
        InstanceManager.getDefault(LogixNG_Manager.class).registerManager(this);
        
        for (DigitalExpressionFactory expressionFactory : ServiceLoader.load(DigitalExpressionFactory.class)) {
            expressionFactory.init();
        }
        
        for (Category category : Category.values()) {
            expressionClassList.put(category, new ArrayList<>());
        }
        
//        System.out.format("Read expressions%n");
        for (DigitalExpressionFactory expressionFactory : ServiceLoader.load(DigitalExpressionFactory.class)) {
            expressionFactory.getExpressionClasses().forEach((entry) -> {
//                System.out.format("Add expression: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                expressionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
        for (MaleDigitalExpressionSocketFactory maleSocketFactory : ServiceLoader.load(MaleDigitalExpressionSocketFactory.class)) {
            _maleSocketFactories.add(maleSocketFactory);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends MaleSocket> getMaleSocketClass() {
        return DefaultMaleDigitalExpressionSocket.class;
    }

    protected MaleDigitalExpressionSocket createMaleExpressionSocket(DigitalExpressionBean expression) {
        MaleDigitalExpressionSocket socket = new DefaultMaleDigitalExpressionSocket(this, expression);
        expression.setParent(socket);
        return socket;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket getLastRegisteredMaleSocket() {
        return _lastRegisteredBean;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleDigitalExpressionSocket registerBean(MaleDigitalExpressionSocket maleSocket) {
        MaleDigitalExpressionSocket bean = super.registerBean(maleSocket);
        _lastRegisteredBean = maleSocket;
        return bean;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleActionSocket for the action.
     *
     * @param expression the bean
     */
    @Override
    public MaleDigitalExpressionSocket registerExpression(@Nonnull DigitalExpressionBean expression)
            throws IllegalArgumentException {
        
        if (expression instanceof MaleDigitalExpressionSocket) {
            throw new IllegalArgumentException("registerExpression() cannot register a MaleDigitalExpressionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(expression.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + expression.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException("System name is invalid: "+expression.getSystemName());
        }
        
        // Keep track of the last created auto system name
        updateAutoNumber(expression.getSystemName());
        
        // save in the maps
        MaleDigitalExpressionSocket maleSocket = createMaleExpressionSocket(expression);
        return registerBean(maleSocket);
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_DIGITAL_EXPRESSIONS;
    }

    @Override
    public char typeLetter() {
        return 'Q';
    }

    /*.*
     * Test if parameter is a properly formatted system name.
     *
     * @param systemName the system name
     * @return enum indicating current validity, which might be just as a prefix
     *./
    @Override
    public NameValidity validSystemNameFormat(String systemName) {
        return LogixNG_Manager.validSystemNameFormat(
                getSubSystemNamePrefix(), systemName);
    }
*/
    @Override
    public FemaleDigitalExpressionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        
        return new DefaultFemaleDigitalExpressionSocket(parent, listener, socketName);
    }
    
    @Override
    public Map<Category, List<Class<? extends Base>>> getExpressionClasses() {
        return expressionClassList;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameDigitalExpressions" : "BeanNameDigitalExpression");
    }

    /** {@inheritDoc} */
    @Override
    public void deleteDigitalExpression(MaleDigitalExpressionSocket x) {
        // delete the MaleDigitalExpressionSocket
        deregister(x);
        x.dispose();
    }
    
    static volatile DefaultDigitalExpressionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultDigitalExpressionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultDigitalExpressionManager();
        }
        return (_instance);
    }

    @Override
    public Class<MaleDigitalExpressionSocket> getNamedBeanClass() {
        return MaleDigitalExpressionSocket.class;
    }

    @Override
    protected MaleDigitalExpressionSocket castBean(MaleSocket maleSocket) {
        return (MaleDigitalExpressionSocket)maleSocket;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultDigitalExpressionManager.class);

}
