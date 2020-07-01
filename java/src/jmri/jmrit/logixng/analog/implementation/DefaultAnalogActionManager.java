package jmri.jmrit.logixng.analog.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.util.Log4JUtil;
import jmri.util.ThreadingUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.managers.AbstractManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Class providing the basic logic of the ActionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultAnalogActionManager extends AbstractManager<MaleAnalogActionSocket>
        implements AnalogActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();

    
    public DefaultAnalogActionManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (AnalogActionFactory actionFactory : ServiceLoader.load(AnalogActionFactory.class)) {
            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
    }

    protected MaleAnalogActionSocket createMaleActionSocket(AnalogActionBean action) {
        MaleAnalogActionSocket socket = new DefaultMaleAnalogActionSocket(action);
        action.setParent(socket);
        return socket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleActionSocket for the action.
     *
     * @param action the bean
     */
    @Override
    public MaleAnalogActionSocket registerAction(@Nonnull AnalogActionBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleAnalogActionSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleAnalogActionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", action.getSystemName()));
        }
        
        // Keep track of the last created auto system name
        updateAutoNumber(action.getSystemName());
        
        MaleAnalogActionSocket maleSocket = createMaleActionSocket(action);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_ANALOG_ACTIONS;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameAnalogAction");
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
    public FemaleAnalogActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleAnalogActionSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameAnalogActions" : "BeanNameAnalogAction");
    }
    
    static volatile DefaultAnalogActionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultAnalogActionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            Log4JUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultAnalogActionManager(
                    InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
        return (_instance);
    }

    @Override
    public Class<MaleAnalogActionSocket> getNamedBeanClass() {
        return MaleAnalogActionSocket.class;
    }
    
    private final static Logger log = LoggerFactory.getLogger(DefaultAnalogActionManager.class);
    
}
