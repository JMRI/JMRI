package jmri.jmrit.logixng.digital.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.managers.AbstractManager;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

/**
 * Class providing the basic logic of the DigitalActionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultDigitalActionManager extends AbstractManager<MaleDigitalActionSocket>
        implements DigitalActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();

    
    public DefaultDigitalActionManager() {
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (DigitalActionFactory actionFactory : ServiceLoader.load(DigitalActionFactory.class)) {
            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
    }

    protected MaleDigitalActionSocket createMaleActionSocket(DigitalActionBean action) {
        MaleDigitalActionSocket socket = new DefaultMaleDigitalActionSocket(action);
        action.setParent(socket);
        return socket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleDigitalActionSocket for the action.
     *
     * @param action the bean
     */
    @Override
    public MaleDigitalActionSocket registerAction(@Nonnull DigitalActionBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleDigitalActionSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleDigitalActionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", action.getSystemName()));
        }
        
        // Keep track of the last created auto system name
        updateAutoNumber(action.getSystemName());
        
        // save in the maps
        MaleDigitalActionSocket maleSocket = createMaleActionSocket(action);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_DIGITAL_ACTIONS;
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
    public FemaleDigitalActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleDigitalActionSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameDigitalActions" : "BeanNameDigitalAction");
    }
    
    static volatile DefaultDigitalActionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultDigitalActionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultDigitalActionManager();
        }
        return (_instance);
    }

    @Override
    public Class<MaleDigitalActionSocket> getNamedBeanClass() {
        return MaleDigitalActionSocket.class;
    }
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultDigitalActionManager.class);

}
