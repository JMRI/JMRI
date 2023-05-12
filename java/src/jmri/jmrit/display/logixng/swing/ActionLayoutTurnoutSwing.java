package jmri.jmrit.display.logixng.swing;

import java.awt.event.ActionEvent;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.logixng.ActionLayoutTurnout;
import jmri.jmrit.display.logixng.ActionLayoutTurnout.Operation;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionLayoutTurnout object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class ActionLayoutTurnoutSwing extends AbstractDigitalActionSwing {

    private String _selectedLayoutEditor;

    private JComboBox<EditorItem> _layoutEditorComboBox;
    private JTabbedPane _tabbedPaneLayoutTurnout;
    private JComboBox<Turnout> _layoutTurnoutComboBox;
    private JPanel _panelLayoutTurnoutDirect;
    private JPanel _panelLayoutTurnoutReference;
    private JPanel _panelLayoutTurnoutLocalVariable;
    private JPanel _panelLayoutTurnoutFormula;
    private JTextField _layoutTurnoutReferenceTextField;
    private JTextField _layoutTurnoutLocalVariableTextField;
    private JTextField _layoutTurnoutFormulaTextField;

    private JTabbedPane _tabbedPaneLayoutTurnoutState;
    private JComboBox<Operation> _isControllingComboBox;
    private JPanel _panelLayoutTurnoutStateDirect;
    private JPanel _panelLayoutTurnoutStateReference;
    private JPanel _panelLayoutTurnoutStateLocalVariable;
    private JPanel _panelLayoutTurnoutStateFormula;
    private JTextField _layoutTurnoutStateReferenceTextField;
    private JTextField _layoutTurnoutStateLocalVariableTextField;
    private JTextField _layoutTurnoutStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionLayoutTurnout action = (ActionLayoutTurnout)object;

        panel = new JPanel();

        _selectedLayoutEditor = action != null ? action.getLayoutEditorName() : null;

        _layoutEditorComboBox = new JComboBox<>();
        JComboBoxUtil.setupComboBoxMaxRows(_layoutEditorComboBox);
        for (LayoutEditor layoutEditor : jmri.InstanceManager.getDefault(EditorManager.class).getAll(LayoutEditor.class)) {
            EditorItem item = new EditorItem(layoutEditor);
            _layoutEditorComboBox.addItem(item);
            if (layoutEditor.getName().equals(_selectedLayoutEditor)) _layoutEditorComboBox.setSelectedItem(item);
        }
        _layoutEditorComboBox.addActionListener(this::updateLayoutTurnouts);

        _tabbedPaneLayoutTurnout = new JTabbedPane();
        _panelLayoutTurnoutDirect = new javax.swing.JPanel();
        _panelLayoutTurnoutReference = new javax.swing.JPanel();
        _panelLayoutTurnoutLocalVariable = new javax.swing.JPanel();
        _panelLayoutTurnoutFormula = new javax.swing.JPanel();

        _tabbedPaneLayoutTurnout.addTab(NamedBeanAddressing.Direct.toString(), _panelLayoutTurnoutDirect);
        _tabbedPaneLayoutTurnout.addTab(NamedBeanAddressing.Reference.toString(), _panelLayoutTurnoutReference);
        _tabbedPaneLayoutTurnout.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLayoutTurnoutLocalVariable);
        _tabbedPaneLayoutTurnout.addTab(NamedBeanAddressing.Formula.toString(), _panelLayoutTurnoutFormula);

        _layoutTurnoutComboBox = new JComboBox<>();
        JComboBoxUtil.setupComboBoxMaxRows(_layoutTurnoutComboBox);
        updateLayoutTurnouts(null);
        _panelLayoutTurnoutDirect.add(_layoutTurnoutComboBox);

        _layoutTurnoutReferenceTextField = new JTextField();
        _layoutTurnoutReferenceTextField.setColumns(30);
        _panelLayoutTurnoutReference.add(_layoutTurnoutReferenceTextField);

        _layoutTurnoutLocalVariableTextField = new JTextField();
        _layoutTurnoutLocalVariableTextField.setColumns(30);
        _panelLayoutTurnoutLocalVariable.add(_layoutTurnoutLocalVariableTextField);

        _layoutTurnoutFormulaTextField = new JTextField();
        _layoutTurnoutFormulaTextField.setColumns(30);
        _panelLayoutTurnoutFormula.add(_layoutTurnoutFormulaTextField);


        _tabbedPaneLayoutTurnoutState = new JTabbedPane();
        _panelLayoutTurnoutStateDirect = new javax.swing.JPanel();
        _panelLayoutTurnoutStateReference = new javax.swing.JPanel();
        _panelLayoutTurnoutStateLocalVariable = new javax.swing.JPanel();
        _panelLayoutTurnoutStateFormula = new javax.swing.JPanel();

        _tabbedPaneLayoutTurnoutState.addTab(NamedBeanAddressing.Direct.toString(), _panelLayoutTurnoutStateDirect);
        _tabbedPaneLayoutTurnoutState.addTab(NamedBeanAddressing.Reference.toString(), _panelLayoutTurnoutStateReference);
        _tabbedPaneLayoutTurnoutState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLayoutTurnoutStateLocalVariable);
        _tabbedPaneLayoutTurnoutState.addTab(NamedBeanAddressing.Formula.toString(), _panelLayoutTurnoutStateFormula);

        _isControllingComboBox = new JComboBox<>();
        for (Operation e : Operation.values()) {
            _isControllingComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_isControllingComboBox);

        _panelLayoutTurnoutStateDirect.add(_isControllingComboBox);

        _layoutTurnoutStateReferenceTextField = new JTextField();
        _layoutTurnoutStateReferenceTextField.setColumns(30);
        _panelLayoutTurnoutStateReference.add(_layoutTurnoutStateReferenceTextField);

        _layoutTurnoutStateLocalVariableTextField = new JTextField();
        _layoutTurnoutStateLocalVariableTextField.setColumns(30);
        _panelLayoutTurnoutStateLocalVariable.add(_layoutTurnoutStateLocalVariableTextField);

        _layoutTurnoutStateFormulaTextField = new JTextField();
        _layoutTurnoutStateFormulaTextField.setColumns(30);
        _panelLayoutTurnoutStateFormula.add(_layoutTurnoutStateFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneLayoutTurnout.setSelectedComponent(_panelLayoutTurnoutDirect); break;
                case Reference: _tabbedPaneLayoutTurnout.setSelectedComponent(_panelLayoutTurnoutReference); break;
                case LocalVariable: _tabbedPaneLayoutTurnout.setSelectedComponent(_panelLayoutTurnoutLocalVariable); break;
                case Formula: _tabbedPaneLayoutTurnout.setSelectedComponent(_panelLayoutTurnoutFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getLayoutTurnout() != null) {
                _layoutTurnoutComboBox.setSelectedItem(action.getLayoutTurnout());
            }
            _layoutTurnoutReferenceTextField.setText(action.getReference());
            _layoutTurnoutLocalVariableTextField.setText(action.getLocalVariable());
            _layoutTurnoutFormulaTextField.setText(action.getFormula());

            switch (action.getStateAddressing()) {
                case Direct: _tabbedPaneLayoutTurnoutState.setSelectedComponent(_panelLayoutTurnoutStateDirect); break;
                case Reference: _tabbedPaneLayoutTurnoutState.setSelectedComponent(_panelLayoutTurnoutStateReference); break;
                case LocalVariable: _tabbedPaneLayoutTurnoutState.setSelectedComponent(_panelLayoutTurnoutStateLocalVariable); break;
                case Formula: _tabbedPaneLayoutTurnoutState.setSelectedComponent(_panelLayoutTurnoutStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _isControllingComboBox.setSelectedItem(action.getOperation());
            _layoutTurnoutStateReferenceTextField.setText(action.getStateReference());
            _layoutTurnoutStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _layoutTurnoutStateFormulaTextField.setText(action.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _layoutEditorComboBox,
            _tabbedPaneLayoutTurnout,
            _tabbedPaneLayoutTurnoutState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionLayoutTurnout_Components"), components);

        for (JComponent c : componentList) panel.add(c);
    }

    private void updateLayoutTurnouts(ActionEvent e) {
        _layoutTurnoutComboBox.removeAllItems();
        if (_layoutEditorComboBox.getSelectedIndex() == -1) return;

        EditorItem item = _layoutEditorComboBox.getItemAt(_layoutEditorComboBox.getSelectedIndex());
        List<LayoutTurnout> list = new ArrayList<>();
        for (LayoutTurnout layoutTurnout : item._layoutEditor.getLayoutTurnouts()) {
            if (!layoutTurnout.getTurnoutName().isBlank()) {
                list.add(layoutTurnout);
            }
        }
        Collections.sort(list, (o1,o2) -> o1.getTurnoutName().compareTo(o2.getTurnoutName()));
        for (LayoutTurnout lt : list) {
            _layoutTurnoutComboBox.addItem(new Turnout(lt));
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionLayoutTurnout action = new ActionLayoutTurnout("IQDA1", null);

        try {
            if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutReference) {
                action.setReference(_layoutTurnoutReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneLayoutTurnoutState.getSelectedComponent() == _panelLayoutTurnoutStateReference) {
                action.setStateReference(_layoutTurnoutStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_layoutTurnoutFormulaTextField.getText());
            if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionLayoutTurnout action = new ActionLayoutTurnout(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionLayoutTurnout)) {
            throw new IllegalArgumentException("object must be an ActionLayoutTurnout but is a: "+object.getClass().getName());
        }
        ActionLayoutTurnout action = (ActionLayoutTurnout)object;
        if (_layoutEditorComboBox.getSelectedIndex() != -1) {
            action.setLayoutEditor(_layoutEditorComboBox.getItemAt(_layoutEditorComboBox.getSelectedIndex())._layoutEditor.getName());
        }
        if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutDirect) {
            if (_layoutTurnoutComboBox.getSelectedIndex() != -1) {
                action.setLayoutTurnout(_layoutTurnoutComboBox.getItemAt(_layoutTurnoutComboBox.getSelectedIndex())._lt);
            } else {
                action.setLayoutTurnout((LayoutTurnout)null);
            }
        }
        try {
            if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_layoutTurnoutReferenceTextField.getText());
            } else if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_layoutTurnoutLocalVariableTextField.getText());
            } else if (_tabbedPaneLayoutTurnout.getSelectedComponent() == _panelLayoutTurnoutFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_layoutTurnoutFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLayoutTurnout has unknown selection");
            }

            if (_tabbedPaneLayoutTurnoutState.getSelectedComponent() == _panelLayoutTurnoutStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
                action.setOperation(_isControllingComboBox.getItemAt(_isControllingComboBox.getSelectedIndex()));
            } else if (_tabbedPaneLayoutTurnoutState.getSelectedComponent() == _panelLayoutTurnoutStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
                action.setStateReference(_layoutTurnoutStateReferenceTextField.getText());
            } else if (_tabbedPaneLayoutTurnoutState.getSelectedComponent() == _panelLayoutTurnoutStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
                action.setStateLocalVariable(_layoutTurnoutStateLocalVariableTextField.getText());
            } else if (_tabbedPaneLayoutTurnoutState.getSelectedComponent() == _panelLayoutTurnoutStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
                action.setStateFormula(_layoutTurnoutStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneLayoutTurnoutState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionLayoutTurnout_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }


    private static class EditorItem {

        private final LayoutEditor _layoutEditor;

        public EditorItem(LayoutEditor layoutEditor) {
            this._layoutEditor = layoutEditor;
        }

        @Override
        public String toString() {
            return _layoutEditor.getName();
        }
    }


    private static class Turnout {
        private final LayoutTurnout _lt;

        public Turnout(LayoutTurnout turnout) {
            _lt = turnout;
        }

        @Override
        public String toString() {
            return _lt.getTurnoutName();
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionLayoutTurnoutSwing.class);

}
