package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ExecuteProgram;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectStringSwing;

/**
 * Configures an ExecuteProgram object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ExecuteProgramSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectStringSwing _selectProgramSwing;
    private LogixNG_SelectStringSwing _selectParametersSwing;

    private JTextField _resultLocalVariableTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExecuteProgram action = (ExecuteProgram)object;

        _selectProgramSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectParametersSwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());

        JPanel _programPanel;
        JPanel _parametersPanel;
        if (action != null) {
            _programPanel = _selectProgramSwing.createPanel(action.getSelectProgram());
            _parametersPanel = _selectParametersSwing.createPanel(action.getSelectParameters());
        } else {
            _programPanel = _selectProgramSwing.createPanel(null);
            _parametersPanel = _selectParametersSwing.createPanel(null);
        }

        _resultLocalVariableTextField = new JTextField(30);

        if (action != null) {
            _resultLocalVariableTextField.setText(action.getResultLocalVariable());
        }

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_Program")), constraints);
        constraints.gridy = 1;
        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_Parameters")), constraints);
        constraints.gridy = 2;
        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_ResultLocalVariable")), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        panel.add(_programPanel, constraints);
        constraints.gridy = 1;
        panel.add(_parametersPanel, constraints);
        constraints.gridy = 2;
        panel.add(_resultLocalVariableTextField, constraints);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ExecuteProgram action = new ExecuteProgram("IQDA2", null);

        _selectProgramSwing.validate(action.getSelectProgram(), errorMessages);
        _selectParametersSwing.validate(action.getSelectParameters(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExecuteProgram action = new ExecuteProgram(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExecuteProgram)) {
            throw new IllegalArgumentException("object must be an ExecuteProgram but is a: "+object.getClass().getName());
        }
        ExecuteProgram action = (ExecuteProgram)object;

        _selectProgramSwing.updateObject(action.getSelectProgram());
        _selectParametersSwing.updateObject(action.getSelectParameters());

        action.setResultLocalVariable(_resultLocalVariableTextField.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ExecuteProgram_Short");
    }

    @Override
    public void dispose() {
        _selectProgramSwing.dispose();
        _selectParametersSwing.dispose();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgramSwing.class);

}
