package jmri.jmrit.logixng.logixemulator.implementation;

import jmri.jmrit.logixng.digital.implementation.*;
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
import jmri.jmrit.logixng.LogixEmulatorActionFactory;
import jmri.jmrit.logixng.LogixEmulatorActionManager;
import jmri.jmrit.logixng.FemaleLogixEmulatorActionSocket;
import jmri.jmrit.logixng.MaleLogixEmulatorActionSocket;
import jmri.jmrit.logixng.SocketAlreadyConnectedException;
import jmri.managers.AbstractManager;
import jmri.jmrit.logixng.LogixEmulatorActionBean;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Class providing the basic logic of the LogixEmulatorActionManager interface.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultLogixEmulatorActionManager extends AbstractManager<MaleLogixEmulatorActionSocket>
        implements LogixEmulatorActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private int lastAutoActionRef = 0;
    
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    
    public DefaultLogixEmulatorActionManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
        InstanceManager.getDefault(LogixNG_Manager.class)
                .registerFemaleSocketFactory(new DefaultFemaleLogixEmulatorActionSocketFactory());
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (LogixEmulatorActionFactory actionFactory : ServiceLoader.load(LogixEmulatorActionFactory.class)) {
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

    protected MaleLogixEmulatorActionSocket createMaleActionSocket(LogixEmulatorActionBean action) {
        MaleLogixEmulatorActionSocket socket = new DefaultMaleLogixEmulatorActionSocket(action);
        action.setParent(socket);
        return socket;
    }
    
    /**
     * Remember a NamedBean Object created outside the manager.
     * This method creates a MaleLogixEmulatorActionSocket for the action.
     *
     * @param action the bean
     */
    @Override
    public MaleLogixEmulatorActionSocket registerAction(@Nonnull LogixEmulatorActionBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleLogixEmulatorActionSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleLogixEmulatorActionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException(String.format("System name is invalid: %s", action.getSystemName()));
        }
        
        // Remove the letters in the beginning to get only the number of the
        // system name.
        String actionNumberStr = action.getSystemName().replaceAll(getSystemNamePrefix()+"DA:?", "");
        int actionNumber = Integer.parseInt(actionNumberStr);
        if (lastAutoActionRef < actionNumber) {
            lastAutoActionRef = actionNumber;
        }
        
        // save in the maps
        MaleLogixEmulatorActionSocket maleSocket = createMaleActionSocket(action);
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
        if (systemName.matches(getSystemNamePrefix()+"DA:?\\d+")) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    @Override
    public String getNewSystemName() {
        int nextAutoLogixNGRef = ++lastAutoActionRef;
        StringBuilder b = new StringBuilder(getSystemNamePrefix());
        b.append("DA:");
        String nextNumber = paddedNumber.format(nextAutoLogixNGRef);
        b.append(nextNumber);
        return b.toString();
    }

    @Override
    public FemaleLogixEmulatorActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleLogixEmulatorActionSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }
/*
    @Override
    public void addAction(LogixEmulatorActionBean action) throws IllegalArgumentException {
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
    public LogixEmulatorActionBean getAction(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LogixEmulatorActionBean getByUserName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LogixEmulatorActionBean getBySystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAction(LogixEmulatorActionBean x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameLogixEmulatorActions" : "BeanNameLogixEmulatorAction");
    }
    
    static volatile DefaultLogixEmulatorActionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultLogixEmulatorActionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            Log4JUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultLogixEmulatorActionManager(
                    InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
        return (_instance);
    }
    
    private final static Logger log = LoggerFactory.getLogger(DefaultLogixEmulatorActionManager.class);

}
