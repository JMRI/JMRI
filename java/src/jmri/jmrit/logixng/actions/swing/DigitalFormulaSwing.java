package jmri.jmrit.logixng.actions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.DigitalFormula;
import jmri.jmrit.logixng.util.parser.*;

/**
 * Configures an Formula object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalFormulaSwing extends AbstractDigitalActionSwing {

    private JTextField _formula;
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        DigitalFormula action = (DigitalFormula)object;
        panel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("DigitalFormula_Formula"));
        _formula = new JTextField();
        _formula.setColumns(40);
        if (action != null) _formula.setText(action.getFormula());
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
            errorMessages.add(Bundle.getMessage("DigitalFormula_InvalidFormula", _formula.getText()));
            log.error("Invalid formula '"+_formula.getText()+"'. Error: "+ex.getMessage(), ex);
            return false;
        }
        return true;
    }
    
    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        DigitalFormula action = new DigitalFormula(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof DigitalFormula)) {
            throw new IllegalArgumentException("object must be an DigitalFormula but is a: "+object.getClass().getName());
        }
        
        DigitalFormula action = (DigitalFormula)object;
        
        try {
            action.setFormula(_formula.getText());
        } catch (ParserException ex) {
            log.error("Error when parsing formula", ex);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("DigitalFormula_Short");
    }
    
    @Override
    public void dispose() {
    }
    
    
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DigitalFormulaSwing.class);
    
}
