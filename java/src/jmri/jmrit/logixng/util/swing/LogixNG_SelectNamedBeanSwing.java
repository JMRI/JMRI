package jmri.jmrit.logixng.util.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectNamedBean.
 *
 * @param <E> the type of the named bean
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectNamedBeanSwing<E extends NamedBean> {

    private final @Nonnull Manager<E> _manager;

    private JTabbedPane _tabbedPane;
    private BeanSelectPanel<E> _namedBeanPanel;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JTextField _referenceTextField;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;


    public LogixNG_SelectNamedBeanSwing(@Nonnull Manager<E> manager) {
        _manager = manager;
    }

    public JPanel createPanel(
            @CheckForNull LogixNG_SelectNamedBean<E> selectNamedBean) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);

        _namedBeanPanel = new BeanSelectPanel<E>(_manager, null);
        _panelDirect.add(_namedBeanPanel);

        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(30);
        _panelReference.add(_referenceTextField);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        if (selectNamedBean != null) {
            switch (selectNamedBean.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case Reference: _tabbedPane.setSelectedComponent(_panelReference); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectNamedBean.getAddressing().name());
            }
            if (selectNamedBean.getNamedBean() != null) {
                _namedBeanPanel.setDefaultNamedBean(selectNamedBean.getNamedBean().getBean());
            }
            _referenceTextField.setText(selectNamedBean.getReference());
            _localVariableTextField.setText(selectNamedBean.getLocalVariable());
            _formulaTextField.setText(selectNamedBean.getFormula());
        }

        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectNamedBean<E> selectNamedBean,
            @Nonnull List<String> errorMessages) {
        try {
            if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectNamedBean.setReference(_referenceTextField.getText());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add(e.getMessage());
            return false;
        }

        try {
            selectNamedBean.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectNamedBean.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectNamedBean.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectNamedBean.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectNamedBean.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        return true;
    }

    public void updateObject(@Nonnull LogixNG_SelectNamedBean<E> selectNamedBean) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            E namedBean = _namedBeanPanel.getNamedBean();
            if (namedBean != null) {
                NamedBeanHandle<E> handle
                        = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                .getNamedBeanHandle(namedBean.getDisplayName(), namedBean);
                selectNamedBean.setNamedBean(handle);
            } else {
                selectNamedBean.removeNamedBean();
            }
        } else {
            selectNamedBean.removeNamedBean();
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectNamedBean.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
                selectNamedBean.setAddressing(NamedBeanAddressing.Reference);
                selectNamedBean.setReference(_referenceTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectNamedBean.setAddressing(NamedBeanAddressing.LocalVariable);
                selectNamedBean.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectNamedBean.setAddressing(NamedBeanAddressing.Formula);
                selectNamedBean.setFormula(_formulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneNamedBean has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    public void dispose() {
        if (_namedBeanPanel != null) {
            _namedBeanPanel.dispose();
        }
    }

}
