package jmri.jmrit.logixng.expressions.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.AnalogFormula;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.swing.FormulaPanel;

/**
 * Configures an ActionTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class AnalogFormulaSwing extends AbstractAnalogExpressionSwing {

    private final FormulaPanel _formulaPanel = new FormulaPanel();

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        AnalogFormula expression = (AnalogFormula)object;
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
        AnalogFormula expression = new AnalogFormula(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(AnalogExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof AnalogFormula)) {
            throw new IllegalArgumentException("object must be an AnalogFormula but is a: "+object.getClass().getName());
        }

        AnalogFormula expression = (AnalogFormula)object;

        try {
            expression.setFormula(_formulaPanel.getFormula());
        } catch (ParserException ex) {
            log.error("Error when parsing formula", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("AnalogFormula_Short");
    }

    @Override
    public void dispose() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnalogFormulaSwing.class);

}
