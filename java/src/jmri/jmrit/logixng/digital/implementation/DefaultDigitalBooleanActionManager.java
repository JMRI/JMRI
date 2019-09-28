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
import jmri.managers.AbstractManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.jmrit.logixng.DigitalActionWithChangeFactory;
import jmri.jmrit.logixng.MaleDigitalActionWithChangeSocket;
import jmri.jmrit.logixng.FemaleDigitalBooleanActionSocket;
import jmri.jmrit.logixng.DigitalBooleanActionManager;
import jmri.jmrit.logixng.DigitalBooleanActionBean;

/**
 * Class providing the basic logic of the DigitalBooleanActionManager interface.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultDigitalBooleanActionManager extends AbstractManager<MaleDigitalActionWithChangeSocket>
        implements DigitalBooleanActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private int lastAutoActionRef = 0;
    
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    
    public DefaultDigitalBooleanActionManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
        InstanceManager.getDefault(LogixNG_Manager.class)
                .registerFemaleSocketFactory(new DefaultFemaleDigitalBooleanActionSocketFactory());
        
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

    protected MaleDigitalActionWithChangeSocket createMaleActionSocket(DigitalBooleanActionBean action) {
        MaleDigitalActionWithChangeSocket socket = new DefaultMaleDigitalBooleanActionSocket(action);
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
    public MaleDigitalActionWithChangeSocket registerAction(@Nonnull DigitalBooleanActionBean action)
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
        String actionNumberStr = action.getSystemName().replaceAll(getSystemNamePrefix()+"DB:?", "");
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
        if (systemName.matches(getSystemNamePrefix()+"DB:?\\d+")) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    @Override
    public String getNewSystemName() {
        int nextAutoLogixNGRef = ++lastAutoActionRef;
        StringBuilder b = new StringBuilder(getSystemNamePrefix());
        b.append("DB:");
        String nextNumber = paddedNumber.format(nextAutoLogixNGRef);
        b.append(nextNumber);
        return b.toString();
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
/*
    @Override
    public void addAction(DigitalBooleanActionBean action) throws IllegalArgumentException {
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
    public DigitalBooleanActionBean getAction(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DigitalBooleanActionBean getByUserName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DigitalBooleanActionBean getBySystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAction(DigitalBooleanActionBean x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameDigitalActionWithChanges" : "BeanNameDigitalActionWithChange");
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
    
    private final static Logger log = LoggerFactory.getLogger(DefaultDigitalBooleanActionManager.class);

}
