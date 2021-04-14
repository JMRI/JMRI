package jmri.jmrit.display.logixng.swing;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.logixng.*;
import jmri.jmrit.display.logixng.ActionEnableDisable;
import jmri.jmrit.display.logixng.ActionEnableDisable.IsControlling;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionEnableDisable object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright (C) 2021
 */
public class ActionEnableDisableSwing extends AbstractDigitalActionSwing {

    private String _selectedEditor;
    
    private JComboBox<EditorItem> _editorComboBox;
    private JTabbedPane _tabbedPanePositionable;
    private JComboBox<String> _positionableComboBox;
    private JPanel _panelPositionableDirect;
    private JPanel _panelPositionableReference;
    private JPanel _panelPositionableLocalVariable;
    private JPanel _panelPositionableFormula;
    private JTextField _positionableReferenceTextField;
    private JTextField _positionableLocalVariableTextField;
    private JTextField _positionableFormulaTextField;
    
    private JTabbedPane _tabbedPanePositionableState;
    private JComboBox<IsControlling> _isControllingComboBox;
    private JPanel _panelPositionableStateDirect;
    private JPanel _panelPositionableStateReference;
    private JPanel _panelPositionableStateLocalVariable;
    private JPanel _panelPositionableStateFormula;
    private JTextField _positionableStateReferenceTextField;
    private JTextField _positionableStateLocalVariableTextField;
    private JTextField _positionableStateFormulaTextField;
    
    
    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionEnableDisable action = (ActionEnableDisable)object;
        
        panel = new JPanel();
        
        _selectedEditor = action != null ? action.getEditorName() : null;
        
        _editorComboBox = new JComboBox<>();
        for (Editor editor : jmri.InstanceManager.getDefault(EditorManager.class).getAll()) {
            EditorItem item = new EditorItem(editor);
            _editorComboBox.addItem(item);
            if (editor.getName().equals(_selectedEditor)) _editorComboBox.setSelectedItem(item);
        }
        _editorComboBox.addActionListener(this::updatePositionables);
        
        _tabbedPanePositionable = new JTabbedPane();
        _panelPositionableDirect = new javax.swing.JPanel();
        _panelPositionableReference = new javax.swing.JPanel();
        _panelPositionableLocalVariable = new javax.swing.JPanel();
        _panelPositionableFormula = new javax.swing.JPanel();
        
