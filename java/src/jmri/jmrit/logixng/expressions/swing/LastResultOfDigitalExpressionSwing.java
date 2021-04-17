package jmri.jmrit.logixng.expressions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.LastResultOfDigitalExpression;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an LastResultOfDigitalExpression object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class LastResultOfDigitalExpressionSwing extends AbstractDigitalExpressionSwing {

    private JComboBox<String> _expressionsComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        LastResultOfDigitalExpression expression = (LastResultOfDigitalExpression)object;
        
        panel = new JPanel();
        
        _expressionsComboBox = new JComboBox<>();
        _expressionsComboBox.addItem("");
        for (MaleDigitalExpressionSocket bean : InstanceManager.getDefault(DigitalExpressionManager.class).getNamedBeanSet()) {
            if (bean.getUserName() != null) {
                _expressionsComboBox.addItem(bean.getUserName());
                if (expression != null) {
                    NamedBeanHandle<DigitalExpressionBean> handle = expression.getDigitalExpression();
                    if ((handle != null) && (handle.getName().equals(bean.getDisplayName()))) {
                        _expressionsComboBox.setSelectedItem(bean);
                    }
                }
            }
        }
        JComboBoxUtil.setupComboBoxMaxRows(_expressionsComboBox);
        
        panel.add(_expressionsComboBox);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        LastResultOfDigitalExpression expression = new LastResultOfDigitalExpression(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof LastResultOfDigitalExpression)) {
            throw new IllegalArgumentException("object must be an LastResultOfDigitalExpression but is a: "+object.getClass().getName());
        }
        
        LastResultOfDigitalExpression expression = (LastResultOfDigitalExpression)object;
        
        String expr = _expressionsComboBox.getItemAt(_expressionsComboBox.getSelectedIndex());
        if (expr.isEmpty()) expression.removeDigitalExpression();
        else expression.setDigitalExpression(expr);
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("LastResultOfDigitalExpression_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LastResultOfDigitalExpressionSwing.class);
    
}
