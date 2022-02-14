package jmri.jmrit.logixng.swing;

import java.awt.*;
import java.awt.event.ActionEvent;

import javax.swing.*;

import jmri.NamedBean;
import jmri.jmrit.logixng.NamedBeanAddressing;
import jmri.util.JmriJFrame;
import jmri.util.swing.BeanSelectPanel;

/**
 * A dialog to enter data.
 * In LogixNG, the user can in many cases enter data in several ways.
 * For example, a turnout can be entered directly, by using a reference,
 * a local variable or a formula. This dialog presents a dialog for that.
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_DataDialog {

    private final SwingConfiguratorInterface _swi;
    private JmriJFrame _showDialogFrame;

    private Runnable _runOnOk;
    private JTabbedPane _tabbedPane;
    private BeanSelectPanel<? extends NamedBean> _beanPanel;
    private JComboBox<? extends Object> _beanComboBox;
    private JTextField _beanTextField;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JTextField _beanReferenceTextField;
    private JTextField _beanLocalVariableTextField;
    private JTextField _beanFormulaTextField;



    public LogixNG_DataDialog(SwingConfiguratorInterface swi) {
        _swi = swi;
    }

    public void showDialog(
            String title,
            NamedBeanAddressing addressing,
            BeanSelectPanel<? extends NamedBean> beanPanel,
            JTextField referenceTextField,
            JTextField localVariableTextField,
            JTextField formulaTextField,
            Runnable runOnOk) {
        if (checkOpenDialog()) {
            return;
        }

        _beanPanel = beanPanel;
        _beanComboBox = null;
        _beanTextField = null;

        showDialog(title, addressing, referenceTextField, localVariableTextField, formulaTextField, runOnOk);
    }

    public void showDialog(
            String title,
            NamedBeanAddressing addressing,
            JComboBox<? extends Object> comboBox,
            JTextField referenceTextField,
            JTextField localVariableTextField,
            JTextField formulaTextField,
            Runnable runOnOk) {
        if (checkOpenDialog()) {
            return;
        }

        _beanPanel = null;
        _beanComboBox = comboBox;
        _beanTextField = null;

        showDialog(title, addressing, referenceTextField, localVariableTextField, formulaTextField, runOnOk);
    }

    public void showDialog(
            String title,
            NamedBeanAddressing addressing,
            JTextField textField,
            JTextField referenceTextField,
            JTextField localVariableTextField,
            JTextField formulaTextField,
            Runnable runOnOk) {
        if (checkOpenDialog()) {
            return;
        }

        _beanPanel = null;
        _beanComboBox = null;
        _beanTextField = textField;
        showDialog(title, addressing, referenceTextField, localVariableTextField, formulaTextField, runOnOk);
    }

    private void showDialog(
            String title,
            NamedBeanAddressing addressing,
            JTextField referenceTextField,
            JTextField localVariableTextField,
            JTextField formulaTextField,
            Runnable runOnOk) {

        _beanReferenceTextField = referenceTextField;
        _beanLocalVariableTextField = localVariableTextField;
        _beanFormulaTextField = formulaTextField;

        _runOnOk = runOnOk;

//        _showDialogFrame = new JmriJFrame(Bundle.getMessage(titleId));
        _showDialogFrame = new JmriJFrame(title);
//        _showDialogFrame.addHelpMenu(
//                "package.jmri.jmrit.beantable.LogixNGTable", true);     // NOI18N
        _showDialogFrame.setLocation(
                (Toolkit.getDefaultToolkit().getScreenSize().width) / 2 - _showDialogFrame.getWidth() / 2,
                (Toolkit.getDefaultToolkit().getScreenSize().height) / 2 - _showDialogFrame.getHeight() / 2);
        Container contentPane = _showDialogFrame.getContentPane();
        contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);

        switch (addressing) {
            case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
            case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
            case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
            case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
            default: throw new IllegalArgumentException("invalid addressing: " + addressing.name());
        }

        if (_beanPanel != null) _panelDirect.add(_beanPanel);
        else if (_beanComboBox != null) _panelDirect.add(_beanComboBox);
        else if (_beanTextField != null) _panelDirect.add(_beanTextField);

//        _beanReferenceTextField = new JTextField();
//        _beanReferenceTextField.setColumns(30);
        _panelReference.add(_beanReferenceTextField);

//        _beanLocalVariableTextField = new JTextField();
//        _beanLocalVariableTextField.setColumns(30);
        _panelLocalVariable.add(_beanLocalVariableTextField);

//        _beanFormulaTextField = new JTextField();
//        _beanFormulaTextField.setColumns(30);
        _panelFormula.add(_beanFormulaTextField);

        // set up create and cancel buttons
        JPanel panel5 = new JPanel();
        panel5.setLayout(new FlowLayout());
        // Cancel
        JButton cancel = new JButton(Bundle.getMessage("ButtonCancel"));    // NOI18N
        panel5.add(cancel);
        cancel.addActionListener(this::cancelDialogPressed);
//        cancel.setToolTipText(Bundle.getMessage("CancelLogixNGButtonHint"));      // NOI18N

        JButton okButton = new JButton(Bundle.getMessage("ButtonOK"));  // NOI18N
        panel5.add(okButton);
        okButton.addActionListener((ActionEvent e) -> {
            _showDialogFrame.setVisible(false);
            _showDialogFrame.dispose();
            _showDialogFrame = null;
            if (_swi.getJDialog() != null) {
                _swi.getJDialog().setVisible(true);
            }
            _runOnOk.run();
        });

        _showDialogFrame.add(_tabbedPane);

        _showDialogFrame.add(panel5);

        _showDialogFrame.pack();
        _showDialogFrame.setVisible(true);
    }

    public NamedBeanAddressing getAddressing() {
        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return NamedBeanAddressing.Direct;
        } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
            return NamedBeanAddressing.Reference;
        } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
            return NamedBeanAddressing.LocalVariable;
        } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
            return NamedBeanAddressing.Formula;
        } else {
            throw new IllegalArgumentException("_tabbedPane has unknown selection");
        }
    }

    public boolean checkOpenDialog() {
        if (_showDialogFrame != null) {
            _showDialogFrame.requestFocus();
            return true;
        }
        return false;
    }

    /**
     * Respond to the Cancel button in Add bean window.
     * <p>
     * Note: Also get there if the user closes the Add bean window.
     *
     * @param e The event heard
     */
    void cancelDialogPressed(ActionEvent e) {
        _showDialogFrame.setVisible(false);
        _showDialogFrame.dispose();
        _showDialogFrame = null;
        if (_swi.getJDialog() != null) {
            _swi.getJDialog().setVisible(true);
        }
    }

    public void dispose() {
        if (_showDialogFrame != null) {
            _showDialogFrame.setVisible(false);
            _showDialogFrame.dispose();
        }
    }

}
