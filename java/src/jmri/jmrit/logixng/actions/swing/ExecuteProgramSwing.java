package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ExecuteProgram;
import jmri.jmrit.logixng.util.swing.*;

/**
 * Configures an ExecuteProgram object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class ExecuteProgramSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedPane;

    private LogixNG_SelectStringSwing _selectProgramSwing;
    private LogixNG_SelectStringListSwing _selectParametersSwing;
    private LogixNG_SelectStringSwing _selectWorkingDirectorySwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExecuteProgram action = (ExecuteProgram)object;

        _selectProgramSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectParametersSwing = new LogixNG_SelectStringListSwing();
        _selectWorkingDirectorySwing = new LogixNG_SelectStringSwing(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());

        JPanel programPanel;
        JPanel parametersPanel;
        JPanel workingDirectoryPanel;
        if (action != null) {
            programPanel = _selectProgramSwing.createPanel(action.getSelectProgram());
            parametersPanel = _selectParametersSwing.createPanel(action.getSelectParameters());
            workingDirectoryPanel = _selectWorkingDirectorySwing.createPanel(action.getSelectWorkingDirectory());
        } else {
            programPanel = _selectProgramSwing.createPanel(null);
            parametersPanel = _selectParametersSwing.createPanel(null);
            workingDirectoryPanel = _selectWorkingDirectorySwing.createPanel(null);
        }

        _tabbedPane = new JTabbedPane();
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_Program"), programPanel);
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_Parameters"), parametersPanel);
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_WorkingDirectory"), workingDirectoryPanel);

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(_tabbedPane, constraints);
        constraints.gridy = 1;
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ExecuteProgram action = new ExecuteProgram("IQDA2", null);

        _selectProgramSwing.validate(action.getSelectProgram(), errorMessages);
        _selectParametersSwing.validate(action.getSelectParameters(), errorMessages);
        _selectWorkingDirectorySwing.validate(action.getSelectWorkingDirectory(), errorMessages);

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
        _selectWorkingDirectorySwing.updateObject(action.getSelectWorkingDirectory());
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
        _selectWorkingDirectorySwing.dispose();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgramSwing.class);

}
