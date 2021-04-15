package jmri.jmrit.logixng.expressions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.StringFormula;
import jmri.jmrit.logixng.util.parser.*;

/**
 * Configures an Formula object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class StringFormulaSwing extends AbstractStringExpressionSwing {

    private JTextField _formula;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        StringFormula expression = (StringFormula)object;
        panel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("StringFormula_Formula"));
        _formula = new JTextField();
        _formula.setColumns(40);
        if (expression != null) _formula.setText(expression.getFormula());
        panel.add(label);
        panel.add(_formula);
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (_formula.getText().isEmpty()) return true;
        
        try {
            Map<String, Variable> variables = new HashMap<>();
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            parser.parseExpression(_formula.getText());
        } catch (ParserException ex) {
            errorMessages.add(Bundle.getMessage("StringFormula_InvalidFormula", _formula.getText()));
            log.error("Invalid formula '"+_formula.getText()+"'. Error: "+ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        StringFormula expression = new StringFormula(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(StringExpressionManager.class).registerExpression(expression);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof StringFormula)) {
            throw new IllegalArgumentException("object must be an StringFormula but is a: "+object.getClass().getName());
        }
        
        StringFormula expression = (StringFormula)object;
        
        try {
            expression.setFormula(_formula.getText());
        } catch (ParserException ex) {
            log.error("Error when parsing formula", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("StringFormula_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(StringFormulaSwing.class);
    
}
