package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ExecuteProgram;
import jmri.jmrit.logixng.actions.ExecuteProgram.WaitOrLaunchThread;
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
    private LogixNG_SelectStringListSwing _selectEnvironmentSwing;
    private LogixNG_SelectStringSwing _selectWorkingDirectorySwing;
    private LogixNG_SelectCharsetSwing _selectCharsetSwing;

    private JTextField _outputLocalVariableTextField;
    private JTextField _errorLocalVariableTextField;
    private JTextField _exitCodeLocalVariableTextField;

    private JTabbedPane _tabbedPaneWaitOrLaunchThread;
    private JPanel _panelWait;
    private JPanel _panelLaunchThread;

    private JCheckBox _joinOutputCheckBox;
    private JRadioButton _callChildOnEveryOutputRadioButton;
    private JRadioButton _joinOutputRadioButton;
    private JTextField _processLocalVariableTextField;
    private JTextField _inputLocalVariableTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExecuteProgram action = (ExecuteProgram)object;

        _selectProgramSwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectParametersSwing = new LogixNG_SelectStringListSwing();
        _selectEnvironmentSwing = new LogixNG_SelectStringListSwing();
        _selectWorkingDirectorySwing = new LogixNG_SelectStringSwing(getJDialog(), this);
        _selectCharsetSwing = new LogixNG_SelectCharsetSwing(getJDialog(), this);

        panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());

        JPanel programPanel;
        JPanel parametersPanel;
        JPanel environmentPanel;
        JPanel workingDirectoryPanel;
        JPanel tabbedPaneCharset;
        if (action != null) {
            programPanel = _selectProgramSwing.createPanel(action.getSelectProgram());
            parametersPanel = _selectParametersSwing.createPanel(action.getSelectParameters());
            environmentPanel = _selectEnvironmentSwing.createPanel(action.getSelectEnvironment());
            workingDirectoryPanel = _selectWorkingDirectorySwing.createPanel(action.getSelectWorkingDirectory());
            tabbedPaneCharset = _selectCharsetSwing.createPanel(action.getSelectCharset());
        } else {
            programPanel = _selectProgramSwing.createPanel(null);
            parametersPanel = _selectParametersSwing.createPanel(null);
            environmentPanel = _selectEnvironmentSwing.createPanel(null);
            workingDirectoryPanel = _selectWorkingDirectorySwing.createPanel(null);
            tabbedPaneCharset = _selectCharsetSwing.createPanel(null);
        }

        _tabbedPane = new JTabbedPane();
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_Program"), programPanel);
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_Parameters"), parametersPanel);
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_Environment"), environmentPanel);
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_WorkingDirectory"), workingDirectoryPanel);
        _tabbedPane.addTab(Bundle.getMessage("ExecuteProgramSwing_CharSet"), tabbedPaneCharset);

        _outputLocalVariableTextField = new JTextField(30);
        _errorLocalVariableTextField = new JTextField(30);
        _exitCodeLocalVariableTextField = new JTextField(30);

        _tabbedPaneWaitOrLaunchThread = new JTabbedPane();

        _panelWait = new javax.swing.JPanel();
        _tabbedPaneWaitOrLaunchThread.addTab(WaitOrLaunchThread.Wait.toString(), _panelWait);
        _joinOutputCheckBox = new JCheckBox(Bundle.getMessage("ExecuteProgramSwing_JoinOutput"));
        _panelWait.add(_joinOutputCheckBox);

        _panelLaunchThread = new javax.swing.JPanel();
        _panelLaunchThread.setLayout(new java.awt.GridBagLayout());
        _tabbedPaneWaitOrLaunchThread.addTab(WaitOrLaunchThread.LaunchThread.toString(), _panelLaunchThread);
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        _panelLaunchThread.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_ProcessLocalVariable")), constraints);
        constraints.gridy = 1;
        _panelLaunchThread.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_InputLocalVariable")), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        _processLocalVariableTextField = new JTextField(30);
        _panelLaunchThread.add(_processLocalVariableTextField, constraints);
        constraints.gridy = 1;
        _inputLocalVariableTextField = new JTextField(30);
        _panelLaunchThread.add(_inputLocalVariableTextField, constraints);
        constraints.gridy = 2;
        _callChildOnEveryOutputRadioButton = new JRadioButton(Bundle.getMessage("ExecuteProgramSwing_CallChildOnEveryOutput"));
        _panelLaunchThread.add(_callChildOnEveryOutputRadioButton, constraints);
        constraints.gridy = 3;
        _joinOutputRadioButton = new JRadioButton(Bundle.getMessage("ExecuteProgramSwing_JoinOutput"));
        _panelLaunchThread.add(_joinOutputRadioButton, constraints);
        ButtonGroup bg = new ButtonGroup();
        bg.add(_callChildOnEveryOutputRadioButton);
        bg.add(_joinOutputRadioButton);

        if (action != null) {
            _outputLocalVariableTextField.setText(action.getOutputLocalVariable());
            _errorLocalVariableTextField.setText(action.getErrorLocalVariable());
            _exitCodeLocalVariableTextField.setText(action.getExitCodeLocalVariable());
            _callChildOnEveryOutputRadioButton.setSelected(action.getCallChildOnEveryOutput());
            _joinOutputCheckBox.setSelected(action.getJoinOutput());

            switch (action.getWaitOrLaunchThread()) {
                case Wait: _tabbedPaneWaitOrLaunchThread.setSelectedComponent(_panelWait); break;
                case LaunchThread: _tabbedPaneWaitOrLaunchThread.setSelectedComponent(_panelLaunchThread); break;
                default: throw new IllegalArgumentException("invalid _waitOrLaunchThread state: " + action.getWaitOrLaunchThread().name());
            }
        }

        constraints.gridwidth = 2;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(_tabbedPane, constraints);
        constraints.gridy = 1;
        panel.add(_tabbedPaneWaitOrLaunchThread, constraints);
