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
 * Class providing the basic logic of the ExpressionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultAnalogExpressionManager extends AbstractBaseManager<MaleAnalogExpressionSocket>
        implements AnalogExpressionManager, InstanceManagerAutoDefault {

    private final Map<Category, List<Class<? extends Base>>> expressionClassList = new HashMap<>();
    private MaleSocket _lastRegisteredBean;

    
    public DefaultAnalogExpressionManager() {
        InstanceManager.getDefault(LogixNG_Manager.class).registerManager(this);
        
        for (AnalogExpressionFactory expressionFactory : ServiceLoader.load(AnalogExpressionFactory.class)) {
            expressionFactory.init();
        }
        
        for (Category category : Category.values()) {
            expressionClassList.put(category, new ArrayList<>());
        }
        
//        System.out.format("Read expressions%n");
        for (AnalogExpressionFactory expressionFactory : ServiceLoader.load(AnalogExpressionFactory.class)) {
            expressionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add expression: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                expressionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
        for (MaleAnalogExpressionSocketFactory maleSocketFactory : ServiceLoader.load(MaleAnalogExpressionSocketFactory.class)) {
            _maleSocketFactories.add(maleSocketFactory);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends MaleSocket> getMaleSocketClass() {
        return DefaultMaleAnalogExpressionSocket.class;
    }

    protected MaleAnalogExpressionSocket createMaleAnalogExpressionSocket(AnalogExpressionBean expression) {
        MaleAnalogExpressionSocket socket = new DefaultMaleAnalogExpressionSocket(this, expression);
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
    public void register(MaleSocket maleSocket) {
        if (!(maleSocket instanceof MaleAnalogExpressionSocket)) {
            throw new IllegalArgumentException("maleSocket is not a MaleAnalogExpressionSocket");
        }
        register((MaleAnalogExpressionSocket)maleSocket);
    }
    
    /** {@inheritDoc} */
    @Override
    public void register(MaleAnalogExpressionSocket maleSocket) {
        super.register(maleSocket);
        _lastRegisteredBean = maleSocket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleActionSocket for the action.
     *
     * @param expression the bean
     */
    @Override
    public MaleAnalogExpressionSocket registerExpression(@Nonnull AnalogExpressionBean expression)
            throws IllegalArgumentException {
        
        if (expression instanceof MaleAnalogExpressionSocket) {
            throw new IllegalArgumentException("registerExpression() cannot register a MaleAnalogExpressionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(expression.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + expression.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", expression.getSystemName()));
        }
        
        // Keep track of the last created auto system name
        updateAutoNumber(expression.getSystemName());
        
        // save in the maps
        MaleAnalogExpressionSocket maleSocket = createMaleAnalogExpressionSocket(expression);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_ANALOG_EXPRESSIONS;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameAnalogExpression");
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAnalogExpression(MaleAnalogExpressionSocket x) {
        // delete the MaleAnalogExpressionSocket
        deregister(x);
        x.dispose();
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
    public FemaleAnalogExpressionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        
        LogixNGPreferences preferences = InstanceManager.getDefault(LogixNGPreferences.class);
//        preferences.setUseGenericFemaleSockets(false);
        if (preferences.getUseGenericFemaleSockets()) {
            return new DefaultFemaleGenericExpressionSocket(
                    FemaleGenericExpressionSocket.SocketType.ANALOG, parent, listener, socketName)
                    .getAnalogSocket(parent);
        } else {
            return new DefaultFemaleAnalogExpressionSocket(parent, listener, socketName);
        }
    }
    
    @Override
    public Map<Category, List<Class<? extends Base>>> getExpressionClasses() {
        return expressionClassList;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameAnalogExpressions" : "BeanNameAnalogExpression");
    }
    
    static volatile DefaultAnalogExpressionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultAnalogExpressionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultAnalogExpressionManager();
        }
        return (_instance);
    }

    @Override
    public Class<MaleAnalogExpressionSocket> getNamedBeanClass() {
        return MaleAnalogExpressionSocket.class;
    }

    @Override
    protected MaleAnalogExpressionSocket castBean(MaleSocket maleSocket) {
        return (MaleAnalogExpressionSocket)maleSocket;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAnalogExpressionManager.class);

}
