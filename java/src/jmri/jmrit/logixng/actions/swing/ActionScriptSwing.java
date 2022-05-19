package jmri.jmrit.logixng.actions.swing;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionScript;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.script.ScriptEngineSelector;
import jmri.script.swing.ScriptEngineSelectorSwing;
import jmri.script.swing.ScriptFileChooser;
import jmri.util.FileUtil;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionScript object with a Swing JPanel.
 *
 * @author Daniel Bergqvist 2021
 */
public class ActionScriptSwing extends AbstractDigitalActionSwing {

    public static final int NUM_COLUMNS_TEXT_FIELDS = 20;

    private JTabbedPane _tabbedPaneOperationType;
    private JPanel _panelOperationTypeDirect;
    private JPanel _panelOperationTypeReference;
    private JPanel _panelOperationTypeLocalVariable;
    private JPanel _panelOperationTypeFormula;

    private JComboBox<ActionScript.OperationType> _operationComboBox;
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

    private ScriptEngineSelectorSwing _scriptEngineSelectorSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionScript action = (ActionScript)object;

        if (action != null) {
            _scriptEngineSelectorSwing = new ScriptEngineSelectorSwing(action.getScriptEngineSelector());
        } else {
            _scriptEngineSelectorSwing = new ScriptEngineSelectorSwing(new ScriptEngineSelector());
        }

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel actionPanel = new JPanel();


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
        for (ActionScript.OperationType e : ActionScript.OperationType.values()) {
            _operationComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_operationComboBox);
        _panelOperationTypeDirect.add(new JLabel(Bundle.getMessage("ActionScript_Operation")));
        _panelOperationTypeDirect.add(_operationComboBox);

        _scriptOperationReferenceTextField = new JTextField();
        _scriptOperationReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeReference.add(new JLabel(Bundle.getMessage("ActionScript_Operation")));
        _panelOperationTypeReference.add(_scriptOperationReferenceTextField);

        _scriptOperationLocalVariableTextField = new JTextField();
        _scriptOperationLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionScript_Operation")));
        _panelOperationTypeLocalVariable.add(_scriptOperationLocalVariableTextField);

        _scriptOperationFormulaTextField = new JTextField();
        _scriptOperationFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelOperationTypeFormula.add(new JLabel(Bundle.getMessage("ActionScript_Operation")));
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

        JButton _actionSelectFileButton = new JButton("..."); // "File" replaced by ...
        _actionSelectFileButton.setMaximumSize(_actionSelectFileButton.getPreferredSize());
        _actionSelectFileButton.setToolTipText(Bundle.getMessage("FileButtonHint"));  // NOI18N
        _actionSelectFileButton.addActionListener((ActionEvent e) -> {
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
        _panelScriptTypeDirect.add(_actionSelectFileButton);
        JPanel _scriptTextPanel = new JPanel();
        _scriptTextPanel.setLayout(new BoxLayout(_scriptTextPanel, BoxLayout.Y_AXIS));
        _scriptTextField = new JTextField(30);
        _scriptTextPanel.add(new JLabel(Bundle.getMessage("ActionScript_Script")));
        _scriptTextPanel.add(_scriptTextField);
        _panelScriptTypeDirect.add(_scriptTextPanel);

        _scriptReferenceTextField = new JTextField();
        _scriptReferenceTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelScriptTypeReference.add(new JLabel(Bundle.getMessage("ActionScript_Script")));
        _panelScriptTypeReference.add(_scriptReferenceTextField);

        _scriptLocalVariableTextField = new JTextField();
        _scriptLocalVariableTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelScriptTypeLocalVariable.add(new JLabel(Bundle.getMessage("ActionScript_Script")));
        _panelScriptTypeLocalVariable.add(_scriptLocalVariableTextField);

        _scriptFormulaTextField = new JTextField();
        _scriptFormulaTextField.setColumns(NUM_COLUMNS_TEXT_FIELDS);
        _panelScriptTypeFormula.add(new JLabel(Bundle.getMessage("ActionScript_Script")));
        _panelScriptTypeFormula.add(_scriptFormulaTextField);


        if (action != null) {
            switch (action.getOperationAddressing()) {
                case Direct: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeDirect); break;
                case Reference: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeReference); break;
                case LocalVariable: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeLocalVariable); break;
                case Formula: _tabbedPaneOperationType.setSelectedComponent(_panelOperationTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getOperationAddressing().name());
            }
            _operationComboBox.setSelectedItem(action.getOperationType());
            _scriptOperationReferenceTextField.setText(action.getOperationReference());
            _scriptOperationLocalVariableTextField.setText(action.getOperationLocalVariable());
            _scriptOperationFormulaTextField.setText(action.getOperationFormula());

