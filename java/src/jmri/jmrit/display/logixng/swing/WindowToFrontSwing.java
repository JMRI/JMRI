package jmri.jmrit.display.logixng.swing;

import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.display.logixng.WindowToFront;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an WindowToFront object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class WindowToFrontSwing extends AbstractDigitalActionSwing {

    private JComboBox<JmriJFrameItem> _jmriJFrameComboBox;
    private JTabbedPane _tabbedPaneJmriJFrame;
    private JPanel _panelJmriJFrameDirect;
    private JPanel _panelJmriJFrameReference;
    private JPanel _panelJmriJFrameLocalVariable;
    private JPanel _panelJmriJFrameFormula;
    private JTextField _jmriJFrameReferenceTextField;
    private JTextField _jmriJFrameLocalVariableTextField;
    private JTextField _jmriJFrameFormulaTextField;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        WindowToFront action = (WindowToFront)object;

        panel = new JPanel();

        String _selectedJmriJFrameTitle = action != null ? action.getJmriJFrameTitle() : null;

        _tabbedPaneJmriJFrame = new JTabbedPane();
        _panelJmriJFrameDirect = new javax.swing.JPanel();
        _panelJmriJFrameReference = new javax.swing.JPanel();
        _panelJmriJFrameLocalVariable = new javax.swing.JPanel();
        _panelJmriJFrameFormula = new javax.swing.JPanel();

        _tabbedPaneJmriJFrame.addTab(NamedBeanAddressing.Direct.toString(), _panelJmriJFrameDirect);
        _tabbedPaneJmriJFrame.addTab(NamedBeanAddressing.Reference.toString(), _panelJmriJFrameReference);
        _tabbedPaneJmriJFrame.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelJmriJFrameLocalVariable);
        _tabbedPaneJmriJFrame.addTab(NamedBeanAddressing.Formula.toString(), _panelJmriJFrameFormula);

        _jmriJFrameComboBox = new JComboBox<>();
        JComboBoxUtil.setupComboBoxMaxRows(_jmriJFrameComboBox);
        for (JmriJFrame jmriJFrame : JmriJFrame.getFrameList()) {
            JmriJFrameItem item = new JmriJFrameItem(jmriJFrame);
            _jmriJFrameComboBox.addItem(item);
            if (jmriJFrame.getTitle().equals(_selectedJmriJFrameTitle)) {
                _jmriJFrameComboBox.setSelectedItem(item);
            }
        }
        _panelJmriJFrameDirect.add(_jmriJFrameComboBox);

        _jmriJFrameReferenceTextField = new JTextField();
        _jmriJFrameReferenceTextField.setColumns(30);
        _panelJmriJFrameReference.add(_jmriJFrameReferenceTextField);

        _jmriJFrameLocalVariableTextField = new JTextField();
        _jmriJFrameLocalVariableTextField.setColumns(30);
        _panelJmriJFrameLocalVariable.add(_jmriJFrameLocalVariableTextField);

        _jmriJFrameFormulaTextField = new JTextField();
        _jmriJFrameFormulaTextField.setColumns(30);
        _panelJmriJFrameFormula.add(_jmriJFrameFormulaTextField);


        if (action != null) {
            switch (action.getAddressing()) {
                case Direct: _tabbedPaneJmriJFrame.setSelectedComponent(_panelJmriJFrameDirect); break;
                case Reference: _tabbedPaneJmriJFrame.setSelectedComponent(_panelJmriJFrameReference); break;
                case LocalVariable: _tabbedPaneJmriJFrame.setSelectedComponent(_panelJmriJFrameLocalVariable); break;
                case Formula: _tabbedPaneJmriJFrame.setSelectedComponent(_panelJmriJFrameFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            _jmriJFrameReferenceTextField.setText(action.getReference());
            _jmriJFrameLocalVariableTextField.setText(action.getLocalVariable());
            _jmriJFrameFormulaTextField.setText(action.getFormula());
        }

        panel.add(_tabbedPaneJmriJFrame);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        WindowToFront action = new WindowToFront("IQDA1", null);

        try {
            if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameReference) {
                action.setReference(_jmriJFrameReferenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            action.setFormula(_jmriJFrameFormulaTextField.getText());
            if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameFormula) {
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
        WindowToFront action = new WindowToFront(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof WindowToFront)) {
            throw new IllegalArgumentException("object must be an WindowToFront but is a: "+object.getClass().getName());
        }
        WindowToFront action = (WindowToFront)object;
        if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameDirect) {
            if (_jmriJFrameComboBox.getSelectedIndex() != -1) {
                action.setJmriJFrame(_jmriJFrameComboBox.getItemAt(_jmriJFrameComboBox.getSelectedIndex())._frame);
            }
        }
        try {
            if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameDirect) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setReference(_jmriJFrameReferenceTextField.getText());
            } else if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setLocalVariable(_jmriJFrameLocalVariableTextField.getText());
            } else if (_tabbedPaneJmriJFrame.getSelectedComponent() == _panelJmriJFrameFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setFormula(_jmriJFrameFormulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneJmriJFrame has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("WindowToFront_Short");
    }

    @Override
    public void dispose() {
        // Do nothing
    }



    private static class JmriJFrameItem {

        private final JmriJFrame _frame;

        public JmriJFrameItem(JmriJFrame frame) {
            this._frame = frame;
        }

        @Override
        public String toString() {
            return _frame.getTitle();
        }
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WindowToFrontSwing.class);

}
