package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.StringIO;
import jmri.StringIOManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.StringActionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.actions.StringActionStringIO;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an StringActionStringIO object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringActionStringIOSwing extends AbstractStringActionSwing {

    private BeanSelectPanel<StringIO> _stringIOBeanPanel;
//    private JComboBox<StringIOOperation> stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
//        StringActionStringIO action = (StringActionStringIO)object;
        
        panel = new JPanel();
        _stringIOBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(StringIOManager.class), null);
/*        
        stateComboBox = new JComboBox<>();
        for (StringIOOperation e : StringIOOperation.values()) {
            stateComboBox.addItem(e);
        }
        
        if (action != null) {
            if (action.getStringIO() != null) {
                memoryBeanPanel.setDefaultNamedBean(action.getStringIO().getBean());
            }
            stateComboBox.setSelectedItem(action.getStringIOOperation());
        }
*/        
        panel.add(new JLabel(Bundle.getMessage("BeanNameStringIO")));
        panel.add(_stringIOBeanPanel);
//        panel.add(stateComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringActionStringIO action = new StringActionStringIO(systemName, userName);
        if (!_stringIOBeanPanel.isEmpty()) {
            StringIO memory = _stringIOBeanPanel.getNamedBean();
            if (memory != null) {
                NamedBeanHandle<StringIO> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(memory.getDisplayName(), memory);
                action.setStringIO(handle);
            }
        }
        return InstanceManager.getDefault(StringActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof StringActionStringIO)) {
            throw new IllegalArgumentException("object must be an StringActionStringIO but is a: "+object.getClass().getName());
        }
        StringActionStringIO action = (StringActionStringIO)object;
        StringIO memory = _stringIOBeanPanel.getNamedBean();
        if (memory != null) {
            NamedBeanHandle<StringIO> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(memory.getDisplayName(), memory);
            action.setStringIO(handle);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringActionStringIO_Short");
    }
    
    @Override
    public void dispose() {
        if (_stringIOBeanPanel != null) {
            _stringIOBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringActionStringIOSwing.class);
    
}
