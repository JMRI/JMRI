package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionDispatcher;
import jmri.jmrit.logixng.expressions.ExpressionDispatcher.DispatcherState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.DispatcherActiveTrainManager;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionDispatcher object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionDispatcherSwing extends AbstractDigitalExpressionSwing {

    private JTabbedPane _tabbedPaneDispatcher;
    private JPanel _panelDispatcherDirect;
    private JPanel _panelDispatcherReference;
    private JPanel _panelDispatcherLocalVariable;
    private JPanel _panelDispatcherFormula;

    private JComboBox<String> _fileNamesComboBox;
    private JTextField _dispatcherReferenceTextField;
    private JTextField _dispatcherLocalVariableTextField;
    private JTextField _dispatcherFormulaTextField;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneDispatcherState;
    private JPanel _panelDispatcherStateDirect;
    private JPanel _panelDispatcherStateReference;
    private JPanel _panelDispatcherStateLocalVariable;
    private JPanel _panelDispatcherStateFormula;

    private JComboBox<DispatcherState> _stateComboBox;
    private JTextField _dispatcherStateReferenceTextField;
    private JTextField _dispatcherStateLocalVariableTextField;
    private JTextField _dispatcherStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionDispatcher expression = (ExpressionDispatcher)object;

        panel = new JPanel();

        _tabbedPaneDispatcher = new JTabbedPane();
        _panelDispatcherDirect = new javax.swing.JPanel();
        _panelDispatcherReference = new javax.swing.JPanel();
        _panelDispatcherLocalVariable = new javax.swing.JPanel();
        _panelDispatcherFormula = new javax.swing.JPanel();

        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.Direct.toString(), _panelDispatcherDirect);
        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.Reference.toString(), _panelDispatcherReference);
        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDispatcherLocalVariable);
        _tabbedPaneDispatcher.addTab(NamedBeanAddressing.Formula.toString(), _panelDispatcherFormula);

        _fileNamesComboBox = new JComboBox<>();
        InstanceManager.getDefault(DispatcherActiveTrainManager.class).getTrainInfoFileNames().forEach(name -> {
            _fileNamesComboBox.addItem(name);
        });
        JComboBoxUtil.setupComboBoxMaxRows(_fileNamesComboBox);
        _panelDispatcherDirect.add(_fileNamesComboBox);

        _dispatcherReferenceTextField = new JTextField();
        _dispatcherReferenceTextField.setColumns(30);
        _panelDispatcherReference.add(_dispatcherReferenceTextField);

        _dispatcherLocalVariableTextField = new JTextField();
        _dispatcherLocalVariableTextField.setColumns(30);
        _panelDispatcherLocalVariable.add(_dispatcherLocalVariableTextField);

        _dispatcherFormulaTextField = new JTextField();
        _dispatcherFormulaTextField.setColumns(30);
        _panelDispatcherFormula.add(_dispatcherFormulaTextField);


        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);


        _tabbedPaneDispatcherState = new JTabbedPane();
        _panelDispatcherStateDirect = new javax.swing.JPanel();
        _panelDispatcherStateReference = new javax.swing.JPanel();
        _panelDispatcherStateLocalVariable = new javax.swing.JPanel();
        _panelDispatcherStateFormula = new javax.swing.JPanel();

        _tabbedPaneDispatcherState.addTab(NamedBeanAddressing.Direct.toString(), _panelDispatcherStateDirect);
        _tabbedPaneDispatcherState.addTab(NamedBeanAddressing.Reference.toString(), _panelDispatcherStateReference);
        _tabbedPaneDispatcherState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelDispatcherStateLocalVariable);
        _tabbedPaneDispatcherState.addTab(NamedBeanAddressing.Formula.toString(), _panelDispatcherStateFormula);

        _stateComboBox = new JComboBox<>();
        for (DispatcherState e : DispatcherState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelDispatcherStateDirect.add(_stateComboBox);

        _dispatcherStateReferenceTextField = new JTextField();
        _dispatcherStateReferenceTextField.setColumns(30);
        _panelDispatcherStateReference.add(_dispatcherStateReferenceTextField);

        _dispatcherStateLocalVariableTextField = new JTextField();
        _dispatcherStateLocalVariableTextField.setColumns(30);
        _panelDispatcherStateLocalVariable.add(_dispatcherStateLocalVariableTextField);

        _dispatcherStateFormulaTextField = new JTextField();
        _dispatcherStateFormulaTextField.setColumns(30);
        _panelDispatcherStateFormula.add(_dispatcherStateFormulaTextField);


        if (expression != null) {
            switch (expression.getAddressing()) {
                case Direct: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherDirect); break;
                case Reference: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherReference); break;
                case LocalVariable: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherLocalVariable); break;
                case Formula: _tabbedPaneDispatcher.setSelectedComponent(_panelDispatcherFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }

            _fileNamesComboBox.setSelectedItem(expression.getTrainInfoFileName());
            _dispatcherReferenceTextField.setText(expression.getReference());
            _dispatcherLocalVariableTextField.setText(expression.getLocalVariable());
            _dispatcherFormulaTextField.setText(expression.getFormula());

            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneDispatcherState.setSelectedComponent(_panelDispatcherStateDirect); break;
                case Reference: _tabbedPaneDispatcherState.setSelectedComponent(_panelDispatcherStateReference); break;
                case LocalVariable: _tabbedPaneDispatcherState.setSelectedComponent(_panelDispatcherStateLocalVariable); break;
                case Formula: _tabbedPaneDispatcherState.setSelectedComponent(_panelDispatcherStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _dispatcherStateReferenceTextField.setText(expression.getStateReference());
            _dispatcherStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _dispatcherStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneDispatcher,
            _is_IsNot_ComboBox,
            _tabbedPaneDispatcherState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionDispatcher_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (_tabbedPaneDispatcherState.getSelectedComponent() == _panelDispatcherStateDirect) {
            DispatcherState state = (DispatcherState) _stateComboBox.getSelectedItem();
            if (state.getType().equals("Separator")) {
                errorMessages.add(Bundle.getMessage("Dispatcher_No_Status_Selected"));
            }
        }


        // Create a temporary expression to test formula
        ExpressionDispatcher expression = new ExpressionDispatcher("IQDE1", null);

        try {
            if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherReference) {
                expression.setReference(_dispatcherReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneDispatcherState.getSelectedComponent() == _panelDispatcherStateReference) {
                expression.setStateReference(_dispatcherStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            expression.setFormula(_dispatcherFormulaTextField.getText());
            if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public String getAutoSystemName() {
        return InstanceManager.getDefault(DigitalExpressionManager.class).getAutoSystemName();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ExpressionDispatcher expression = new ExpressionDispatcher(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionDispatcher)) {
            throw new IllegalArgumentException("object must be an ExpressionDispatcher but is a: " + object.getClass().getName());
        }
        ExpressionDispatcher expression = (ExpressionDispatcher)object;

        try {
            if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherDirect) {
                expression.setAddressing(NamedBeanAddressing.Direct);
                expression.setTrainInfoFileName((String) _fileNamesComboBox.getSelectedItem());
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherReference) {
                expression.setAddressing(NamedBeanAddressing.Reference);
                expression.setReference(_dispatcherReferenceTextField.getText());
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherLocalVariable) {
                expression.setAddressing(NamedBeanAddressing.LocalVariable);
                expression.setLocalVariable(_dispatcherLocalVariableTextField.getText());
            } else if (_tabbedPaneDispatcher.getSelectedComponent() == _panelDispatcherFormula) {
                expression.setAddressing(NamedBeanAddressing.Formula);
                expression.setFormula(_dispatcherFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneDispatcher has unknown selection");
            }

            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneDispatcherState.getSelectedComponent() == _panelDispatcherStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((DispatcherState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneDispatcherState.getSelectedComponent() == _panelDispatcherStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_dispatcherStateReferenceTextField.getText());
            } else if (_tabbedPaneDispatcherState.getSelectedComponent() == _panelDispatcherStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_dispatcherStateLocalVariableTextField.getText());
            } else if (_tabbedPaneDispatcherState.getSelectedComponent() == _panelDispatcherStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_dispatcherStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneDispatcherState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: " + e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Dispatcher_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionDispatcherSwing.class);

}
