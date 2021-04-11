package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.Base;
import jmri.jmrit.logixng.MaleSocket;
import jmri.jmrit.logixng.StringExpressionManager;
import jmri.jmrit.logixng.expressions.*;

/**
 * Configures an StringExpressionConstant object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringExpressionConstantSwing extends AbstractStringExpressionSwing {

    private JTextField _constant;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        StringExpressionConstant expression = (StringExpressionConstant)object;
        panel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("StringExpressionConstant_Constant"));
        _constant = new JTextField();
        _constant.setColumns(20);
        if (expression != null) _constant.setText(expression.getValue());
        panel.add(label);
        panel.add(_constant);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringExpressionConstant expression = new StringExpressionConstant(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(StringExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof StringExpressionConstant)) {
            throw new IllegalArgumentException("object must be an StringExpressionConstant but is a: "+object.getClass().getName());
        }
        
        StringExpressionConstant expression = (StringExpressionConstant)object;
        
        if (!_constant.getText().isEmpty()) {
            expression.setValue(_constant.getText());
        } else {
            expression.setValue("");
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringExpressionConstant_Short");
    }
    
    @Override
    public void dispose() {
    }
    
}