        _tabbedPanePositionable.addTab(NamedBeanAddressing.Direct.toString(), _panelPositionableDirect);
        _tabbedPanePositionable.addTab(NamedBeanAddressing.Reference.toString(), _panelPositionableReference);
        _tabbedPanePositionable.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelPositionableLocalVariable);
        _tabbedPanePositionable.addTab(NamedBeanAddressing.Formula.toString(), _panelPositionableFormula);
        
        _positionableComboBox = new JComboBox<>();
        updatePositionables(null);
        _panelPositionableDirect.add(_positionableComboBox);
        
        _positionableReferenceTextField = new JTextField();
        _positionableReferenceTextField.setColumns(30);
        _panelPositionableReference.add(_positionableReferenceTextField);
        
        _positionableLocalVariableTextField = new JTextField();
        _positionableLocalVariableTextField.setColumns(30);
        _panelPositionableLocalVariable.add(_positionableLocalVariableTextField);
        
        _positionableFormulaTextField = new JTextField();
        _positionableFormulaTextField.setColumns(30);
        _panelPositionableFormula.add(_positionableFormulaTextField);
        
        
        _tabbedPanePositionableState = new JTabbedPane();
        _panelPositionableStateDirect = new javax.swing.JPanel();
        _panelPositionableStateReference = new javax.swing.JPanel();
        _panelPositionableStateLocalVariable = new javax.swing.JPanel();
        _panelPositionableStateFormula = new javax.swing.JPanel();
        
        _tabbedPanePositionableState.addTab(NamedBeanAddressing.Direct.toString(), _panelPositionableStateDirect);
        _tabbedPanePositionableState.addTab(NamedBeanAddressing.Reference.toString(), _panelPositionableStateReference);
        _tabbedPanePositionableState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelPositionableStateLocalVariable);
        _tabbedPanePositionableState.addTab(NamedBeanAddressing.Formula.toString(), _panelPositionableStateFormula);
        
        _isControllingComboBox = new JComboBox<>();
        for (IsControlling e : IsControlling.values()) {
            _isControllingComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_isControllingComboBox);
        
        _panelPositionableStateDirect.add(_isControllingComboBox);
        
        _positionableStateReferenceTextField = new JTextField();
        _positionableStateReferenceTextField.setColumns(30);
        _panelPositionableStateReference.add(_positionableStateReferenceTextField);
        
        _positionableStateLocalVariableTextField = new JTextField();
        _positionableStateLocalVariableTextField.setColumns(30);
        _panelPositionableStateLocalVariable.add(_positionableStateLocalVariableTextField);
        
        _positionableStateFormulaTextField = new JTextField();
        _positionableStateFormulaTextField.setColumns(30);
        _panelPositionableStateFormula.add(_positionableStateFormulaTextField);
        
        
        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPanePositionable.setSelectedComponent(_panelPositionableDirect); break;
                case Reference: _tabbedPanePositionable.setSelectedComponent(_panelPositionableReference); break;
                case LocalVariable: _tabbedPanePositionable.setSelectedComponent(_panelPositionableLocalVariable); break;
                case Formula: _tabbedPanePositionable.setSelectedComponent(_panelPositionableFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getPositionableName() != null) {
                _positionableComboBox.setSelectedItem(action.getPositionableName());
            } else {
                _positionableComboBox.setSelectedItem("");
            }
            _positionableReferenceTextField.setText(action.getReference());
            _positionableLocalVariableTextField.setText(action.getLocalVariable());
            _positionableFormulaTextField.setText(action.getFormula());
            
            switch (action.getStateAddressing()) {
                case Direct: _tabbedPanePositionableState.setSelectedComponent(_panelPositionableStateDirect); break;
                case Reference: _tabbedPanePositionableState.setSelectedComponent(_panelPositionableStateReference); break;
                case LocalVariable: _tabbedPanePositionableState.setSelectedComponent(_panelPositionableStateLocalVariable); break;
                case Formula: _tabbedPanePositionableState.setSelectedComponent(_panelPositionableStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _isControllingComboBox.setSelectedItem(action.getIsControlling());
            _positionableStateReferenceTextField.setText(action.getStateReference());
            _positionableStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _positionableStateFormulaTextField.setText(action.getStateFormula());
        }
        
        JComponent[] components = new JComponent[]{
            _editorComboBox,
            _tabbedPanePositionable,
            _tabbedPanePositionableState};
        
        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionEnableDisable_Components"), components);
        
        for (JComponent c : componentList) panel.add(c);
    }
    
    private void updatePositionables(ActionEvent e) {
        _positionableComboBox.removeAll();
        if (_editorComboBox.getSelectedIndex() == -1) return;
        
        EditorItem item = _editorComboBox.getItemAt(_editorComboBox.getSelectedIndex());
        for (Positionable positionable : item._editor.getContents()) {
            if (positionable.getId() != null) {
                _positionableComboBox.addItem(positionable.getId());
            }
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionEnableDisable action = new ActionEnableDisable("IQDA1", null);
        
        try {
            if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableReference) {
                action.setReference(_positionableReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            if (_tabbedPanePositionableState.getSelectedComponent() == _panelPositionableStateReference) {
                action.setStateReference(_positionableStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }
        
        try {
            action.setFormula(_positionableFormulaTextField.getText());
            if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableFormula) {
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
        ActionEnableDisable action = new ActionEnableDisable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }
    
    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionEnableDisable)) {
            throw new IllegalArgumentException("object must be an ActionEnableDisable but is a: "+object.getClass().getName());
        }
        ActionEnableDisable action = (ActionEnableDisable)object;
        action.setEditor(_editorComboBox.getItemAt(_editorComboBox.getSelectedIndex())._editor.getName());
        if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableDirect) {
            action.setPositionable(_positionableComboBox.getItemAt(_positionableComboBox.getSelectedIndex()));
        }
        try {
            if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_positionableReferenceTextField.getText());
            } else if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_positionableLocalVariableTextField.getText());
            } else if (_tabbedPanePositionable.getSelectedComponent() == _panelPositionableFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_positionableFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPanePositionable has unknown selection");
            }
            
            if (_tabbedPanePositionableState.getSelectedComponent() == _panelPositionableStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
                action.setIsControlling(_isControllingComboBox.getItemAt(_isControllingComboBox.getSelectedIndex()));
            } else if (_tabbedPanePositionableState.getSelectedComponent() == _panelPositionableStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
                action.setStateReference(_positionableStateReferenceTextField.getText());
            } else if (_tabbedPanePositionableState.getSelectedComponent() == _panelPositionableStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
                action.setStateLocalVariable(_positionableStateLocalVariableTextField.getText());
            } else if (_tabbedPanePositionableState.getSelectedComponent() == _panelPositionableStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
                action.setStateFormula(_positionableStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPanePositionableState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionEnableDisable_Short");
    }
    
    @Override
    public void dispose() {
        // Do nothing
    }
    
    
    private static class EditorItem {
        
        private final Editor _editor;
        
        public EditorItem(Editor editor) {
            this._editor = editor;
        }
        
        @Override
        public String toString() {
            return _editor.getName();
        }
    }
    
    
//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEnableDisableSwing.class);
    
}
