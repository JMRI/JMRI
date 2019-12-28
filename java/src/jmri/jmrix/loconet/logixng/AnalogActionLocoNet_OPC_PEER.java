package jmri.jmrix.loconet.logixng;

import jmri.jmrit.logixng.analog.actions.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.Locale;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.jmrit.logixng.AnalogActionManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.Category;
import jmri.jmrit.logixng.FemaleSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sends an OPC_PEER message on the LocoNet
 * 
 * @author Daniel Bergqvist Copyright 2018
 */
public class AnalogActionLocoNet_OPC_PEER extends AbstractAnalogAction
        implements VetoableChangeListener {

    private NamedBeanHandle<Memory> _memoryHandle;
    
    public AnalogActionLocoNet_OPC_PEER(String sys, String user) {
        super(sys, user);
    }
    
    public void setMemory(String memoryName) {
        if (memoryName != null) {
            Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
            if (memory != null) {
                _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class).getNamedBeanHandle(memoryName, memory);
            } else {
                log.warn("memory '{}' does not exists", memoryName);
            }
        } else {
            _memoryHandle = null;
        }
    }
    
    public void setMemory(NamedBeanHandle<Memory> handle) {
        _memoryHandle = handle;
    }
    
    public void setMemory(@CheckForNull Memory memory) {
        if (memory != null) {
            _memoryHandle = InstanceManager.getDefault(NamedBeanHandleManager.class)
                    .getNamedBeanHandle(memory.getDisplayName(), memory);
        } else {
            _memoryHandle = null;
        }
    }
    
    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }
    
    /** {@inheritDoc} */
    @Override
    public void setValue(double value) {
        if (_memoryHandle != null) {
            _memoryHandle.getBean().setValue(value);
        }
    }

    @Override
    public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getMemory().getBean())) {
                    setMemory((Memory)null);
                }
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public FemaleSocket getChild(int index)
            throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String getShortDescription(Locale locale) {
        if (_memoryHandle != null) {
            return Bundle.getMessage(locale, "AnalogActionLocoNet_OPC_PEER_Short", _memoryHandle.getBean().getDisplayName());
        } else {
            return Bundle.getMessage(locale, "AnalogActionLocoNet_OPC_PEER_Short", "none");
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getLongDescription(Locale locale) {
        return getShortDescription(locale);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }
    
    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }
    
    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }
    
    private final static Logger log = LoggerFactory.getLogger(AnalogActionLocoNet_OPC_PEER.class);

}
