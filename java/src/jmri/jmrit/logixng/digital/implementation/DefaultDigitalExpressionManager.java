package jmri.jmrit.logixng.digital.implementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.Category;
import jmri.util.Log4JUtil;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.LogixNGPluginFactory;
import jmri.jmrit.logixng.DigitalExpressionFactory;
import jmri.jmrit.logixng.DigitalExpressionManager;
import jmri.jmrit.logixng.FemaleDigitalExpressionSocket;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleDigitalExpressionSocket;
import jmri.managers.AbstractManager;
import jmri.jmrit.logixng.DigitalExpressionBean;
import jmri.jmrit.logixng.FemaleGenericExpressionSocket;
import jmri.jmrit.logixng.implementation.DefaultFemaleGenericExpressionSocket;
import jmri.jmrit.logixng.implementation.LogixNGPreferences;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Class providing the basic logic of the DigitalExpressionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultDigitalExpressionManager extends AbstractManager<MaleDigitalExpressionSocket>
        implements DigitalExpressionManager, InstanceManagerAutoDefault {

    private final Map<Category, List<Class<? extends Base>>> expressionClassList = new HashMap<>();

    
    public DefaultDigitalExpressionManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
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
        
//        System.out.format("Read plugin expressions%n");
        for (LogixNGPluginFactory expressionFactory : ServiceLoader.load(LogixNGPluginFactory.class)) {
//            System.out.format("Read plugin factory: %s%n", expressionFactory.getClass().getName());
            expressionFactory.getExpressionClasses().forEach((entry) -> {
//                System.out.format("Add expression plugin: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                expressionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
    }

    protected MaleDigitalExpressionSocket createMaleExpressionSocket(DigitalExpressionBean expression) {
        MaleDigitalExpressionSocket socket = new DefaultMaleDigitalExpressionSocket(expression);
        expression.setParent(socket);
        return socket;
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
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_DIGITAL_EXPRESSIONS;
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
        return LogixNG_Manager.validSystemNameFormat(
                getSubSystemNamePrefix(), systemName);
    }

    @Override
    public FemaleDigitalExpressionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        
        LogixNGPreferences preferences = InstanceManager.getDefault(LogixNGPreferences.class);
//        if (preferences.getUseGenericFemaleSockets() && false) {
        if (preferences.getUseGenericFemaleSockets()) {
            return new DefaultFemaleGenericExpressionSocket(
                    FemaleGenericExpressionSocket.SocketType.DIGITAL, parent, listener, socketName)
                    .getDigitalSocket();
        } else {
            return new DefaultFemaleDigitalExpressionSocket(parent, listener, socketName);
        }
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
    
    static volatile DefaultDigitalExpressionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultDigitalExpressionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            Log4JUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultDigitalExpressionManager(
                    InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
        return (_instance);
    }

    @Override
    public Class<MaleDigitalExpressionSocket> getNamedBeanClass() {
        return MaleDigitalExpressionSocket.class;
    }

    private final static Logger log = LoggerFactory.getLogger(DefaultDigitalExpressionManager.class);

}
