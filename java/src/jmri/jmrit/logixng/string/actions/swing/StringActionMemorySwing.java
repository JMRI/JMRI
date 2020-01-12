package jmri.jmrit.logixng.string.actions.swing;

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
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.string.actions.StringActionMemory;
//import jmri.jmrit.logixng.string.actions.StringActionMemory.MemoryOperation;
import jmri.util.swing.BeanSelectCreatePanel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Configures an ActionMemory object with a Swing JPanel.
 */
public class StringActionMemorySwing extends AbstractActionSwing {

    private BeanSelectCreatePanel<Memory> memoryBeanPanel;
//    private JComboBox<MemoryOperation> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
//        StringActionMemory action = (StringActionMemory)object;
        
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
        StringActionMemory action = new StringActionMemory(systemName, userName);
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
        return InstanceManager.getDefault(StringActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof StringActionMemory)) {
            throw new IllegalArgumentException("object must be an ActionMemory but is a: "+object.getClass().getName());
        }
        StringActionMemory action = (StringActionMemory)object;
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
    
    
    private final static Logger log = LoggerFactory.getLogger(StringActionMemorySwing.class);
    
}
