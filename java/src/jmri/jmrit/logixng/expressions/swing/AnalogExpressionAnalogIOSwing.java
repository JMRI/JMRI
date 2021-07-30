package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.NamedBeanHandleManager;
import jmri.AnalogIO;
import jmri.AnalogIOManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.AnalogExpressionAnalogIO;
import jmri.util.swing.BeanSelectPanel;

/**
 * Configures an AnalogExpressionAnalogIO object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogExpressionAnalogIOSwing extends AbstractAnalogExpressionSwing {

    private BeanSelectPanel<AnalogIO> analogIOBeanPanel;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogExpressionAnalogIO action = (AnalogExpressionAnalogIO)object;
        
        panel = new JPanel();
        analogIOBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(AnalogIOManager.class), null);
        
        if (action != null) {
            if (action.getAnalogIO() != null) {
                analogIOBeanPanel.setDefaultNamedBean(action.getAnalogIO().getBean());
            }
        }
        
        panel.add(new JLabel(Bundle.getMessage("BeanNameAnalogIO")));
        panel.add(analogIOBeanPanel);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogExpressionAnalogIO action = new AnalogExpressionAnalogIO(systemName, userName);
        if (!analogIOBeanPanel.isEmpty()) {
            AnalogIO analogIO = analogIOBeanPanel.getNamedBean();
            if (analogIO != null) {
                NamedBeanHandle<AnalogIO> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(analogIO.getDisplayName(), analogIO);
                action.setAnalogIO(handle);
            }
        }
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof AnalogExpressionAnalogIO)) {
            throw new IllegalArgumentException("object must be an AnalogExpressionAnalogIO but is a: "+object.getClass().getName());
        }
        AnalogExpressionAnalogIO action = (AnalogExpressionAnalogIO)object;
        AnalogIO analogIO = analogIOBeanPanel.getNamedBean();
        if (analogIO != null) {
            NamedBeanHandle<AnalogIO> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(analogIO.getDisplayName(), analogIO);
            action.setAnalogIO(handle);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogExpressionAnalogIO_Short");
    }
    
    @Override
    public void dispose() {
        if (analogIOBeanPanel != null) {
            analogIOBeanPanel.dispose();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogExpressionAnalogIOSwing.class);
    
}
