package jmri.jmrit.display.logixng.swing;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.AudioIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.logixng.*;
import jmri.jmrit.display.logixng.ActionAudioIcon;
import jmri.jmrit.display.logixng.ActionAudioIcon.Operation;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionAudioIcon object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2023
 */
public class ActionAudioIconSwing extends AbstractDigitalActionSwing {

    private String _selectedEditor;

    private JComboBox<EditorItem> _editorComboBox;
    private JTabbedPane _tabbedPaneAudioIcon;
    private JComboBox<String> _positionableComboBox;
    private JPanel _panelAudioIconDirect;
    private JPanel _panelAudioIconReference;
    private JPanel _panelAudioIconLocalVariable;
    private JPanel _panelAudioIconFormula;
    private JTextField _positionableReferenceTextField;
    private JTextField _positionableLocalVariableTextField;
    private JTextField _positionableFormulaTextField;

    private JTabbedPane _tabbedPaneAudioIconState;
    private JComboBox<Operation> _isControllingComboBox;
    private JPanel _panelAudioIconStateDirect;
    private JPanel _panelAudioIconStateReference;
    private JPanel _panelAudioIconStateLocalVariable;
    private JPanel _panelAudioIconStateFormula;
    private JTextField _positionableStateReferenceTextField;
    private JTextField _positionableStateLocalVariableTextField;
    private JTextField _positionableStateFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        ActionAudioIcon action = (ActionAudioIcon)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        _selectedEditor = action != null ? action.getEditorName() : null;

        JPanel innerPanel = new JPanel();

        _editorComboBox = new JComboBox<>();
        JComboBoxUtil.setupComboBoxMaxRows(_editorComboBox);
        for (Editor editor : jmri.InstanceManager.getDefault(EditorManager.class).getAll()) {
            EditorItem item = new EditorItem(editor);
            _editorComboBox.addItem(item);
            if (editor.getName().equals(_selectedEditor)) _editorComboBox.setSelectedItem(item);
        }
        _editorComboBox.addActionListener(this::updateAudioIcons);

        _tabbedPaneAudioIcon = new JTabbedPane();
        _panelAudioIconDirect = new javax.swing.JPanel();
        _panelAudioIconReference = new javax.swing.JPanel();
        _panelAudioIconLocalVariable = new javax.swing.JPanel();
        _panelAudioIconFormula = new javax.swing.JPanel();

