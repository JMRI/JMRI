package jmri.jmrit.display.logixng.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.*;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.display.logixng.WindowManagement;
import jmri.jmrit.display.logixng.WindowManagement.HideOrShow;
import jmri.jmrit.display.logixng.WindowManagement.MaximizeMinimizeNormalize;
import jmri.jmrit.display.logixng.WindowManagement.BringToFrontOrBack;
import jmri.jmrit.logixng.actions.swing.AbstractDigitalActionSwing;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectEnumSwing;
import jmri.util.JmriJFrame;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an WindowManagement object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright (C) 2024
 */
public class WindowManagementSwing extends AbstractDigitalActionSwing {

    private JComboBox<JmriJFrameItem> _jmriJFrameComboBox;
    private JTabbedPane _tabbedPaneJmriJFrame;
    private JPanel _panelJmriJFrameDirect;
    private JPanel _panelJmriJFrameReference;
    private JPanel _panelJmriJFrameLocalVariable;
    private JPanel _panelJmriJFrameFormula;
    private JTextField _jmriJFrameReferenceTextField;
    private JTextField _jmriJFrameLocalVariableTextField;
    private JTextField _jmriJFrameFormulaTextField;
    private JCheckBox _ignoreWindowNotFoundCheckBox;

    private LogixNG_SelectEnumSwing<HideOrShow> _selectEnumHideOrShowSwing;
    private LogixNG_SelectEnumSwing<MaximizeMinimizeNormalize> _selectEnumMaximizeMinimizeNormalizeSwing;
    private LogixNG_SelectEnumSwing<BringToFrontOrBack> _selectEnumBringToFrontOrBackSwing;


    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        WindowManagement action = (WindowManagement)object;

        _selectEnumHideOrShowSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectEnumMaximizeMinimizeNormalizeSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);
        _selectEnumBringToFrontOrBackSwing = new LogixNG_SelectEnumSwing<>(getJDialog(), this);

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


        _ignoreWindowNotFoundCheckBox = new JCheckBox(Bundle.getMessage("WindowManagement_IgnoreWindowNotFound"));


        JPanel panelHideOrShow;
        JPanel panelMaximizeMinimizeNormalize;
        JPanel panelBringToFrontOrBack;
        if (action != null) {
            panelHideOrShow = _selectEnumHideOrShowSwing.createPanel(
                    action.getSelectEnumHideOrShow(), HideOrShow.values());
            panelMaximizeMinimizeNormalize = _selectEnumMaximizeMinimizeNormalizeSwing
                    .createPanel(action.getSelectEnumMaximizeMinimizeNormalize(),
                            MaximizeMinimizeNormalize.values());
            panelBringToFrontOrBack = _selectEnumBringToFrontOrBackSwing
                    .createPanel(action.getSelectEnumBringToFrontOrBack(),
                            BringToFrontOrBack.values());
        } else {
            panelHideOrShow = _selectEnumHideOrShowSwing
                    .createPanel(null, HideOrShow.values());
            panelMaximizeMinimizeNormalize = _selectEnumMaximizeMinimizeNormalizeSwing
                    .createPanel(null, MaximizeMinimizeNormalize.values());
            panelBringToFrontOrBack = _selectEnumBringToFrontOrBackSwing
                    .createPanel(null, BringToFrontOrBack.values());
        }

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
            _ignoreWindowNotFoundCheckBox.setSelected(action.isIgnoreWindowNotFound());
        }

        panel.setLayout(new GridBagLayout());
        GridBagConstraints constraint = new GridBagConstraints();
        constraint.gridwidth = 1;
        constraint.gridheight = 1;
        constraint.gridx = 0;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(Bundle.getMessage("WindowToFrontSwing_Window")), constraint);
        constraint.gridy = 4;
        panel.add(new JLabel(Bundle.getMessage("WindowToFrontSwing_HideOrShow")), constraint);
        constraint.gridy = 5;
        panel.add(new JLabel(Bundle.getMessage("WindowToFrontSwing_MaximizeMinimizeNormalize")), constraint);
        constraint.gridy = 6;
        panel.add(new JLabel(Bundle.getMessage("WindowToFrontSwing_BringToFrontOrBack")), constraint);

        constraint.gridx = 1;
        constraint.gridy = 0;
        constraint.anchor = GridBagConstraints.WEST;
        panel.add(_tabbedPaneJmriJFrame, constraint);
        constraint.gridy = 1;
        panel.add(Box.createVerticalStrut(3), constraint);
        constraint.gridy = 2;
        panel.add(_ignoreWindowNotFoundCheckBox, constraint);
        constraint.gridy = 3;
        panel.add(Box.createVerticalStrut(5), constraint);
        constraint.gridy = 4;
        panel.add(panelHideOrShow, constraint);
        constraint.gridy = 5;
        panel.add(panelMaximizeMinimizeNormalize, constraint);
        constraint.gridy = 6;
        panel.add(panelBringToFrontOrBack, constraint);
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        WindowManagement action = new WindowManagement("IQDA1", null);

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

        _selectEnumHideOrShowSwing.validate(action.getSelectEnumHideOrShow(), errorMessages);
        _selectEnumMaximizeMinimizeNormalizeSwing.validate(action.getSelectEnumMaximizeMinimizeNormalize(), errorMessages);
        _selectEnumBringToFrontOrBackSwing.validate(action.getSelectEnumBringToFrontOrBack(), errorMessages);

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        WindowManagement action = new WindowManagement(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (! (object instanceof WindowManagement)) {
            throw new IllegalArgumentException("object must be an WindowToFront but is a: "+object.getClass().getName());
        }
        WindowManagement action = (WindowManagement)object;
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

        action.setIgnoreWindowNotFound(_ignoreWindowNotFoundCheckBox.isSelected());

        _selectEnumHideOrShowSwing.updateObject(action.getSelectEnumHideOrShow());

        _selectEnumMaximizeMinimizeNormalizeSwing.updateObject(
                action.getSelectEnumMaximizeMinimizeNormalize());

        _selectEnumBringToFrontOrBackSwing.updateObject(
                action.getSelectEnumBringToFrontOrBack());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("WindowManagement_Short");
    }

    @Override
    public void dispose() {
        _selectEnumHideOrShowSwing.dispose();
        _selectEnumMaximizeMinimizeNormalizeSwing.dispose();
        _selectEnumBringToFrontOrBackSwing.dispose();
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
