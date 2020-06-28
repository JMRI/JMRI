package jmri.jmrit.logixng.digital.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.util.Log4JUtil;
import jmri.util.ThreadingUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.LogixNGPluginFactory;
import jmri.managers.AbstractManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrit.logixng.FemaleDigitalBooleanActionSocket;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.DigitalBooleanActionBean;
import jmri.jmrit.logixng.DigitalBooleanActionFactory;
import jmri.jmrit.logixng.MaleDigitalBooleanActionSocket;

/**
 * Class providing the basic logic of the DigitalBooleanActionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultDigitalBooleanActionManager extends AbstractManager<MaleDigitalBooleanActionSocket>
        implements DigitalBooleanActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();

    
    public DefaultDigitalBooleanActionManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
        InstanceManager.getDefault(LogixNG_Manager.class)
                .registerFemaleSocketFactory(new DefaultFemaleDigitalBooleanActionSocketFactory());
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (DigitalBooleanActionFactory actionFactory : ServiceLoader.load(DigitalBooleanActionFactory.class)) {
            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
        for (LogixNGPluginFactory actionFactory : ServiceLoader.load(LogixNGPluginFactory.class)) {
            actionFactory.getActionClasses().forEach((entry) -> {
//                System.out.format("Add action plugin: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
    }

    protected MaleDigitalBooleanActionSocket createMaleActionSocket(DigitalBooleanActionBean action) {
        MaleDigitalBooleanActionSocket socket = new DefaultMaleDigitalBooleanActionSocket(action);
        action.setParent(socket);
        return socket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleDigitalBooleanActionSocket for the action.
     *
     * @param action the bean
     */
    @Override
    public MaleDigitalBooleanActionSocket registerAction(@Nonnull DigitalBooleanActionBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleDigitalBooleanActionSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleDigitalBooleanActionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", action.getSystemName()));
        }
        
        // Keep track of the last created auto system name
        updateAutoNumber(action.getSystemName());
        
        // save in the maps
        MaleDigitalBooleanActionSocket maleSocket = createMaleActionSocket(action);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_DIGITAL_BOOLEAN_ACTIONS;
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
    public FemaleDigitalBooleanActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleDigitalBooleanActionSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameDigitalBooleanActions" : "BeanNameDigitalBooleanAction");
    }
    
    static volatile DefaultDigitalBooleanActionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultDigitalBooleanActionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            Log4JUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultDigitalBooleanActionManager(
                    InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
        return (_instance);
    }

    @Override
    public Class<MaleDigitalBooleanActionSocket> getNamedBeanClass() {
        return MaleDigitalBooleanActionSocket.class;
    }
    
    private final static Logger log = LoggerFactory.getLogger(DefaultDigitalBooleanActionManager.class);

}