        _tabbedPaneAudioIcon.addTab(NamedBeanAddressing.Direct.toString(), _panelAudioIconDirect);
        _tabbedPaneAudioIcon.addTab(NamedBeanAddressing.Reference.toString(), _panelAudioIconReference);
        _tabbedPaneAudioIcon.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAudioIconLocalVariable);
        _tabbedPaneAudioIcon.addTab(NamedBeanAddressing.Formula.toString(), _panelAudioIconFormula);

        _positionableComboBox = new JComboBox<>();
        JComboBoxUtil.setupComboBoxMaxRows(_positionableComboBox);
        updateAudioIcons(null);
        _panelAudioIconDirect.add(_positionableComboBox);

        _positionableReferenceTextField = new JTextField();
        _positionableReferenceTextField.setColumns(30);
        _panelAudioIconReference.add(_positionableReferenceTextField);

        _positionableLocalVariableTextField = new JTextField();
        _positionableLocalVariableTextField.setColumns(30);
        _panelAudioIconLocalVariable.add(_positionableLocalVariableTextField);

        _positionableFormulaTextField = new JTextField();
        _positionableFormulaTextField.setColumns(30);
        _panelAudioIconFormula.add(_positionableFormulaTextField);


        _tabbedPaneAudioIconState = new JTabbedPane();
        _panelAudioIconStateDirect = new javax.swing.JPanel();
        _panelAudioIconStateReference = new javax.swing.JPanel();
        _panelAudioIconStateLocalVariable = new javax.swing.JPanel();
        _panelAudioIconStateFormula = new javax.swing.JPanel();

        _tabbedPaneAudioIconState.addTab(NamedBeanAddressing.Direct.toString(), _panelAudioIconStateDirect);
        _tabbedPaneAudioIconState.addTab(NamedBeanAddressing.Reference.toString(), _panelAudioIconStateReference);
        _tabbedPaneAudioIconState.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelAudioIconStateLocalVariable);
        _tabbedPaneAudioIconState.addTab(NamedBeanAddressing.Formula.toString(), _panelAudioIconStateFormula);

        _isControllingComboBox = new JComboBox<>();
        for (Operation e : Operation.values()) {
            _isControllingComboBox.addItem(e);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_isControllingComboBox);

        _panelAudioIconStateDirect.add(_isControllingComboBox);

        _positionableStateReferenceTextField = new JTextField();
        _positionableStateReferenceTextField.setColumns(30);
        _panelAudioIconStateReference.add(_positionableStateReferenceTextField);

        _positionableStateLocalVariableTextField = new JTextField();
        _positionableStateLocalVariableTextField.setColumns(30);
        _panelAudioIconStateLocalVariable.add(_positionableStateLocalVariableTextField);

        _positionableStateFormulaTextField = new JTextField();
        _positionableStateFormulaTextField.setColumns(30);
        _panelAudioIconStateFormula.add(_positionableStateFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneAudioIcon.setSelectedComponent(_panelAudioIconDirect); break;
                case Reference: _tabbedPaneAudioIcon.setSelectedComponent(_panelAudioIconReference); break;
                case LocalVariable: _tabbedPaneAudioIcon.setSelectedComponent(_panelAudioIconLocalVariable); break;
                case Formula: _tabbedPaneAudioIcon.setSelectedComponent(_panelAudioIconFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            if (action.getAudioIconName() != null) {
                _positionableComboBox.setSelectedItem(action.getAudioIconName());
            }
            _positionableReferenceTextField.setText(action.getReference());
            _positionableLocalVariableTextField.setText(action.getLocalVariable());
            _positionableFormulaTextField.setText(action.getFormula());

            switch (action.getStateAddressing()) {
                case Direct: _tabbedPaneAudioIconState.setSelectedComponent(_panelAudioIconStateDirect); break;
                case Reference: _tabbedPaneAudioIconState.setSelectedComponent(_panelAudioIconStateReference); break;
                case LocalVariable: _tabbedPaneAudioIconState.setSelectedComponent(_panelAudioIconStateLocalVariable); break;
                case Formula: _tabbedPaneAudioIconState.setSelectedComponent(_panelAudioIconStateFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _isControllingComboBox.setSelectedItem(action.getOperation());
            _positionableStateReferenceTextField.setText(action.getStateReference());
            _positionableStateLocalVariableTextField.setText(action.getStateLocalVariable());
            _positionableStateFormulaTextField.setText(action.getStateFormula());
        }

        JComponent[] components = new JComponent[]{
            _editorComboBox,
            _tabbedPaneAudioIcon,
            _tabbedPaneAudioIconState};

        List<JComponent> componentList = SwingConfiguratorInterface.parseMessage(
                Bundle.getMessage("ActionAudioIcon_Components"), components);

        for (JComponent c : componentList) innerPanel.add(c);

        panel.add(innerPanel);

        JPanel labelPanel = new JPanel();
        JLabel label = new JLabel(Bundle.getMessage("ActionAudioIcon_Info"));
        label.setBorder(new javax.swing.border.CompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                BorderFactory.createEmptyBorder(3, 3, 3, 3)));
        labelPanel.add(label);
        panel.add(labelPanel);
    }

    private void updateAudioIcons(ActionEvent e) {
        _positionableComboBox.removeAllItems();
        if (_editorComboBox.getSelectedIndex() == -1) return;

        EditorItem item = _editorComboBox.getItemAt(_editorComboBox.getSelectedIndex());
        List<String> list = new ArrayList<>();
        for (Positionable positionable : item._editor.getContents()) {
            if ((positionable.getId() != null) && (positionable instanceof AudioIcon)) {
                list.add(positionable.getId());
            }
        }
        Collections.sort(list);
        for (String s : list) {
            _positionableComboBox.addItem(s);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        ActionAudioIcon action = new ActionAudioIcon("IQDA1", null);

        try {
            if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconReference) {
                action.setReference(_positionableReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            if (_tabbedPaneAudioIconState.getSelectedComponent() == _panelAudioIconStateReference) {
                action.setStateReference(_positionableStateReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_positionableFormulaTextField.getText());
            if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
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
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionAudioIcon action = new ActionAudioIcon(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof ActionAudioIcon)) {
            throw new IllegalArgumentException("object must be an ActionAudioIcon but is a: "+object.getClass().getName());
        }
        ActionAudioIcon action = (ActionAudioIcon)object;
        if (_editorComboBox.getSelectedIndex() != -1) {
            action.setEditor(_editorComboBox.getItemAt(_editorComboBox.getSelectedIndex())._editor.getName());
        }
        if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconDirect) {
            action.setAudioIcon(_positionableComboBox.getItemAt(_positionableComboBox.getSelectedIndex()));
        }
        try {
            if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_positionableReferenceTextField.getText());
            } else if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_positionableLocalVariableTextField.getText());
            } else if (_tabbedPaneAudioIcon.getSelectedComponent() == _panelAudioIconFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_positionableFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAudioIcon has unknown selection");
            }

            if (_tabbedPaneAudioIconState.getSelectedComponent() == _panelAudioIconStateDirect) {
                action.setStateAddressing(NamedBeanAddressing.Direct);
                action.setOperation(_isControllingComboBox.getItemAt(_isControllingComboBox.getSelectedIndex()));
            } else if (_tabbedPaneAudioIconState.getSelectedComponent() == _panelAudioIconStateReference) {
                action.setStateAddressing(NamedBeanAddressing.Reference);
                action.setStateReference(_positionableStateReferenceTextField.getText());
            } else if (_tabbedPaneAudioIconState.getSelectedComponent() == _panelAudioIconStateLocalVariable) {
                action.setStateAddressing(NamedBeanAddressing.LocalVariable);
                action.setStateLocalVariable(_positionableStateLocalVariableTextField.getText());
            } else if (_tabbedPaneAudioIconState.getSelectedComponent() == _panelAudioIconStateFormula) {
                action.setStateAddressing(NamedBeanAddressing.Formula);
                action.setStateFormula(_positionableStateFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneAudioIconState has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionAudioIcon_Short");
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


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionAudioIconSwing.class);

}