            switch (action.getScriptAddressing()) {
                case Direct: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeDirect); break;
                case Reference: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeReference); break;
                case LocalVariable: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeLocalVariable); break;
                case Formula: _tabbedPaneScriptType.setSelectedComponent(_panelScriptTypeFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getScriptAddressing().name());
            }
            _scriptTextField.setText(action.getScript());
            _scriptReferenceTextField.setText(action.getScriptReference());
            _scriptLocalVariableTextField.setText(action.getScriptLocalVariable());
            _scriptFormulaTextField.setText(action.getScriptFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneOperationType,
            _tabbedPaneScriptType
        };

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionScript_Components"), components);

        for (JComponent c : componentList) actionPanel.add(c);
        panel.add(actionPanel);

        JPanel scriptSelectorPanel = new JPanel();
        scriptSelectorPanel.add(new JLabel(Bundle.getMessage("ActionScript_ScriptSelector")));
        scriptSelectorPanel.add(_scriptEngineSelectorSwing.getComboBox());

        panel.add(scriptSelectorPanel);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionScript action = new ActionScript("IQDA1", null);

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationReference(_scriptOperationReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setScriptFormula(_scriptFormulaTextField.getText());
            if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeDirect) {
                action.setScriptAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeReference) {
                action.setScriptAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeLocalVariable) {
                action.setScriptAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeFormula) {
                action.setScriptAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPaneScriptType has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionScript action = new ActionScript(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionScript)) {
            throw new IllegalArgumentException("object must be an ActionScript but is a: "+object.getClass().getName());
        }
        ActionScript action = (ActionScript)object;

        try {
            if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeDirect) {
                action.setOperationAddressing(NamedBeanAddressing.Direct);
                action.setOperationType((ActionScript.OperationType)_operationComboBox.getSelectedItem());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeReference) {
                action.setOperationAddressing(NamedBeanAddressing.Reference);
                action.setOperationReference(_scriptOperationReferenceTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeLocalVariable) {
                action.setOperationAddressing(NamedBeanAddressing.LocalVariable);
                action.setOperationLocalVariable(_scriptOperationLocalVariableTextField.getText());
            } else if (_tabbedPaneOperationType.getSelectedComponent() == _panelOperationTypeFormula) {
                action.setOperationAddressing(NamedBeanAddressing.Formula);
                action.setOperationFormula(_scriptOperationFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneOperationType has unknown selection");
            }

            if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeDirect) {
                action.setScriptAddressing(NamedBeanAddressing.Direct);
                action.setScript(_scriptTextField.getText());
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeReference) {
                action.setScriptAddressing(NamedBeanAddressing.Reference);
                action.setScriptReference(_scriptReferenceTextField.getText());
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeLocalVariable) {
                action.setScriptAddressing(NamedBeanAddressing.LocalVariable);
                action.setScriptLocalVariable(_scriptLocalVariableTextField.getText());
            } else if (_tabbedPaneScriptType.getSelectedComponent() == _panelScriptTypeFormula) {
                action.setScriptAddressing(NamedBeanAddressing.Formula);
                action.setScriptFormula(_scriptFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAspectType has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _scriptEngineSelectorSwing.update();
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionScript_Short");
    }

    @Override
    public void dispose() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionScriptSwing.class);

}
