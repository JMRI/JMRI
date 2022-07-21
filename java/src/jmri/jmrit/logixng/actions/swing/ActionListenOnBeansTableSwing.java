package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionListenOnBeansTable;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionListenOnBeansTable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionListenOnBeansTableSwing extends AbstractDigitalActionSwing {

    private BeanSelectPanel<NamedTable> _tableBeanPanel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JComboBox<String> _rowOrColumnNameComboBox;
    private JCheckBox _includeCellsWithoutHeaderCheckBox;
    private JComboBox<NamedBeanType> _namedBeanTypeComboBox;
    private JCheckBox _listenOnAllPropertiesCheckBox;
    private JTextField _localVariableNamedBean;
    private JTextField _localVariableEvent;
    private JTextField _localVariableNewValue;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionListenOnBeansTable)) {
            throw new IllegalArgumentException("object must be an ActionListenOnBeansTable but is a: "+object.getClass().getName());
        }

        ActionListenOnBeansTable action = (ActionListenOnBeansTable)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tableBeanPanelPanel = new JPanel();
        tableBeanPanelPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansTableSwing_Table")));
        _tableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        tableBeanPanelPanel.add(_tableBeanPanel);
        panel.add(tableBeanPanelPanel);

        _rowOrColumnNameComboBox = new JComboBox<>();
        _tableRowOrColumnComboBox = new JComboBox<>();
        for (TableRowOrColumn item : TableRowOrColumn.values()) {
            _tableRowOrColumnComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_tableRowOrColumnComboBox);
        _tableRowOrColumnComboBox.addActionListener((evt) -> {
            comboListener(action);
        });
        _tableBeanPanel.getBeanCombo().addActionListener((evt) -> {
            comboListener(action);
        });

        JPanel tableRowOrColumnPanel = new JPanel();
        tableRowOrColumnPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansTableSwing_RowOrColumn")));
        tableRowOrColumnPanel.add(_tableRowOrColumnComboBox);
        panel.add(tableRowOrColumnPanel);

        JPanel rowOrColumnNamePanel = new JPanel();
        rowOrColumnNamePanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansTableSwing_RowOrColumnName")));
        rowOrColumnNamePanel.add(_rowOrColumnNameComboBox);
        panel.add(rowOrColumnNamePanel);
        JComboBoxUtil.setupComboBoxMaxRows(_rowOrColumnNameComboBox);

        JPanel includeCellsWithoutHeaderPanel = new JPanel();
        includeCellsWithoutHeaderPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansTableSwing_IncludeCellsWithoutHeader")));
        _includeCellsWithoutHeaderCheckBox = new JCheckBox();
        includeCellsWithoutHeaderPanel.add(_includeCellsWithoutHeaderCheckBox);
        panel.add(includeCellsWithoutHeaderPanel);

        JPanel namedBeanTypePanel = new JPanel();
        namedBeanTypePanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansTableSwing_NamedBeanType")));
        _namedBeanTypeComboBox = new JComboBox<>();
        for (NamedBeanType item : NamedBeanType.values()) {
            _namedBeanTypeComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_namedBeanTypeComboBox);
        namedBeanTypePanel.add(_namedBeanTypeComboBox);
        panel.add(namedBeanTypePanel);

        JPanel listenOnAllPropertiesPanel = new JPanel();
        listenOnAllPropertiesPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansTableSwing_ListenOnAllPropertiesCheckBox")));
        _listenOnAllPropertiesCheckBox = new JCheckBox();
        listenOnAllPropertiesPanel.add(_listenOnAllPropertiesCheckBox);
        panel.add(listenOnAllPropertiesPanel);

        JPanel localVariableNamedBeanPanel = new JPanel();
        localVariableNamedBeanPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNamedBean")));
        _localVariableNamedBean = new JTextField(20);
        localVariableNamedBeanPanel.add(_localVariableNamedBean);
        panel.add(localVariableNamedBeanPanel);

        JPanel localVariableNamedEventPanel = new JPanel();
        localVariableNamedEventPanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableEvent")));
        _localVariableEvent = new JTextField(20);
        localVariableNamedEventPanel.add(_localVariableEvent);
        panel.add(localVariableNamedEventPanel);

        JPanel localVariableNewValuePanel = new JPanel();
        localVariableNewValuePanel.add(new JLabel(Bundle.getMessage("ActionListenOnBeansSwing_LocalVariableNewValue")));
        _localVariableNewValue = new JTextField(20);
        localVariableNewValuePanel.add(_localVariableNewValue);
        panel.add(localVariableNewValuePanel);

        if (action != null) {
            if (action.getSelectNamedBean().getNamedBean() != null) {
                _tableBeanPanel.setDefaultNamedBean(action.getSelectNamedBean().getNamedBean().getBean());
            }
            _tableRowOrColumnComboBox.setSelectedItem(action.getTableRowOrColumn());
            _includeCellsWithoutHeaderCheckBox.setSelected(action.getIncludeCellsWithoutHeader());
            _namedBeanTypeComboBox.setSelectedItem(action.getNamedBeanType());
            _listenOnAllPropertiesCheckBox.setSelected(action.getListenOnAllProperties());

            _localVariableNamedBean.setText(action.getLocalVariableNamedBean());
            _localVariableEvent.setText(action.getLocalVariableEvent());
            _localVariableNewValue.setText(action.getLocalVariableNewValue());
        }
    }

    private void comboListener( ActionListenOnBeansTable action ) {

        _rowOrColumnNameComboBox.removeAllItems();
        NamedTable table = _tableBeanPanel.getNamedBean();
        if (table != null) {
            if (_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()) == TableRowOrColumn.Column) {
                for (int column=0; column <= table.numColumns(); column++) {
                    // If the header is null or empty, treat the row as a comment
                    Object header = table.getCell(0, column);
                    if ((header != null) && (!header.toString().isEmpty())) {
                        _rowOrColumnNameComboBox.addItem(header.toString());
                    }
                }
            } else {
                for (int row=0; row <= table.numRows(); row++) {
                    // If the header is null or empty, treat the row as a comment
                    Object header = table.getCell(row, 0);
                    if ((header != null) && (!header.toString().isEmpty())) {
                        _rowOrColumnNameComboBox.addItem(header.toString());
                    }
                }
            }
            if (action != null) {
                _rowOrColumnNameComboBox.setSelectedItem(action.getRowOrColumnName());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionListenOnBeansTable action = new ActionListenOnBeansTable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ActionListenOnBeansTable)) {
            throw new IllegalArgumentException("object must be an ActionListenOnBeansTable but is a: "+object.getClass().getName());
        }
        if ( _tableBeanPanel == null ){
            throw new UnsupportedOperationException("No Bean Panel Present ");
        }

        ActionListenOnBeansTable action = (ActionListenOnBeansTable)object;
        NamedTable table = _tableBeanPanel.getNamedBean();
        if (table != null) {
            NamedBeanHandle<NamedTable> handle
                    = InstanceManager.getDefault(NamedBeanHandleManager.class)
                            .getNamedBeanHandle(table.getDisplayName(), table);
            action.getSelectNamedBean().setNamedBean(handle);
        } else {
            action.getSelectNamedBean().removeNamedBean();
        }
        action.setTableRowOrColumn(_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()));
        if (_rowOrColumnNameComboBox.getSelectedIndex() != -1) {
            action.setRowOrColumnName(_rowOrColumnNameComboBox.getItemAt(_rowOrColumnNameComboBox.getSelectedIndex()));
        } else {
            action.setRowOrColumnName("");
        }
        if (_namedBeanTypeComboBox.getSelectedIndex() != -1) {
            action.setNamedBeanType(_namedBeanTypeComboBox.getItemAt(_namedBeanTypeComboBox.getSelectedIndex()));
        }
        action.setIncludeCellsWithoutHeader(_includeCellsWithoutHeaderCheckBox.isSelected());
        action.setListenOnAllProperties(_listenOnAllPropertiesCheckBox.isSelected());

        action.setLocalVariableNamedBean(_localVariableNamedBean.getText());
        action.setLocalVariableEvent(_localVariableEvent.getText());
        action.setLocalVariableNewValue(_localVariableNewValue.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionListenOnBeansTable_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansTableSwing.class);

}
