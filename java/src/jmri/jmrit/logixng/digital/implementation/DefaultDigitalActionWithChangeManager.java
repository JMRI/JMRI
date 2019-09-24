package jmri.jmrit.logixng.digital.implementation;

import java.text.DecimalFormat;
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
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.managers.AbstractManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrit.logixng.DigitalActionWithChangeManager;
import jmri.jmrit.logixng.DigitalActionWithChangeFactory;
import jmri.jmrit.logixng.FemaleDigitalActionWithChangeSocket;
import jmri.jmrit.logixng.DigitalActionWithChangeBean;
import jmri.jmrit.logixng.MaleDigitalActionWithChangeSocket;

/**
 * Class providing the basic logic of the DigitalActionWithChangeManager interface.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultDigitalActionWithChangeManager extends AbstractManager<MaleDigitalActionWithChangeSocket>
        implements DigitalActionWithChangeManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private int lastAutoActionRef = 0;
    
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    
    public DefaultDigitalActionWithChangeManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
        InstanceManager.getDefault(LogixNG_Manager.class)
                .registerFemaleSocketFactory(new DefaultFemaleDigitalActionWithChangeSocketFactory());
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (DigitalActionWithChangeFactory actionFactory : ServiceLoader.load(DigitalActionWithChangeFactory.class)) {
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

    protected MaleDigitalActionWithChangeSocket createMaleActionSocket(DigitalActionWithChangeBean action) {
        MaleDigitalActionWithChangeSocket socket = new DefaultMaleDigitalActionWithChangeSocket(action);
        action.setParent(socket);
        return socket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleDigitalActionWithChangeSocket for the action.
     *
     * @param action the bean
     */
    @Override
    public MaleDigitalActionWithChangeSocket registerAction(@Nonnull DigitalActionWithChangeBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleDigitalActionWithChangeSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleDigitalActionWithChangeSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", action.getSystemName()));
        }
        
        // Remove the letters in the beginning to get only the number of the
        // system name.
        String actionNumberStr = action.getSystemName().replaceAll(getSystemNamePrefix()+"DC:?", "");
        int actionNumber = Integer.parseInt(actionNumberStr);
        if (lastAutoActionRef < actionNumber) {
            lastAutoActionRef = actionNumber;
        }
        
        // save in the maps
        MaleDigitalActionWithChangeSocket maleSocket = createMaleActionSocket(action);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNGS;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameAction");
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
        if (systemName.matches(getSystemNamePrefix()+"DC:?\\d+")) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    @Override
    public String getNewSystemName() {
        int nextAutoLogixNGRef = ++lastAutoActionRef;
        StringBuilder b = new StringBuilder(getSystemNamePrefix());
        b.append("DC:");
        String nextNumber = paddedNumber.format(nextAutoLogixNGRef);
        b.append(nextNumber);
        return b.toString();
    }

    @Override
    public FemaleDigitalActionWithChangeSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleDigitalActionWithChangeSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }
/*
    @Override
    public void addAction(DigitalActionWithChangeBean action) throws IllegalArgumentException {
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException("System name is invalid");
        }
        // save in the maps
        registerAction(action);
    }
/*
    @Override
    public DigitalActionWithChangeBean getAction(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DigitalActionWithChangeBean getByUserName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DigitalActionWithChangeBean getBySystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAction(DigitalActionWithChangeBean x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameDigitalActionWithChanges" : "BeanNameDigitalActionWithChange");
    }
    
    static volatile DefaultDigitalActionWithChangeManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultDigitalActionWithChangeManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            Log4JUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultDigitalActionWithChangeManager(
                    InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
        return (_instance);
    }
    
    private final static Logger log = LoggerFactory.getLogger(DefaultDigitalActionWithChangeManager.class);

}
