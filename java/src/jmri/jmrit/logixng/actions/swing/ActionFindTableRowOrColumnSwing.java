package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionFindTableRowOrColumn;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionFindTableRowOrColumn object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionFindTableRowOrColumnSwing extends AbstractDigitalActionSwing {

    private BeanSelectPanel<NamedTable> _tableBeanPanel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JComboBox<String> _rowOrColumnNameComboBox;
    private JCheckBox _includeCellsWithoutHeaderCheckBox;
    private JTextField _localVariableNamedBean;
    private JTextField _localVariableRow;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionFindTableRowOrColumn)) {
            throw new IllegalArgumentException("object must be an ActionFindTableRowOrColumn but is a: "+object.getClass().getName());
        }

        ActionFindTableRowOrColumn action = (ActionFindTableRowOrColumn)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tableBeanPanelPanel = new JPanel();
        tableBeanPanelPanel.add(new JLabel(Bundle.getMessage("ActionFindTableRowOrColumnSwing_Table")));
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
        tableRowOrColumnPanel.add(new JLabel(Bundle.getMessage("ActionFindTableRowOrColumnSwing_RowOrColumn")));
        tableRowOrColumnPanel.add(_tableRowOrColumnComboBox);
        panel.add(tableRowOrColumnPanel);

        JPanel rowOrColumnNamePanel = new JPanel();
        rowOrColumnNamePanel.add(new JLabel(Bundle.getMessage("ActionFindTableRowOrColumnSwing_RowOrColumnName")));
        rowOrColumnNamePanel.add(_rowOrColumnNameComboBox);
        panel.add(rowOrColumnNamePanel);
        JComboBoxUtil.setupComboBoxMaxRows(_rowOrColumnNameComboBox);

        JPanel includeCellsWithoutHeaderPanel = new JPanel();
        includeCellsWithoutHeaderPanel.add(new JLabel(Bundle.getMessage("ActionFindTableRowOrColumnSwing_IncludeCellsWithoutHeader")));
        _includeCellsWithoutHeaderCheckBox = new JCheckBox();
        includeCellsWithoutHeaderPanel.add(_includeCellsWithoutHeaderCheckBox);
        panel.add(includeCellsWithoutHeaderPanel);

        JPanel localVariableNamedBeanPanel = new JPanel();
        localVariableNamedBeanPanel.add(new JLabel(Bundle.getMessage("ActionFindTableRowOrColumnSwing_LocalVariableNamedBean")));
        _localVariableNamedBean = new JTextField(20);
        localVariableNamedBeanPanel.add(_localVariableNamedBean);
        panel.add(localVariableNamedBeanPanel);

        JPanel localVariableRowPanel = new JPanel();
        localVariableRowPanel.add(new JLabel(Bundle.getMessage("ActionFindTableRowOrColumnSwing_LocalVariableRow")));
        _localVariableRow = new JTextField(20);
        localVariableRowPanel.add(_localVariableRow);
        panel.add(localVariableRowPanel);

        if (action != null) {
            if (action.getSelectNamedBean().getNamedBean() != null) {
                _tableBeanPanel.setDefaultNamedBean(action.getSelectNamedBean().getNamedBean().getBean());
            }
            _tableRowOrColumnComboBox.setSelectedItem(action.getTableRowOrColumn());
            _includeCellsWithoutHeaderCheckBox.setSelected(action.getIncludeCellsWithoutHeader());

            _localVariableNamedBean.setText(action.getLocalVariableNamedBean());
            _localVariableRow.setText(action.getLocalVariableRow());
        }
    }

    private void comboListener( ActionFindTableRowOrColumn action ) {

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
        ActionFindTableRowOrColumn action = new ActionFindTableRowOrColumn(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ActionFindTableRowOrColumn)) {
            throw new IllegalArgumentException("object must be an ActionFindTableRowOrColumn but is a: "+object.getClass().getName());
        }
        if ( _tableBeanPanel == null ){
            throw new UnsupportedOperationException("No Bean Panel Present ");
        }

        ActionFindTableRowOrColumn action = (ActionFindTableRowOrColumn)object;
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
        action.setIncludeCellsWithoutHeader(_includeCellsWithoutHeaderCheckBox.isSelected());

        action.setLocalVariableNamedBean(_localVariableNamedBean.getText());
        action.setLocalVariableRow(_localVariableRow.getText());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionFindTableRowOrColumn_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionFindTableRowOrColumnSwing.class);

}
