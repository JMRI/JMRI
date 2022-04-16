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
import jmri.util.*;

/**
 * Class providing the basic logic of the ActionManager interface.
 * 
 * @author Dave Duchamp       Copyright (C) 2007
 * @author Daniel Bergqvist   Copyright (C) 2018
 */
public class DefaultAnalogActionManager extends AbstractBaseManager<MaleAnalogActionSocket>
        implements AnalogActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private MaleSocket _lastRegisteredBean;

    
    public DefaultAnalogActionManager() {
        InstanceManager.getDefault(LogixNG_Manager.class).registerManager(this);
        
        for (AnalogActionFactory actionFactory : ServiceLoader.load(AnalogActionFactory.class)) {
            actionFactory.init();
        }
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (AnalogActionFactory actionFactory : ServiceLoader.load(AnalogActionFactory.class)) {
            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
        for (MaleAnalogActionSocketFactory maleSocketFactory : ServiceLoader.load(MaleAnalogActionSocketFactory.class)) {
            _maleSocketFactories.add(maleSocketFactory);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends MaleSocket> getMaleSocketClass() {
        return DefaultMaleAnalogActionSocket.class;
    }

    protected MaleAnalogActionSocket createMaleActionSocket(AnalogActionBean action) {
        MaleAnalogActionSocket socket = new DefaultMaleAnalogActionSocket(this, action);
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
    public MaleAnalogActionSocket registerBean(MaleAnalogActionSocket maleSocket) {
        MaleAnalogActionSocket bean = super.registerBean(maleSocket);
        _lastRegisteredBean = maleSocket;
        return bean;
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
        return registerBean(maleSocket);
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_ANALOG_ACTIONS;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameAnalogAction");
    }

    /** {@inheritDoc} */
    @Override
    public void deleteAnalogAction(MaleAnalogActionSocket x) {
        // delete the MaleAnalogActionSocket
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
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultAnalogActionManager();
        }
        return (_instance);
    }

    @Override
    public Class<MaleAnalogActionSocket> getNamedBeanClass() {
        return MaleAnalogActionSocket.class;
    }

    @Override
    protected MaleAnalogActionSocket castBean(MaleSocket maleSocket) {
        return (MaleAnalogActionSocket)maleSocket;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultAnalogActionManager.class);

}
