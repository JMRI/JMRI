package jmri.jmrit.logixng.implementation;

import static jmri.NamedBean.UNKNOWN;

import java.io.PrintWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.implementation.JmriSimplePropertyListener;
import jmri.implementation.AbstractNamedBean;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.FemaleSocket;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.MaleDigitalActionSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default implementation of LogixNG.
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class DefaultLogixNG extends AbstractNamedBean
        implements LogixNG {
//        implements LogixNG, FemaleSocketListener {
    
//    private DefaultLogixNG _template;
    private Base _parent = null;
//    private String _socketSystemName = null;
//    private final FemaleDigitalActionSocket _femaleActionSocket;
    private boolean _enabled = false;
//    private boolean _userEnabled = false;
    
    public DefaultLogixNG(String sys) throws BadUserNameException, BadSystemNameException  {
        super(sys);
//        _femaleActionSocket = InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(this, this, "");
    }
    
    public DefaultLogixNG(String sys, String user) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);
//        _femaleActionSocket = InstanceManager.getDefault(DigitalActionManager.class).createFemaleSocket(this, this, "");
    }
    
    public DefaultLogixNG(String sys, MaleDigitalActionSocket action) throws BadUserNameException, BadSystemNameException  {
        super(sys);
//        _femaleActionSocket = InstanceManager.getDefault(DigitalActionManager.class).createFemaleActionSocket(this, this, "", action);
    }
    
    public DefaultLogixNG(String sys, String user, MaleDigitalActionSocket action) throws BadUserNameException, BadSystemNameException  {
        super(sys, user);
//        _femaleActionSocket = InstanceManager.getDefault(DigitalActionManager.class).createFemaleActionSocket(this, this, "", action);
    }
    
    private DefaultLogixNG(DefaultLogixNG template) {
        super(InstanceManager.getDefault(LogixNG_Manager.class).getNewSystemName(), null);
//        _template = template;
//        _femaleActionSocket = InstanceManager.getDefault(DigitalActionManager.class)
//                .createFemaleSocket(this, this, _template._femaleActionSocket.getName());
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getNewObjectBasedOnTemplate() {
        return new DefaultLogixNG(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public Base getParent() {
        return _parent;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setParent(Base parent) {
        _parent = parent;
    }
    
    @Override
    public String getBeanType() {
        return Bundle.getMessage("BeanNameLogixNG");
    }

    @Override
    public void setState(int s) throws JmriException {
        log.warn("Unexpected call to setState in DefaultLogixNG.");  // NOI18N
    }

    @Override
    public int getState() {
        log.warn("Unexpected call to getState in DefaultLogixNG.");  // NOI18N
        return UNKNOWN;
    }

    /*.*
     * Set enabled status. Enabled is a bound property All conditionals are set
     * to UNKNOWN state and recalculated when the Logix is enabled, provided the
     * Logix has been previously activated.
     *./
    @Override
    public void setEnabled(boolean state) {

        boolean old = _enabled;
        _enabled = state;
        if (old != state) {
/*            
            boolean active = _isActivated;
            deActivateLogix();
            activateLogix();
            _isActivated = active;
            for (int i = _listeners.size() - 1; i >= 0; i--) {
                _listeners.get(i).setEnabled(state);
            }
            firePropertyChange("Enabled", Boolean.valueOf(old), Boolean.valueOf(state));  // NOI18N
*/            
//        }
//    }

    /*.*
     * Get enabled status
     */
//    @Override
//    public boolean getEnabled() {
//        return _enabled;
//    }

    private final static Logger log = LoggerFactory.getLogger(DefaultLogixNG.class);
/*
    @Override
    public void connected(FemaleSocket socket) {
        // Do nothing
    }

    @Override
    public void disconnected(FemaleSocket socket) {
        // Do nothing
    }
*/
    @Override
    public String getShortDescription(Locale locale) {
        return "LogixNG";
    }

    @Override
    public String getLongDescription(Locale locale) {
        return "LogixNG: "+getDisplayName();
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        throw new UnsupportedOperationException("Not supported.");
//        return 1;
    }

    @Override
    public Category getCategory() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public boolean isExternal() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public Lock getLock() {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public void setLock(Lock lock) {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    final public void setup() {
        log.debug("LogixNG: setup()");
        for (ConditionalNG c : _conditionalNG_List) {
            c.setup();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setEnabled(boolean enable) {
        _enabled = enable;
        if (enable) {
            registerListeners();
        } else {
            unregisterListeners();
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isEnabled() {
        return _enabled;
//        return _enabled && _userEnabled;
    }
    
    /*.*
     * Set whenether this object is enabled or disabled by the user.
     * 
     * @param enable true if this object should be enabled, false otherwise
     *./
    public void setUserEnabled(boolean enable) {
        _userEnabled = enable;
    }
*/    
    /*.*
     * Determines whether this object is enabled by the user.
     * 
     * @return true if the object is enabled, false otherwise
     *./
    public boolean isUserEnabled() {
        return _userEnabled;
    }
*/    
    
    /**
     * Persistant instance variables (saved between runs)
     */
    List<ConditionalNG> _conditionalNG_List = new ArrayList<>();
    ArrayList<JmriSimplePropertyListener> _listeners = new ArrayList<>();
    
    /**
     * Maintain a list of conditional objects.  The key is the conditional system name
     */
    HashMap<String, ConditionalNG> _conditionalNGMap = new HashMap<>();
    
    /**
     * Operational instance variables (not saved between runs)
     */
    private boolean _isActivated = false;
    
    /** {@inheritDoc} */
    @Override
    public int getNumConditionalNGs() {
        return _conditionalNG_List.size();
    }

    /** {@inheritDoc} */
    @Override
    public void swapConditionalNG(int nextInOrder, int row) {
        if (row <= nextInOrder) {
            return;
        }
        ConditionalNG temp = _conditionalNG_List.get(row);
        for (int i = row; i > nextInOrder; i--) {
            _conditionalNG_List.set(i, _conditionalNG_List.get(i - 1));
        }
        _conditionalNG_List.set(nextInOrder, temp);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG(int order) {
        try {
            return _conditionalNG_List.get(order);
        } catch (java.lang.IndexOutOfBoundsException ioob) {
            return null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean addConditionalNG(ConditionalNG conditionalNG) {
        ConditionalNG chkDuplicate = _conditionalNGMap.putIfAbsent(conditionalNG.getSystemName(), conditionalNG);
        if (chkDuplicate == null) {
            _conditionalNG_List.add(conditionalNG);
            conditionalNG.setParent(this);
            return (true);
        }
        log.error("ConditionalNG '{}' has already been added to LogixNG '{}'", conditionalNG.getSystemName(), getSystemName());  // NOI18N
        return (false);
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG(String systemName) {
        return _conditionalNGMap.get(systemName);
    }
    
    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNGByUserName(String userName) {
        for (ConditionalNG conditionalNG : _conditionalNGMap.values()) {
            if (userName.equals(conditionalNG.getUserName())) {
                return conditionalNG;
            }
        }
        return null;
    }
    
    /** {@inheritDoc} */
    @Override
    public void deleteConditionalNG(ConditionalNG conditionalNG) {
        if (_conditionalNG_List.size() <= 0) {
            return;
        }

        // Remove Conditional from this logix
        if (!_conditionalNG_List.remove(conditionalNG)) {
            log.error("attempt to delete ConditionalNG not in LogixNG: {}", conditionalNG.getSystemName());  // NOI18N
            return;
        }
        _conditionalNGMap.remove(conditionalNG.getSystemName());
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean isActive() {
        return _isActivated;
    }
    
    /** {@inheritDoc} */
    @Override
    public void activateLogixNG() {
        // if the Logix is already busy, simply return
        if (_isActivated) {
            return;
        }
        
        _isActivated = true;
        
        if (_enabled) {
            for (ConditionalNG conditionalNG : _conditionalNGMap.values()) {
                if (conditionalNG.isEnabled()) {
                    conditionalNG.registerListeners();
                }
            }
            
            for (ConditionalNG conditionalNG : _conditionalNGMap.values()) {
                if (conditionalNG.isEnabled()) {
                    if (conditionalNG.supportsEnableExecution()) {
                        if (conditionalNG.isEnabled() && conditionalNG.isExecutionEnabled()) {
                            conditionalNG.execute();
                        }
                    } else {
                        // The conditionalNG doesn't support enableExecution
                        // so we don't test if execution is enabled.
                        conditionalNG.execute();
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void deActivateLogixNG() {
        if (_isActivated) {
            // Logix is active, deactivate it and all listeners
            _isActivated = false;
            // remove listeners if there are any
            for (ConditionalNG conditionalNG : _conditionalNGMap.values()) {
                conditionalNG.unregisterListeners();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void calculateConditionalNGs() {
        for (ConditionalNG c : _conditionalNG_List) {
            c.execute();
        }
    }

    /** {@inheritDoc} */
    @Override
    public ConditionalNG getConditionalNG() {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG getLogixNG() {
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public final Base getRoot() {
        Base current = this;
        Base parent = getParent();
        while (parent != null) {
            current = parent;
            parent = parent.getParent();
        }
        return current;
    }

    /** {@inheritDoc} */
    @Override
    public void setParentForAllChildren() {
        for (ConditionalNG c : _conditionalNG_List) {
            c.setParent(this);
            c.setParentForAllChildren();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void registerListeners() {
        for (ConditionalNG c : _conditionalNG_List) {
            c.registerListeners();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListeners() {
        for (ConditionalNG c : _conditionalNG_List) {
            c.unregisterListeners();
        }
    }
    
    protected void printTreeRow(Locale locale, PrintWriter writer, String currentIndent) {
        writer.append(currentIndent);
        writer.append(getLongDescription(locale));
        writer.println();
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(PrintWriter writer, String indent) {
        printTree(Locale.getDefault(), writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent) {
        printTree(locale, writer, indent, "");
    }
    
    /** {@inheritDoc} */
    @Override
    public void printTree(Locale locale, PrintWriter writer, String indent, String currentIndent) {
        printTreeRow(locale, writer, currentIndent);

        for (int i=0; i < this.getNumConditionalNGs(); i++) {
            getConditionalNG(i).printTree(locale, writer, indent, currentIndent+indent);
        }
    }
    
}
