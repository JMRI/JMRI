package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionReference;
import jmri.jmrit.logixng.expressions.ExpressionReference.PointsTo;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionReference object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionReferenceSwing extends AbstractDigitalExpressionSwing {

    private JTextField _sensorReferenceTextField;
    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;
    private JComboBox<PointsTo> _stateComboBox;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionReference expression = (ExpressionReference)object;
        
        panel = new JPanel();
        
        _sensorReferenceTextField = new JTextField(30);
        
        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);
        
        _stateComboBox = new JComboBox<>();
        for (PointsTo e : PointsTo.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);
        
        if (expression != null) {
            _sensorReferenceTextField.setText(expression.getReference());
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());
            _stateComboBox.setSelectedItem(expression.getPointsTo());
        }
        
        JComponent[] components = new JComponent[]{
            _sensorReferenceTextField,
            _is_IsNot_ComboBox,
            _stateComboBox};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionReference_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionReference expression = new ExpressionReference("IQDE1", null);
        
        try {
            expression.setReference(_sensorReferenceTextField.getText());
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
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
        ExpressionReference expression = new ExpressionReference(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionReference)) {
            throw new IllegalArgumentException("object must be an ExpressionReference but is a: "+object.getClass().getName());
        }
        ExpressionReference expression = (ExpressionReference)object;
        expression.setReference(_sensorReferenceTextField.getText());
        expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());
        expression.setPointsTo((PointsTo)_stateComboBox.getSelectedItem());
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Reference_Short");
    }
    
    @Override
    public void dispose() {
        // Do nothing
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionReferenceSwing.class);
    
}
