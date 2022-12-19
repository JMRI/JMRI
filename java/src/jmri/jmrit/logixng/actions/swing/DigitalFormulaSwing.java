package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.DigitalFormula;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.FormulaPanel;

/**
 * Configures an Formula object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class DigitalFormulaSwing extends AbstractDigitalActionSwing {

    private final FormulaPanel _formulaPanel = new FormulaPanel();

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        DigitalFormula action = (DigitalFormula)object;
        panel = _formulaPanel.createPanel(action != null ? action.getFormula() : "", buttonPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return _formulaPanel.validate(errorMessages);
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
            action.setFormula(_formulaPanel.getFormula());
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
