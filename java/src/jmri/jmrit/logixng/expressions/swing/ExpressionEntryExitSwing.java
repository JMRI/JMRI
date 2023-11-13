package jmri.jmrit.logixng.expressions.swing;

import java.awt.Component;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.entryexit.EntryExitPairs;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.expressions.ExpressionEntryExit;
import jmri.jmrit.logixng.expressions.ExpressionEntryExit.EntryExitState;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ExpressionEntryExit object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ExpressionEntryExitSwing extends AbstractDigitalExpressionSwing {

    private LogixNG_SelectNamedBeanSwing<DestinationPoints> _selectNamedBeanSwing;

    private JComboBox<Is_IsNot_Enum> _is_IsNot_ComboBox;

    private JTabbedPane _tabbedPaneEntryExitState;
    private JComboBox<EntryExitState> _stateComboBox;
    private JPanel _panelEntryExitStateDirect;
    private JPanel _panelEntryExitStateReference;
    private JPanel _panelEntryExitStateLocalVariable;
    private JPanel _panelEntryExitStateFormula;
    private JTextField _entryExitStateReferenceTextField;
    private JTextField _entryExitStateLocalVariableTextField;
    private JTextField _entryExitStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ExpressionEntryExit expression = (ExpressionEntryExit)object;

        panel = new JPanel();

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(EntryExitPairs.class), getJDialog(), this);

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


        _tabbedPaneEntryExitState = new JTabbedPane();
        _panelEntryExitStateDirect = new javax.swing.JPanel();
        _panelEntryExitStateReference = new javax.swing.JPanel();
        _panelEntryExitStateLocalVariable = new javax.swing.JPanel();
        _panelEntryExitStateFormula = new javax.swing.JPanel();

        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.Direct.toString(), _panelEntryExitStateDirect);
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.Reference.toString(), _panelEntryExitStateReference);
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelEntryExitStateLocalVariable);
        _tabbedPaneEntryExitState.addTab(NamedBeanAddressing.Formula.toString(), _panelEntryExitStateFormula);

        _stateComboBox = new JComboBox<>();
        for (EntryExitState e : EntryExitState.values()) {
            _stateComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_stateComboBox);

        _stateComboBox.setRenderer(new ComboBoxRenderer<>(_stateComboBox.getRenderer()));

        _panelEntryExitStateDirect.add(_stateComboBox);

        _entryExitStateReferenceTextField = new JTextField();
        _entryExitStateReferenceTextField.setColumns(30);
        _panelEntryExitStateReference.add(_entryExitStateReferenceTextField);

        _entryExitStateLocalVariableTextField = new JTextField();
        _entryExitStateLocalVariableTextField.setColumns(30);
        _panelEntryExitStateLocalVariable.add(_entryExitStateLocalVariableTextField);

        _entryExitStateFormulaTextField = new JTextField();
        _entryExitStateFormulaTextField.setColumns(30);
        _panelEntryExitStateFormula.add(_entryExitStateFormulaTextField);


        if (expression != null) {
            _is_IsNot_ComboBox.setSelectedItem(expression.get_Is_IsNot());

            switch (expression.getStateAddressing()) {
                case Direct: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateDirect); break;
                case Reference: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateReference); break;
                case LocalVariable: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateLocalVariable); break;
                case Formula: _tabbedPaneEntryExitState.setSelectedComponent(_panelEntryExitStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + expression.getStateAddressing().name());
            }
            _stateComboBox.setSelectedItem(expression.getBeanState());
            _entryExitStateReferenceTextField.setText(expression.getStateReference());
            _entryExitStateLocalVariableTextField.setText(expression.getStateLocalVariable());
            _entryExitStateFormulaTextField.setText(expression.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _tabbedPaneNamedBean,
            _is_IsNot_ComboBox,
            _tabbedPaneEntryExitState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ExpressionEntryExit_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary expression to test formula
        ExpressionEntryExit expression = new ExpressionEntryExit("IQDE1", null);

        _selectNamedBeanSwing.validate(expression.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateReference) {
                expression.setStateReference(_entryExitStateReferenceTextField.getText());
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
        ExpressionEntryExit expression = new ExpressionEntryExit(systemName, userName);
        updateObject(expression);
        return InstanceManager.getDefault(DigitalExpressionManager.class).registerExpression(expression);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ExpressionEntryExit)) {
            throw new IllegalArgumentException("object must be an ExpressionEntryExit but is a: "+object.getClass().getName());
        }
        ExpressionEntryExit expression = (ExpressionEntryExit)object;

        _selectNamedBeanSwing.updateObject(expression.getSelectNamedBean());

        try {
            expression.set_Is_IsNot((Is_IsNot_Enum)_is_IsNot_ComboBox.getSelectedItem());

            if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateDirect) {
                expression.setStateAddressing(NamedBeanAddressing.Direct);
                expression.setBeanState((EntryExitState)_stateComboBox.getSelectedItem());
            } else if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateReference) {
                expression.setStateAddressing(NamedBeanAddressing.Reference);
                expression.setStateReference(_entryExitStateReferenceTextField.getText());
            } else if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateLocalVariable) {
                expression.setStateAddressing(NamedBeanAddressing.LocalVariable);
                expression.setStateLocalVariable(_entryExitStateLocalVariableTextField.getText());
            } else if (_tabbedPaneEntryExitState.getSelectedComponent() == _panelEntryExitStateFormula) {
                expression.setStateAddressing(NamedBeanAddressing.Formula);
                expression.setStateFormula(_entryExitStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEntryExitState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("EntryExit_Short");
    }

    @Override
    public void dispose() {
    }


    private static class ComboBoxRenderer<E> extends JLabel implements ListCellRenderer<E> {

        private final JSeparator _separator = new JSeparator(JSeparator.HORIZONTAL);
        private final ListCellRenderer<E> _old;

        private ComboBoxRenderer(ListCellRenderer<E> old) {
            this._old = old;
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends E> list,
                E value, int index, boolean isSelected, boolean cellHasFocus) {
            if (Base.SEPARATOR.equals(value.toString())) {
                return _separator;
            } else {
                return _old.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            }
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionEntryExitSwing.class);

}
