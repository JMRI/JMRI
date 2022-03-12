package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.AnalogExpressionMemory;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an AnalogExpressionMemory object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogExpressionMemorySwing extends AbstractAnalogExpressionSwing {

    private BeanSelectPanel<Memory> memoryBeanPanel;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogExpressionMemory action = (AnalogExpressionMemory)object;
        
        panel = new JPanel();
        memoryBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        
        if (action != null) {
            if (action.getMemory() != null) {
                memoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameMemory")));
        panel.add(memoryBeanPanel);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogExpressionMemory action = new AnalogExpressionMemory(systemName, userName);
        if (!memoryBeanPanel.isEmpty()) {
            Memory memory = memoryBeanPanel.getNamedBean();
            if (memory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(memory.getDisplayName(), memory);
                action.setMemory(handle);
            }
        }
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AnalogExpressionMemory)) {
            throw new IllegalArgumentException("object must be an AnalogExpressionMemory but is a: "+object.getClass().getName());
        }
        AnalogExpressionMemory action = (AnalogExpressionMemory)object;
        Memory memory = memoryBeanPanel.getNamedBean();
        if (memory != null) {
            NamedBeanHandle<Memory> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(memory.getDisplayName(), memory);
            action.setMemory(handle);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogExpressionMemory_Short");
    }
    
    @Override
    public void dispose() {
        if (memoryBeanPanel != null) {
            memoryBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionMemorySwing.class);
    
}
