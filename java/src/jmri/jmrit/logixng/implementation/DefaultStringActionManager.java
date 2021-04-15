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
public class DefaultStringActionManager extends AbstractBaseManager<MaleStringActionSocket>
        implements StringActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private MaleSocket _lastRegisteredBean;

    
    public DefaultStringActionManager() {
        InstanceManager.getDefault(LogixNG_Manager.class).registerManager(this);
        
        for (StringActionFactory actionFactory : ServiceLoader.load(StringActionFactory.class)) {
            actionFactory.init();
        }
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (StringActionFactory actionFactory : ServiceLoader.load(StringActionFactory.class)) {
            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
        for (MaleStringActionSocketFactory maleSocketFactory : ServiceLoader.load(MaleStringActionSocketFactory.class)) {
            _maleSocketFactories.add(maleSocketFactory);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<? extends MaleSocket> getMaleSocketClass() {
        return DefaultMaleStringActionSocket.class;
    }

    protected MaleStringActionSocket createMaleActionSocket(StringActionBean action) {
        MaleStringActionSocket socket = new DefaultMaleStringActionSocket(this, action);
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
    public void register(MaleSocket maleSocket) {
        if (!(maleSocket instanceof MaleStringActionSocket)) {
            throw new IllegalArgumentException("maleSocket is not a MaleStringActionSocket");
        }
        register((MaleStringActionSocket)maleSocket);
    }
    
    /** {@inheritDoc} */
    @Override
    public void register(MaleStringActionSocket maleSocket) {
        super.register(maleSocket);
        _lastRegisteredBean = maleSocket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleActionSocket for the action.
     *
     * @param action the bean
     */
    @Override
    public MaleStringActionSocket registerAction(@Nonnull StringActionBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleStringActionSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleStringActionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", action.getSystemName()));
        }
        
        // Keep track of the last created auto system name
        updateAutoNumber(action.getSystemName());
        
        MaleStringActionSocket maleSocket = createMaleActionSocket(action);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNG_STRING_ACTIONS;
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
    public FemaleStringActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleStringActionSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameStringActions" : "BeanNameStringAction");
    }

    /** {@inheritDoc} */
    @Override
    public void deleteStringAction(MaleStringActionSocket x) {
        // delete the StringAction
        deregister(x);
        x.dispose();
    }
    
    static volatile DefaultStringActionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultStringActionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            LoggingUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultStringActionManager();
        }
        return (_instance);
    }

    @Override
    public Class<MaleStringActionSocket> getNamedBeanClass() {
        return MaleStringActionSocket.class;
    }

    @Override
    protected MaleStringActionSocket castBean(MaleSocket maleSocket) {
        return (MaleStringActionSocket)maleSocket;
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DefaultStringActionManager.class);

}
