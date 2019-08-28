package jmri.jmrit.logixng.string.implementation;

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
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleStringActionSocket;
import jmri.jmrit.logixng.FemaleSocketListener;
import jmri.jmrit.logixng.MaleStringActionSocket;
import jmri.managers.AbstractManager;
import jmri.jmrit.logixng.StringActionFactory;
import jmri.jmrit.logixng.StringActionBean;
import jmri.jmrix.internal.InternalSystemConnectionMemo;

/**
 * Class providing the basic logic of the ActionManager interface.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultStringActionManager extends AbstractManager<MaleStringActionSocket>
        implements StringActionManager {

    private final Map<Category, List<Class<? extends Base>>> actionClassList = new HashMap<>();
    private int lastAutoActionRef = 0;
    
    // This is for testing only!!!
    // This number needs to be saved and restored.
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    
    public DefaultStringActionManager(InternalSystemConnectionMemo memo) {
        super(memo);
        
        for (Category category : Category.values()) {
            actionClassList.put(category, new ArrayList<>());
        }
        
        for (StringActionFactory actionFactory : ServiceLoader.load(StringActionFactory.class)) {
            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
                actionClassList.get(entry.getKey()).add(entry.getValue());
            });
        }
        
//        for (LogixNGPluginFactory actionFactory : ServiceLoader.load(LogixNGPluginFactory.class)) {
//            actionFactory.getClasses().forEach((entry) -> {
//                System.out.format("Add action plugin: %s, %s%n", entry.getKey().name(), entry.getValue().getName());
//                actionClassList.get(entry.getKey()).add(entry.getValue());
//            });
//        }
    }

    protected MaleStringActionSocket createMaleActionSocket(StringActionBean action) {
        MaleStringActionSocket socket = new DefaultMaleStringActionSocket(action);
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
    public MaleStringActionSocket registerAction(@Nonnull StringActionBean action)
            throws IllegalArgumentException {
        
        if (action instanceof MaleStringActionSocket) {
            throw new IllegalArgumentException("registerAction() cannot register a MaleStringActionSocket. Use the method register() instead.");
        }
        
        // Check if system name is valid
        if (this.validSystemNameFormat(action.getSystemName()) != NameValidity.VALID) {
            log.warn("SystemName " + action.getSystemName() + " is not in the correct format");
            throw new IllegalArgumentException("System name is invalid");
        }
        
        MaleStringActionSocket maleSocket = createMaleActionSocket(action);
        register(maleSocket);
        return maleSocket;
    }
    
    @Override
    public int getXMLOrder() {
        return LOGIXNGS;
    }

    @Override
    public String getBeanTypeHandled() {
        return Bundle.getMessage("BeanNameStringAction");
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
        if (systemName.matches(getSystemNamePrefix()+"SA:?\\d+")) {
            return NameValidity.VALID;
        } else {
            return NameValidity.INVALID;
        }
    }

    @Override
    public String getNewSystemName() {
        int nextAutoLogixNGRef = ++lastAutoActionRef;
        StringBuilder b = new StringBuilder(getSystemNamePrefix());
        b.append("SA:");
        String nextNumber = paddedNumber.format(nextAutoLogixNGRef);
        b.append(nextNumber);
        return b.toString();
    }

    @Override
    public FemaleStringActionSocket createFemaleSocket(
            Base parent, FemaleSocketListener listener, String socketName) {
        return new DefaultFemaleStringActionSocket(parent, listener, socketName);
    }

    @Override
    public Map<Category, List<Class<? extends Base>>> getActionClasses() {
        return actionClassList;
    }
/*
    @Override
    public void addAction(Action action) throws IllegalArgumentException {
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
    public Action getAction(String name) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Action getByUserName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Action getBySystemName(String s) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void deleteAction(Action x) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
*/    

    /** {@inheritDoc} */
    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameStringActions" : "BeanNameStringAction");
    }
    
    static volatile DefaultStringActionManager _instance = null;

    @InvokeOnGuiThread  // this method is not thread safe
    static public DefaultStringActionManager instance() {
        if (!ThreadingUtil.isGUIThread()) {
            Log4JUtil.warnOnce(log, "instance() called on wrong thread");
        }
        
        if (_instance == null) {
            _instance = new DefaultStringActionManager(
                    InstanceManager.getDefault(InternalSystemConnectionMemo.class));
        }
        return (_instance);
    }
    
    private final static Logger log = LoggerFactory.getLogger(DefaultStringActionManager.class);

}
