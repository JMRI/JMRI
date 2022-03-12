package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionPower;
import jmri.jmrit.logixng.expressions.ExpressionPower.PowerState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionPower object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ExpressionPowerSwing extends AbstractDigitalExpressionSwing {

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    private JComboBox<PowerState> _stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionPower expression = (ExpressionPower)object;
        
        panel = new JPanel();
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);
        
        _stateComboBox = new JComboBox<>();
        for (PowerState e : PowerState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            _stateComboBox.setSelectedItem(expression.getBeanState());
        }
        
        JComponent[] components = new JComponent[]{
            _is_IsNot_ComboBox,
            _stateComboBox};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionPower_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionPower expression = new ExpressionPower(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionPower)) {
            throw new IllegalArgumentException("object must be an ExpressionPower but is a: "+object.getClass().getName());
        }
        ExpressionPower expression = (ExpressionPower)object;
        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
        expression.setBeanState((PowerState)_stateComboBox.getSelectedItem());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Power_Short");
    }
    
    @Override
    public void dispose() {
        // Do nothing
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionPowerSwing.class);
    
}
