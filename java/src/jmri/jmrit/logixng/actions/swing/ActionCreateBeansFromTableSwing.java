package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.ActionCreateBeansFromTable;
import jmri.jmrit.logixng.actions.NamedBeanType;
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an ActionCreateBeansFromTable object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionCreateBeansFromTableSwing extends AbstractDigitalActionSwing {

    private BeanSelectPanel<NamedTable> _tableBeanPanel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JComboBox<String> _rowOrColumnSystemNameComboBox;
    private JComboBox<String> _rowOrColumnUserNameComboBox;
    private JCheckBox _includeCellsWithoutHeaderCheckBox;
    private JCheckBox _onlyCreatableTypesCheckBox;
    private JComboBox<NamedBeanType> _namedBeanTypeComboBox;
    private JCheckBox _moveUserNameCheckBox;
    private JCheckBox _updateToUserNameCheckBox;
    private JCheckBox _removeOldBeanCheckBox;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof ActionCreateBeansFromTable)) {
            throw new IllegalArgumentException("object must be an ActionCreateBeansFromTable but is a: "+object.getClass().getName());
        }

        ActionCreateBeansFromTable action = (ActionCreateBeansFromTable)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JPanel tableBeanPanelPanel = new JPanel();
        tableBeanPanelPanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_Table")));
        _tableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        tableBeanPanelPanel.add(_tableBeanPanel);
        panel.add(tableBeanPanelPanel);

        _rowOrColumnSystemNameComboBox = new JComboBox<>();
        _rowOrColumnUserNameComboBox = new JComboBox<>();
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
        tableRowOrColumnPanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_RowOrColumn")));
        tableRowOrColumnPanel.add(_tableRowOrColumnComboBox);
        panel.add(tableRowOrColumnPanel);

        JPanel rowOrColumnSystemNamePanel = new JPanel();
        rowOrColumnSystemNamePanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_RowOrColumnSystemName")));
        rowOrColumnSystemNamePanel.add(_rowOrColumnSystemNameComboBox);
        panel.add(rowOrColumnSystemNamePanel);
        JComboBoxUtil.setupComboBoxMaxRows(_rowOrColumnSystemNameComboBox);

        JPanel rowOrColumnUserNamePanel = new JPanel();
        rowOrColumnUserNamePanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_RowOrColumnUserName")));
        rowOrColumnUserNamePanel.add(_rowOrColumnUserNameComboBox);
        panel.add(rowOrColumnUserNamePanel);
        JComboBoxUtil.setupComboBoxMaxRows(_rowOrColumnUserNameComboBox);

        JPanel includeCellsWithoutHeaderPanel = new JPanel();
        includeCellsWithoutHeaderPanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_IncludeCellsWithoutHeader")));
        _includeCellsWithoutHeaderCheckBox = new JCheckBox();
        includeCellsWithoutHeaderPanel.add(_includeCellsWithoutHeaderCheckBox);
        panel.add(includeCellsWithoutHeaderPanel);

        JPanel onlyCreatableTypesPanel = new JPanel();
        onlyCreatableTypesPanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_OnlyCreatableTypesCheckBox")));
        _onlyCreatableTypesCheckBox = new JCheckBox();
        _onlyCreatableTypesCheckBox.setSelected(true);
        _onlyCreatableTypesCheckBox.addActionListener((evt)->{ updateNamedBeanTypeComboBox(); });
        onlyCreatableTypesPanel.add(_onlyCreatableTypesCheckBox);
        panel.add(onlyCreatableTypesPanel);

        JPanel namedBeanTypePanel = new JPanel();
        namedBeanTypePanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_NamedBeanType")));
        _namedBeanTypeComboBox = new JComboBox<>();
        JComboBoxUtil.setupComboBoxMaxRows(_namedBeanTypeComboBox);
        namedBeanTypePanel.add(_namedBeanTypeComboBox);
        panel.add(namedBeanTypePanel);

        JPanel moveUserNamePanel = new JPanel();
        moveUserNamePanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_MoveUserNameCheckBox")));
        _moveUserNameCheckBox = new JCheckBox();
        moveUserNamePanel.add(_moveUserNameCheckBox);
        panel.add(moveUserNamePanel);

        JPanel updateToUserNamePanel = new JPanel();
        updateToUserNamePanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_UpdateToUserName")));
        _updateToUserNameCheckBox = new JCheckBox();
        updateToUserNamePanel.add(_updateToUserNameCheckBox);
        panel.add(updateToUserNamePanel);

        JPanel removeOldBeanPanel = new JPanel();
        removeOldBeanPanel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_RemoveOldBeanCheckBox")));
        _removeOldBeanCheckBox = new JCheckBox();
        removeOldBeanPanel.add(_removeOldBeanCheckBox);
        panel.add(removeOldBeanPanel);

        panel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_Message1")));
        panel.add(new JLabel(Bundle.getMessage("ActionCreateBeansFromTableSwing_Message2")));

        if (action != null) {
            if (action.getSelectNamedBean().getNamedBean() != null) {
                _tableBeanPanel.setDefaultNamedBean(action.getSelectNamedBean().getNamedBean().getBean());
            }
            _tableRowOrColumnComboBox.setSelectedItem(action.getTableRowOrColumn());
            _includeCellsWithoutHeaderCheckBox.setSelected(action.isIncludeCellsWithoutHeader());
            _moveUserNameCheckBox.setSelected(action.isMoveUserName());
            _updateToUserNameCheckBox.setSelected(action.isUpdateToUserName());
            _removeOldBeanCheckBox.setSelected(action.isRemoveOldBean());

            // All types must be in the list when the type is selected.
            _onlyCreatableTypesCheckBox.setSelected(false);
            updateNamedBeanTypeComboBox();
            _namedBeanTypeComboBox.setSelectedItem(action.getNamedBeanType());
            _onlyCreatableTypesCheckBox.setSelected(action.isOnlyCreatableTypes());
        }
    }

    private void updateNamedBeanTypeComboBox() {
        NamedBeanType lastSelectedItem =
                _namedBeanTypeComboBox.getSelectedIndex() != -1
                ? _namedBeanTypeComboBox.getItemAt(
                        _namedBeanTypeComboBox.getSelectedIndex())
                : null;

        _namedBeanTypeComboBox.removeAllItems();
        for (NamedBeanType item : NamedBeanType.values()) {
            if (!_onlyCreatableTypesCheckBox.isSelected()
                    || item.getCreateBean() != null) {
                _namedBeanTypeComboBox.addItem(item);
            }
        }

        if (lastSelectedItem != null) {
            _namedBeanTypeComboBox.setSelectedItem(lastSelectedItem);
        }
    }

    private void comboListener( ActionCreateBeansFromTable action ) {

        _rowOrColumnSystemNameComboBox.removeAllItems();
        _rowOrColumnUserNameComboBox.removeAllItems();
        _rowOrColumnUserNameComboBox.addItem("");

        NamedTable table = _tableBeanPanel.getNamedBean();
        if (table != null) {
            if (_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()) == TableRowOrColumn.Column) {
                for (int column=0; column <= table.numColumns(); column++) {
                    // If the header is null or empty, treat the row as a comment
                    Object header = table.getCell(0, column);
                    if ((header != null) && (!header.toString().isEmpty())) {
                        _rowOrColumnSystemNameComboBox.addItem(header.toString());
                        _rowOrColumnUserNameComboBox.addItem(header.toString());
                    }
                }
            } else {
                for (int row=0; row <= table.numRows(); row++) {
                    // If the header is null or empty, treat the row as a comment
                    Object header = table.getCell(row, 0);
                    if ((header != null) && (!header.toString().isEmpty())) {
                        _rowOrColumnSystemNameComboBox.addItem(header.toString());
                        _rowOrColumnUserNameComboBox.addItem(header.toString());
                    }
                }
            }
            if (action != null) {
                _rowOrColumnSystemNameComboBox.setSelectedItem(action.getRowOrColumnSystemName());
                _rowOrColumnUserNameComboBox.setSelectedItem(action.getRowOrColumnUserName());
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        if (_tableBeanPanel.getNamedBean() != null) {
            String rowColSystemName = null;
            String rowColUserName = null;
            if (_rowOrColumnSystemNameComboBox.getSelectedIndex() != -1) {
                rowColSystemName = _rowOrColumnSystemNameComboBox.getItemAt(_rowOrColumnSystemNameComboBox.getSelectedIndex());
            }
            if (_rowOrColumnUserNameComboBox.getSelectedIndex() != -1) {
                rowColUserName = _rowOrColumnUserNameComboBox.getItemAt(_rowOrColumnUserNameComboBox.getSelectedIndex());
            }
            if (rowColSystemName == null) {
                errorMessages.add("\"Row or column system name\" must be selected");
            } else if (rowColSystemName.equals(rowColUserName)) {
                errorMessages.add("\"Row or column system name\" must not be the same as \"Row or column user name\"");
            }
        }
        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        ActionCreateBeansFromTable action = new ActionCreateBeansFromTable(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof ActionCreateBeansFromTable)) {
            throw new IllegalArgumentException("object must be an ActionCreateBeansFromTable but is a: "+object.getClass().getName());
        }
        if ( _tableBeanPanel == null ){
            throw new UnsupportedOperationException("No Bean Panel Present ");
        }

        ActionCreateBeansFromTable action = (ActionCreateBeansFromTable)object;
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
        if (_rowOrColumnSystemNameComboBox.getSelectedIndex() != -1) {
            action.setRowOrColumnSystemName(_rowOrColumnSystemNameComboBox.getItemAt(_rowOrColumnSystemNameComboBox.getSelectedIndex()));
        } else {
            action.setRowOrColumnSystemName("");
        }
        if (_rowOrColumnUserNameComboBox.getSelectedIndex() != -1) {
            action.setRowOrColumnUserName(_rowOrColumnUserNameComboBox.getItemAt(_rowOrColumnUserNameComboBox.getSelectedIndex()));
        } else {
            action.setRowOrColumnUserName("");
        }
        action.setOnlyCreatableTypes(_onlyCreatableTypesCheckBox.isSelected());
        if (_namedBeanTypeComboBox.getSelectedIndex() != -1) {
            action.setNamedBeanType(_namedBeanTypeComboBox.getItemAt(_namedBeanTypeComboBox.getSelectedIndex()));
        }
        action.setIncludeCellsWithoutHeader(_includeCellsWithoutHeaderCheckBox.isSelected());
        action.setMoveUserName(_moveUserNameCheckBox.isSelected());
        action.setUpdateToUserName(_updateToUserNameCheckBox.isSelected());
        action.setRemoveOldBean(_removeOldBeanCheckBox.isSelected());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("ActionCreateBeansFromTable_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionCreateBeansFromTableSwing.class);

}