//        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_Program")), constraints);
//        constraints.gridy = 1;
//        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_Parameters")), constraints);
//        constraints.gridy = 2;
//        JLabel selectCharsetLabel = new JLabel(Bundle.getMessage("WebRequestSwing_Charset"));
//        selectCharsetLabel.setLabelFor(tabbedPaneCharset);
//        panel.add(selectCharsetLabel, constraints);
        constraints.gridwidth = 1;
        constraints.gridy = 3;
        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_OutputLocalVariable")), constraints);
        constraints.gridy = 4;
        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_ErrorLocalVariable")), constraints);
        constraints.gridy = 5;
        panel.add(new JLabel(Bundle.getMessage("ExecuteProgramSwing_ExitCodeLocalVariable")), constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
//        panel.add(programPanel, constraints);
//        constraints.gridy = 1;
//        panel.add(parametersPanel, constraints);
//        constraints.gridy = 2;
//        panel.add(tabbedPaneCharset, constraints);
        constraints.gridy = 3;
        panel.add(_outputLocalVariableTextField, constraints);
        constraints.gridy = 4;
        panel.add(_errorLocalVariableTextField, constraints);
        constraints.gridy = 5;
        panel.add(_exitCodeLocalVariableTextField, constraints);
//        constraints.gridy = 6;
//        panel.add(_tabbedPaneWaitOrLaunchThread, constraints);
//        panel.add(_launchThreadCheckBox, constraints);
//        constraints.gridy = 6;
//        panel.add(_callChildOnEveryOutputCheckBox, constraints);
//        constraints.gridy = 7;
//        panel.add(_joinOutputCheckBox, constraints);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ExecuteProgram action = new ExecuteProgram("IQDA2", null);

        _selectProgramSwing.validate(action.getSelectProgram(), errorMessages);
        _selectParametersSwing.validate(action.getSelectParameters(), errorMessages);
        _selectEnvironmentSwing.validate(action.getSelectEnvironment(), errorMessages);
        _selectWorkingDirectorySwing.validate(action.getSelectWorkingDirectory(), errorMessages);
        _selectCharsetSwing.validate(action.getSelectCharset(), errorMessages);

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
        _selectEnvironmentSwing.updateObject(action.getSelectEnvironment());
        _selectWorkingDirectorySwing.updateObject(action.getSelectWorkingDirectory());
        _selectCharsetSwing.updateObject(action.getSelectCharset());

        action.setOutputLocalVariable(_outputLocalVariableTextField.getText());
        action.setErrorLocalVariable(_errorLocalVariableTextField.getText());
        action.setExitCodeLocalVariable(_exitCodeLocalVariableTextField.getText());

        if (_tabbedPaneWaitOrLaunchThread.getSelectedComponent() == _panelWait) {
            action.setWaitOrLaunchThread(WaitOrLaunchThread.Wait);
        } else if (_tabbedPaneWaitOrLaunchThread.getSelectedComponent() == _panelLaunchThread) {
            action.setWaitOrLaunchThread(WaitOrLaunchThread.LaunchThread);
            action.setCallChildOnEveryOutput(_callChildOnEveryOutputRadioButton.isSelected());
            action.setJoinOutput(_joinOutputCheckBox.isSelected());
        } else {
            throw new IllegalArgumentException("_tabbedPane has unknown selection");
        }


//        action.setLaunchThread(_launchThreadCheckBox.isSelected());
//        action.setCallChildOnEveryOutput(_callChildOnEveryOutputCheckBox.isSelected());
//        action.setJoinOutput(_joinOutputCheckBox.isSelected());
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
        _selectEnvironmentSwing.dispose();
        _selectWorkingDirectorySwing.dispose();
        _selectCharsetSwing.dispose();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteProgramSwing.class);

}
