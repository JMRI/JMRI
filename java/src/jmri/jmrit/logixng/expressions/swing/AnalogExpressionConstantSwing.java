package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.AnalogExpressionManager;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.expressions.AnalogExpressionConstant;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogExpressionConstantSwing extends AbstractAnalogExpressionSwing {

    private JTextField _constant;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogExpressionConstant expression = (AnalogExpressionConstant)object;
        panel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("AnalogExpressionConstant_Constant"));
        _constant = new JTextField();
        _constant.setColumns(10);
        if (expression != null) _constant.setText(String.format("%1.2f", expression.getValue()));
        panel.add(label);
        panel.add(_constant);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (_constant.getText().isEmpty()) return true;
        
        try {
            Double.parseDouble(_constant.getText());
        } catch (NumberFormatException e) {
            errorMessages.add(Bundle.getMessage("AnalogExpressionConstant_NotANumber", _constant.getText()));
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        AnalogExpressionConstant expression = new AnalogExpressionConstant(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof AnalogExpressionConstant)) {
            throw new IllegalArgumentException("object must be an AnalogExpressionConstant but is a: "+object.getClass().getName());
        }
        
        AnalogExpressionConstant expression = (AnalogExpressionConstant)object;
        
        if (!_constant.getText().isEmpty()) {
            expression.setValue(Double.parseDouble(_constant.getText()));
        } else {
            expression.setValue(0.0);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogExpressionConstant_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
