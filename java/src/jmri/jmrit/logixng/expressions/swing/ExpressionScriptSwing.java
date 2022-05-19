package jmri.jmrit.logixng.expressions.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionScript;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.script.ScriptEngineSelector;
import jmri.script.swing.ScriptEngineSelectorSwing;
import jmri.script.swing.ScriptFileChooser;
import jmri.util.FileUtil;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionScript object with a Swing JPanel.
 *
 * @author Daniel Bergqvist 2021
 */
public class ExpressionScriptSwing extends AbstractDigitalExpressionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private JTabbedPane _tabbedPaneOperationType;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;

    private JComboBox<ExpressionScript.OperationType> _operationComboBox;
    private JTextField _scriptOperationReferenceTextField;
    private JTextField _scriptOperationLocalVariableTextField;
    private JTextField _scriptOperationFormulaTextField;

    private JTabbedPane _tabbedPaneScriptType;
    private JPanel _panelScriptTypeDirect;
    private JPanel _panelScriptTypeReference;
    private JPanel _panelScriptTypeLocalVariable;
    private JPanel _panelScriptTypeFormula;

    private ScriptFileChooser scriptFileChooser;
    private JTextField _scriptTextField;
    private JTextField _scriptReferenceTextField;
    private JTextField _scriptLocalVariableTextField;
    private JTextField _scriptFormulaTextField;

    private JLabel _registerListenerLabel;
    private JLabel _unregisterListenerLabel;
    private JTextField _registerListener;
    private JTextField _unregisterListener;

    private ScriptEngineSelectorSwing _scriptEngineSelectorSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionScript expression = (ExpressionScript)object;

        if (expression != null) {
            _scriptEngineSelectorSwing = new ScriptEngineSelectorSwing(expression.getScriptEngineSelector());
        } else {
            _scriptEngineSelectorSwing = new ScriptEngineSelectorSwing(new ScriptEngineSelector());
        }

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel expressionPanel = new JPanel();


        // Set up the tabbed pane for selecting the operation
        _tabbedPaneOperationType = new JTabbedPane();
        _panelOperationTypeDirect = new javax.swing.JPanel();
        _panelOperationTypeDirect.setLayout(new BoxLayout(_panelOperationTypeDirect, BoxLayout.Y_AXIS));
        _panelOperationTypeReference = new javax.swing.JPanel();
        _panelOperationTypeReference.setLayout(new BoxLayout(_panelOperationTypeReference, BoxLayout.Y_AXIS));
        _panelOperationTypeLocalVariable = new javax.swing.JPanel();
        _panelOperationTypeLocalVariable.setLayout(new BoxLayout(_panelOperationTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelOperationTypeFormula = new javax.swing.JPanel();
        _panelOperationTypeFormula.setLayout(new BoxLayout(_panelOperationTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Direct.toString(), _panelOperationTypeDirect);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Reference.toString(), _panelOperationTypeReference);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelOperationTypeLocalVariable);
        _tabbedPaneOperationType.addTab(NamedBeanAddressing.Formula.toString(), _panelOperationTypeFormula);

        _operationComboBox = new JComboBox<>();
        for (ExpressionScript.OperationType e : ExpressionScript.OperationType.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);
        _panelOperationTypeDirect.add(new JLabel(Bundle.getMessage("ExpressionScript_Operation")));
        _panelOperationTypeDirect.add(_operationComboBox);

        _scriptOperationReferenceTextField = new JTextField();
        _scriptOperationReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeReference.add(new JLabel(Bundle.getMessage("ExpressionScript_Operation")));
        _panelOperationTypeReference.add(_scriptOperationReferenceTextField);

        _scriptOperationLocalVariableTextField = new JTextField();
        _scriptOperationLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeLocalVariable.add(new JLabel(Bundle.getMessage("ExpressionScript_Operation")));
        _panelOperationTypeLocalVariable.add(_scriptOperationLocalVariableTextField);

        _scriptOperationFormulaTextField = new JTextField();
        _scriptOperationFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeFormula.add(new JLabel(Bundle.getMessage("ExpressionScript_Operation")));
        _panelOperationTypeFormula.add(_scriptOperationFormulaTextField);


        // Set up the tabbed pane for selecting the appearance
        _tabbedPaneScriptType = new JTabbedPane();
        _panelScriptTypeDirect = new javax.swing.JPanel();
//        _panelScriptTypeDirect.setLayout(new BoxLayout(_panelScriptTypeDirect, BoxLayout.Y_AXIS));
        _panelScriptTypeReference = new javax.swing.JPanel();
        _panelScriptTypeReference.setLayout(new BoxLayout(_panelScriptTypeReference, BoxLayout.Y_AXIS));
        _panelScriptTypeLocalVariable = new javax.swing.JPanel();
        _panelScriptTypeLocalVariable.setLayout(new BoxLayout(_panelScriptTypeLocalVariable, BoxLayout.Y_AXIS));
        _panelScriptTypeFormula = new javax.swing.JPanel();
        _panelScriptTypeFormula.setLayout(new BoxLayout(_panelScriptTypeFormula, BoxLayout.Y_AXIS));

        _tabbedPaneScriptType.addTab(NamedBeanAddressing.Direct.toString(), _panelScriptTypeDirect);
        _tabbedPaneScriptType.addTab(NamedBeanAddressing.Reference.toString(), _panelScriptTypeReference);
        _tabbedPaneScriptType.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelScriptTypeLocalVariable);
        _tabbedPaneScriptType.addTab(NamedBeanAddressing.Formula.toString(), _panelScriptTypeFormula);

        JButton _expressionSelectFileButton = new JButton("..."); // "File" replaced by ...
        _expressionSelectFileButton.setMaximumSize(_expressionSelectFileButton.getPreferredSize());
        _expressionSelectFileButton.setToolTipText(Bundle.getMessage("FileButtonHint"));  // NOI18N
        _expressionSelectFileButton.addActionListener((ActionEvent e) -> {
            scriptFileChooser = new ScriptFileChooser(FileUtil.getScriptsPath());
            scriptFileChooser.rescanCurrentDirectory();
            int retVal = scriptFileChooser.showOpenDialog(null);
            // handle selection or cancel
            if (retVal == JFileChooser.APPROVE_OPTION) {
                // set selected file location
                try {
                    _scriptTextField.setText(FileUtil.getPortableFilename(scriptFileChooser.getSelectedFile().getCanonicalPath()));
                } catch (java.io.IOException ex) {
                    log.error("exception setting file location", ex);  // NOI18N
                    _scriptTextField.setText("");
                }
            }
        });
        _panelScriptTypeDirect.add(_expressionSelectFileButton);
        JPanel _scriptTextPanel = new JPanel();
        _scriptTextPanel.setLayout(new BoxLayout(_scriptTextPanel, BoxLayout.Y_AXIS));
        _scriptTextField = new JTextField(30);
        _scriptTextPanel.add(new JLabel(Bundle.getMessage("ExpressionScript_Script")));
        _scriptTextPanel.add(_scriptTextField);
        _panelScriptTypeDirect.add(_scriptTextPanel);

        _scriptReferenceTextField = new JTextField();
        _scriptReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelScriptTypeReference.add(new JLabel(Bundle.getMessage("ExpressionScript_Script")));
        _panelScriptTypeReference.add(_scriptReferenceTextField);

        _scriptLocalVariableTextField = new JTextField();
        _scriptLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelScriptTypeLocalVariable.add(new JLabel(Bundle.getMessage("ExpressionScript_Script")));
        _panelScriptTypeLocalVariable.add(_scriptLocalVariableTextField);

        _scriptFormulaTextField = new JTextField();
        _scriptFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelScriptTypeFormula.add(new JLabel(Bundle.getMessage("ExpressionScript_Script")));
        _panelScriptTypeFormula.add(_scriptFormulaTextField);


        if (expression != null) {
            switch (expression.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getOperationAddressing().name());
            }
            _operationComboBox.setSelectedItem(expression.getOperationType());
            _scriptOperationReferenceTextField.setText(expression.getOperationReference());
            _scriptOperationLocalVariableTextField.setText(expression.getOperationLocalVariable());
            _scriptOperationFormulaTextField.setText(expression.getOperationFormula());

            switch (expression.getScriptAddressing()) {
                case Direct: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeDirect); break;
                case Reference: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeReference); break;
                case LocalVariable: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeLocalVariable); break;
                case Formula: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getScriptAddressing().name());
            }
            _scriptTextField.setText(expression.getScript());
            _scriptReferenceTextField.setText(expression.getScriptReference());
            _scriptLocalVariableTextField.setText(expression.getScriptLocalVariable());
            _scriptFormulaTextField.setText(expression.getScriptFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneOperationType,
            _tabbedPaneScriptType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionScript_Components"), components);

        for (JComponent c : componentList) expressionPanel.add(c);
        panel.add(expressionPanel);

        JPanel listernerPanel = new JPanel();
        panel.add(listernerPanel);

        _registerListenerLabel = new JLabel(Bundle.getMessage("ExpressionScript_RegisterListener"));
        _unregisterListenerLabel = new JLabel(Bundle.getMessage("ExpressionScript_UnregisterListener"));
        _registerListener = new JTextField(30);
        _unregisterListener = new JTextField(30);

        if (expression != null) {
            _registerListener.setText(expression.getRegisterListenerScript());
            _unregisterListener.setText(expression.getUnregisterListenerScript());
        }

        listernerPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridwidth = 1;
        c.gridheight = 1;
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.EAST;
        listernerPanel.add(_registerListenerLabel, c);
        _registerListenerLabel.setLabelFor(_registerListener);
        c.gridy = 1;
        listernerPanel.add(_unregisterListenerLabel, c);
        _unregisterListenerLabel.setLabelFor(_unregisterListener);
        c.gridx = 1;
        c.gridy = 0;
        c.anchor = GridBagConstraints.WEST;
        listernerPanel.add(_registerListener, c);
//        _registerListener.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));
        c.gridy = 1;
        listernerPanel.add(_unregisterListener, c);
//        _unregisterListener.setToolTipText(Bundle.getMessage("SysNameToolTip", "Y"));

        JPanel scriptSelectorPanel = new JPanel();
        scriptSelectorPanel.add(new JLabel(Bundle.getMessage("ExpressionScript_ScriptSelector")));
        scriptSelectorPanel.add(_scriptEngineSelectorSwing.getComboBox());

        panel.add(scriptSelectorPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionScript expression = new ExpressionScript("IQDE1", null);

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                expression.setOperationReference(_scriptOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            expression.setScriptFormula(_scriptFormulaTextField.getText());
            if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeDirect) {
                expression.setScriptAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeReference) {
                expression.setScriptAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeLocalVariable) {
                expression.setScriptAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeFormula) {
                expression.setScriptAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneScriptType has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionScript expression = new ExpressionScript(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionScript)) {
            throw new IllegalArgumentException("object must be an ExpressionScript but is a: "+object.getClass().getName());
        }
        ExpressionScript expression = (ExpressionScript)object;

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                expression.setOperationAddressing(NamedBeanAddressing.Direct);
                expression.setOperationType((ExpressionScript.OperationType)_operationComboBox.getSelectedItem());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                expression.setOperationAddressing(NamedBeanAddressing.Reference);
                expression.setOperationReference(_scriptOperationReferenceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeLocalVariable) {
                expression.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                expression.setOperationLocalVariable(_scriptOperationLocalVariableTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeFormula) {
                expression.setOperationAddressing(NamedBeanAddressing.Formula);
                expression.setOperationFormula(_scriptOperationFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOperationType has unknown selection");
            }

            if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeDirect) {
                expression.setScriptAddressing(NamedBeanAddressing.Direct);
                expression.setScript(_scriptTextField.getText());
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeReference) {
                expression.setScriptAddressing(NamedBeanAddressing.Reference);
                expression.setScriptReference(_scriptReferenceTextField.getText());
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeLocalVariable) {
                expression.setScriptAddressing(NamedBeanAddressing.LocalVariable);
                expression.setScriptLocalVariable(_scriptLocalVariableTextField.getText());
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeFormula) {
                expression.setScriptAddressing(NamedBeanAddressing.Formula);
                expression.setScriptFormula(_scriptFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAspectType has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        expression.setRegisterListenerScript(_registerListener.getText());
        expression.setUnregisterListenerScript(_unregisterListener.getText());

        _scriptEngineSelectorSwing.update();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ExpressionScript_Short");
    }

    @Override
    public void dispose() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionScriptSwing.class);

}
