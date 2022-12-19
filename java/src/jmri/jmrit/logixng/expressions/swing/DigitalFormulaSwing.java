package jmri.jmrit.logixng.expressions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.DigitalFormula;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.FormulaPanel;

/**
 * Configures an Formula object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalFormulaSwing extends AbstractDigitalExpressionSwing {

    private final FormulaPanel _formulaPanel = new FormulaPanel();

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        DigitalFormula expression = (DigitalFormula)object;
        panel = _formulaPanel.createPanel(expression != null ? expression.getFormula() : "", buttonPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return _formulaPanel.validate(errorMessages);
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        DigitalFormula expression = new DigitalFormula(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof DigitalFormula)) {
            throw new IllegalArgumentException("object must be an DigitalFormula but is a: "+object.getClass().getName());
        }

        DigitalFormula expression = (DigitalFormula)object;

        try {
            expression.setFormula(_formulaPanel.getFormula());
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
