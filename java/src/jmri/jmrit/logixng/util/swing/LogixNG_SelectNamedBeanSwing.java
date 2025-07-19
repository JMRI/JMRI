package jmri.jmrit.logixng.util.swing;

import java.util.List;
import java.util.function.Predicate;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
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
    private final JDialog _dialog;
    private final LogixNG_SelectTableSwing _selectTableSwing;

    private boolean onlyDirectAddressing = false;

    private JTabbedPane _tabbedPane;
    private BeanSelectPanel<E> _namedBeanPanel;
    private JPanel _panelDirect;
    private JPanel _panelReference;
    private JPanel _panelMemory;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JPanel _panelTable;
    private JTextField _referenceTextField;
    private BeanSelectPanel<Memory> _memoryPanel;
    private JCheckBox _listenToMemoryCheckBox;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;


    public LogixNG_SelectNamedBeanSwing(
            @Nonnull Manager<E> manager,
            @Nonnull JDialog dialog,
            @Nonnull SwingConfiguratorInterface swi) {
        _manager = manager;
        _dialog = dialog;
        _selectTableSwing = new LogixNG_SelectTableSwing(_dialog, swi);
    }

    public JPanel createPanel(
            @CheckForNull LogixNG_SelectNamedBean<E> selectNamedBean) {
        return createPanel(selectNamedBean, null);
    }

    public JPanel createPanel(
            @CheckForNull LogixNG_SelectNamedBean<E> selectNamedBean,
            @CheckForNull Predicate<E> filter) {

        onlyDirectAddressing = selectNamedBean != null
                && selectNamedBean.getOnlyDirectAddressingAllowed();

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelMemory = new JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectNamedBean != null) {
            _panelTable = _selectTableSwing.createPanel(selectNamedBean.getSelectTable());
        } else {
            _panelTable = _selectTableSwing.createPanel(null);
        }

        _memoryPanel = new BeanSelectPanel<>(InstanceManager.getDefault(MemoryManager.class), null);
        _listenToMemoryCheckBox = new JCheckBox(Bundle.getMessage("ListenToMemory"));

        _panelMemory.setLayout(new BoxLayout(_panelMemory, BoxLayout.Y_AXIS));
        _panelMemory.add(_memoryPanel);
        _panelMemory.add(_listenToMemoryCheckBox);

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedPane.addTab(NamedBeanAddressing.Memory.toString(), _panelMemory);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        _tabbedPane.addTab(NamedBeanAddressing.Table.toString(), _panelTable);

        _namedBeanPanel = new BeanSelectPanel<>(_manager, null, NamedBean.DisplayOptions.DISPLAYNAME, true, filter);
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
                case Memory: _tabbedPane.setSelectedComponent(_panelMemory); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                case Table: _tabbedPane.setSelectedComponent(_panelTable); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectNamedBean.getAddressing().name());
            }
            if (selectNamedBean.getNamedBean() != null) {
                _namedBeanPanel.setDefaultNamedBean(selectNamedBean.getNamedBean().getBean());
            }
            _referenceTextField.setText(selectNamedBean.getReference());
            _memoryPanel.setDefaultNamedBean(selectNamedBean.getMemory());
            _listenToMemoryCheckBox.setSelected(selectNamedBean.getListenToMemory());
            _localVariableTextField.setText(selectNamedBean.getLocalVariable());
            _formulaTextField.setText(selectNamedBean.getFormula());
        }

        if (!onlyDirectAddressing) {
            panel.add(_tabbedPane);
        } else {
            panel.add(_panelDirect);
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
            selectNamedBean.setAddressing(getAddressing());
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        _selectTableSwing.validate(selectNamedBean.getSelectTable(), errorMessages);

        return errorMessages.isEmpty();
    }

    public void updateObject(@Nonnull LogixNG_SelectNamedBean<E> selectNamedBean) {

        if (onlyDirectAddressing || _tabbedPane.getSelectedComponent() == _panelDirect) {
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
            NamedBeanAddressing addressing = getAddressing();
            selectNamedBean.setAddressing(addressing);

            switch (addressing) {
                case Direct:
                    // Do nothing
                    break;
                case Reference:
                    selectNamedBean.setReference(_referenceTextField.getText());
                    break;
                case Memory:
                    if (_memoryPanel.getNamedBean() != null) {
                        selectNamedBean.setMemory(_memoryPanel.getNamedBean());
                    }
                    selectNamedBean.setListenToMemory(_listenToMemoryCheckBox.isSelected());
                    break;
                case LocalVariable:
                    selectNamedBean.setLocalVariable(_localVariableTextField.getText());
                    break;
                case Formula:
                    selectNamedBean.setFormula(_formulaTextField.getText());
                    break;
                case Table:
                    // Do nothing
                    break;
                default:
                    throw new IllegalArgumentException("unknown addressing: "+addressing.name());
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }

        _selectTableSwing.updateObject(selectNamedBean.getSelectTable());
    }

    public BeanSelectPanel<E> getBeanSelectPanel() {
        return _namedBeanPanel;
    }

    public NamedBeanAddressing getAddressing() {
        if (onlyDirectAddressing) {
            return NamedBeanAddressing.Direct;
        }

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            return NamedBeanAddressing.Direct;
        } else if (_tabbedPane.getSelectedComponent() == _panelReference) {
            return NamedBeanAddressing.Reference;
        } else if (_tabbedPane.getSelectedComponent() == _panelMemory) {
            return NamedBeanAddressing.Memory;
        } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
            return NamedBeanAddressing.LocalVariable;
        } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
            return NamedBeanAddressing.Formula;
        } else if (_tabbedPane.getSelectedComponent() == _panelTable) {
            return NamedBeanAddressing.Table;
        } else {
            throw new IllegalArgumentException("_tabbedPane has unknown selection");
        }
    }

    public void addAddressingListener(javax.swing.event.ChangeListener l) {
        _tabbedPane.addChangeListener(l);
    }

    public void removeAddressingListener(javax.swing.event.ChangeListener l) {
        _tabbedPane.removeChangeListener(l);
    }

    public E getBean() {
        if (getAddressing() == NamedBeanAddressing.Direct) {
            return _namedBeanPanel.getNamedBean();
        } else {
            throw new IllegalArgumentException("Addressing is not Direct");
        }
    }

    public void dispose() {
        if (_namedBeanPanel != null) {
            _namedBeanPanel.dispose();
        }
        _selectTableSwing.dispose();
    }

}
