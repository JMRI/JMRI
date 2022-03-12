package jmri.jmrit.logixng.implementation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;

import jmri.InstanceManager;
import jmri.InvokeOnGuiThread;
import jmri.jmrit.logixng.*;
import jmri.util.LoggingUtil;
import jmri.util.ThreadingUtil;

/**
 * Class providing the basic logic of the DigitalActionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultDigitalActionManager extends AbstractBaseManager<MaleDigitalActionSocket>
        implements DigitalActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private MaleSocket _lastRegisteredBean;

    
    public DefaultDigitalActionManager() {
        InstanceManager.getDefault(LogixNG_Manager.class).registerManager(this);
        
        for (DigitalActionFactory actionFactory : ServiceLoader.load(DigitalActionFactory.class)) {
            actionFactory.init();
        }
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (DigitalActionFactory actionFactory : ServiceLoader.load(DigitalActionFactory.class)) {
            actionFactory.getActionClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
        for (MaleDigitalActionSocketFactory maleSocketFactory : ServiceLoader.load(MaleDigitalActionSocketFactory.class)) {
            _maleSocketFactories.add(maleSocketFactory);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends MaleSocket> getMaleSocketClass() {
        return DefaultMaleDigitalActionSocket.class;
    }

    protected MaleDigitalActionSocket createMaleActionSocket(DigitalActionBean action) {
        MaleDigitalActionSocket socket = new DefaultMaleDigitalActionSocket(this, action);
        action.setParent(socket);
        return socket;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket getLastRegisteredMaleSocket() {
        return _lastRegisteredBean;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleDigitalActionSocket registerBean(MaleDigitalActionSocket maleSocket) {
        MaleDigitalActionSocket bean = super.registerBean(maleSocket);
        _lastRegisteredBean = maleSocket;
        return bean;
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
        return registerBean(maleSocket);
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_DIGITAL_ACTIONS;
    }

    @Override
    public char typeLetter() {
        return 'Q';
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

    /** {@inheritDoc} */
    @Override
    public void deleteDigitalAction(MaleDigitalActionSocket x) {
        // delete the MaleDigitalActionSocket
        deregister(x);
        x.dispose();
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

    @Override
    protected MaleDigitalActionSocket castBean(MaleSocket maleSocket) {
        return (MaleDigitalActionSocket)maleSocket;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultDigitalActionManager.class);

}
