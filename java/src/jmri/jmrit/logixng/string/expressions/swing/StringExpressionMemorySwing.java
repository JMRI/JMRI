package jmri.jmrit.logixng.string.expressions.swing;

import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.Memory;
import jmri.MemoryManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.string.expressions.StringExpressionMemory;
//import jmri.jmrit.logixng.string.expressions.StringExpressionMemory.MemoryOperation;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an StringExpressionMemory object with a Swing JPanel.
 */
public class StringExpressionMemorySwing extends AbstractExpressionSwing {

    private BeanSelectCreatePanel<Memory> memoryBeanPanel;
//    private JComboBox<MemoryOperation> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
//        StringExpressionMemory action = (StringExpressionMemory)object;
        
        panel = new JPanel();
        memoryBeanPanel = new BeanSelectCreatePanel<>(InstanceManager.getDefault(MemoryManager.class), null);
/*        
        stateComboBox = new JComboBox<>();
        for (MemoryOperation e : MemoryOperation.values()) {
            stateComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getMemory() != null) {
                memoryBeanPanel.setDefaultNamedBean(action.getMemory().getBean());
            }
            stateComboBox.setSelectedItem(action.getMemoryOperation());
        }
*/        
        panel.add(new JLabel(Bundle.getMessage("BeanNameMemory")));
        panel.add(memoryBeanPanel);
//        panel.add(stateComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (1==0) {
            errorMessages.add("An error");
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringExpressionMemory action = new StringExpressionMemory(systemName, userName);
        try {
            if (!memoryBeanPanel.isEmpty()) {
                Memory memory = memoryBeanPanel.getNamedBean();
                if (memory != null) {
                    NamedBeanHandle<Memory> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(memory.getDisplayName(), memory);
                    action.setMemory(handle);
                }
            }
//            action.setMemoryOperation((MemoryOperation)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);
        }
        return InstanceManager.getDefault(StringExpressionManager.class).registerExpression(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof StringExpressionMemory)) {
            throw new IllegalArgumentException("object must be an StringExpressionMemory but is a: "+object.getClass().getName());
        }
        StringExpressionMemory action = (StringExpressionMemory)object;
        try {
            Memory memory = memoryBeanPanel.getNamedBean();
            if (memory != null) {
                NamedBeanHandle<Memory> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(memory.getDisplayName(), memory);
                action.setMemory(handle);
            }
//            action.setMemoryOperation((MemoryOperation)stateComboBox.getSelectedItem());
        } catch (JmriException ex) {
            log.error("Cannot get NamedBeanHandle for memory", ex);
        }
    }
    
    
    /**
     * Create Memory object for the action
     *
     * @param reference Memory application description
     * @return The new output as Memory object
     */
    protected Memory getMemoryFromPanel(String reference) {
        if (memoryBeanPanel == null) {
            return null;
        }
        memoryBeanPanel.setReference(reference); // pass memory application description to be put into memory Comment
        try {
            return memoryBeanPanel.getNamedBean();
        } catch (jmri.JmriException ex) {
            log.warn("skipping creation of memory not found for " + reference);
            return null;
        }
    }
    
//    private void noMemoryMessage(String s1, String s2) {
//        log.warn("Could not provide memory " + s2);
//        String msg = Bundle.getMessage("WarningNoMemory", new Object[]{s1, s2});
//        JOptionPane.showMessageDialog(editFrame, msg,
//                Bundle.getMessage("WarningTitle"), JOptionPane.ERROR_MESSAGE);
//    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Memory_Short");
    }
    
    @Override
    public void dispose() {
        if (memoryBeanPanel != null) {
            memoryBeanPanel.dispose();
        }
    }
    
    
    private final static Logger log = LoggerFactory.getLogger(StringExpressionMemorySwing.class);
    
}
