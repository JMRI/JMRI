package jmri.jmrit.logixng.expressions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionWarrant;
import jmri.jmrit.logixng.expressions.ExpressionWarrant.WarrantState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionWarrant object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionWarrantSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<Warrant> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneWarrantState;
    private JComboBox<WarrantState> _stateComboBox;
    private JPanel _panelWarrantStateDirect;
    private JPanel _panelWarrantStateReference;
    private JPanel _panelWarrantStateLocalVariable;
    private JPanel _panelWarrantStateFormula;
    private JTextField _warrantStateReferenceTextField;
    private JTextField _warrantStateLocalVariableTextField;
    private JTextField _warrantStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionWarrant expression = (ExpressionWarrant)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(WarrantManager.class), getJDialog(), this);

        JPanel _tabbedPaneNamedBean;
        if (expression != null) {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(expression.getSelectNamedBean());
        } else {
            _tabbedPaneNamedBean = _selectNamedBeanSwing.createPanel(null);
        }


        _is_IsNot_ComboBox = new JComboBox<>();
        for (Is_IsNot_Enum e : Is_IsNot_Enum.values()) {
            _is_IsNot_ComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_is_IsNot_ComboBox);


        _tabbedPaneWarrantState = new JTabbedPane();
        _panelWarrantStateDirect = new javax.swing.JPanel();
        _panelWarrantStateReference = new javax.swing.JPanel();
        _panelWarrantStateLocalVariable = new javax.swing.JPanel();
        _panelWarrantStateFormula = new javax.swing.JPanel();

        _tabbedPaneWarrantState.addTab(NamedBeanAddressing.Direct.toString(), _panelWarrantStateDirect);
        _tabbedPaneWarrantState.addTab(NamedBeanAddressing.Reference.toString(), _panelWarrantStateReference);
        _tabbedPaneWarrantState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelWarrantStateLocalVariable);
        _tabbedPaneWarrantState.addTab(NamedBeanAddressing.Formula.toString(), _panelWarrantStateFormula);

        _stateComboBox = new JComboBox<>();
        for (WarrantState e : WarrantState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _panelWarrantStateDirect.add(_stateComboBox);

        _warrantStateReferenceTextField = new JTextField();
        _warrantStateReferenceTextField.setColumns(30);
        _panelWarrantStateReference.add(_warrantStateReferenceTextField);

        _warrantStateLocalVariableTextField = new JTextField();
        _warrantStateLocalVariableTextField.setColumns(30);
        _panelWarrantStateLocalVariable.add(_warrantStateLocalVariableTextField);

        _warrantStateFormulaTextField = new JTextField();
        _warrantStateFormulaTextField.setColumns(30);
        _panelWarrantStateFormula.add(_warrantStateFormulaTextField);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneWarrantState.setSelectedComponent(_panelWarrantStateDirect); break;
                case Reference: _tabbedPaneWarrantState.setSelectedComponent(_panelWarrantStateReference); break;
                case LocalVariable: _tabbedPaneWarrantState.setSelectedComponent(_panelWarrantStateLocalVariable); break;
                case Formula: _tabbedPaneWarrantState.setSelectedComponent(_panelWarrantStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _warrantStateReferenceTextField.setText(expression.getStateReference());
            _warrantStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _warrantStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneWarrantState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionWarrant_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionWarrant expression = new ExpressionWarrant("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneWarrantState.getSelectedComponent() == _panelWarrantStateReference) {
                expression.setStateReference(_warrantStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
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
        ExpressionWarrant expression = new ExpressionWarrant(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionWarrant)) {
            throw new IllegalArgumentException("object must be an ExpressionWarrant but is a: "+object.getClass().getName());
        }
        ExpressionWarrant expression = (ExpressionWarrant)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneWarrantState.getSelectedComponent() == _panelWarrantStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((WarrantState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneWarrantState.getSelectedComponent() == _panelWarrantStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_warrantStateReferenceTextField.getText());
            } else if (_tabbedPaneWarrantState.getSelectedComponent() == _panelWarrantStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_warrantStateLocalVariableTextField.getText());
            } else if (_tabbedPaneWarrantState.getSelectedComponent() == _panelWarrantStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_warrantStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneWarrantState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("Warrant_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionWarrantSwing.class);

}
