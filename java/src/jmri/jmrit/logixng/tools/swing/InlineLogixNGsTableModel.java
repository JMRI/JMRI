package jmri.jmrit.logixng.tools.swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.*;
import java.util.stream.Stream;

import javax.swing.*;
import javax.swing.table.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.util.swing.JComboBoxUtil;

/**
 * Table model for inline LogixNGs.
 *
 * @author Daniel Bergqvist Copyright (C) 2022
 */
public class InlineLogixNGsTableModel extends AbstractTableModel {

    public static final int COLUMN_SYSTEM_NAME = 0;
    public static final int COLUMN_USER_NAME = COLUMN_SYSTEM_NAME + 1;
    public static final int COLUMN_PANEL_NAME = COLUMN_USER_NAME + 1;
    public static final int COLUMN_POSITIONABLE_NAME = COLUMN_PANEL_NAME + 1;
    public static final int COLUMN_POS_X = COLUMN_POSITIONABLE_NAME + 1;
    public static final int COLUMN_POS_Y = COLUMN_POS_X + 1;
    public static final int COLUMN_MENU = COLUMN_POS_Y + 1;
    public static final int NUM_COLUMNS = COLUMN_MENU + 1;

    private final List<LogixNG> _logixNGs = new ArrayList<>();
    private boolean _inEditLogixNGMode = false;
    private jmri.jmrit.logixng.tools.swing.LogixNGEditor _logixNGEditor;


    public InlineLogixNGsTableModel() {
        updateList();
        InstanceManager.getDefault(LogixNG_Manager.class)
                .addPropertyChangeListener("length", (evt) -> {
                    updateList();
                    fireTableDataChanged();
                });
    }

    private void updateList() {
        Stream<LogixNG> stream = InstanceManager.getDefault(LogixNG_Manager.class)
                .getNamedBeanSet().stream().filter((LogixNG t) -> t.isInline());
        _logixNGs.clear();
        _logixNGs.addAll(stream.collect(java.util.stream.Collectors.toList()));
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _logixNGs.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return NUM_COLUMNS;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLUMN_SYSTEM_NAME:
                return Bundle.getMessage("ColumnSystemName");
            case COLUMN_USER_NAME:
                return Bundle.getMessage("ColumnUserName");
            case COLUMN_PANEL_NAME:
                return Bundle.getMessage("InlineLogixNGsTableModel_ColumnPanelName");
            case COLUMN_POSITIONABLE_NAME:
                return Bundle.getMessage("InlineLogixNGsTableModel_ColumnPositionableName");
            case COLUMN_POS_X:
                return Bundle.getMessage("InlineLogixNGsTableModel_ColumnPosX");
            case COLUMN_POS_Y:
                return Bundle.getMessage("InlineLogixNGsTableModel_ColumnPosY");
            case COLUMN_MENU:
                return Bundle.getMessage("InlineLogixNGsTableModel_ColumnMenu");
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_SYSTEM_NAME:
            case COLUMN_USER_NAME:
            case COLUMN_PANEL_NAME:
            case COLUMN_POSITIONABLE_NAME:
                return String.class;
            case COLUMN_POS_X:
            case COLUMN_POS_Y:
                return Integer.class;
            case COLUMN_MENU:
                return Menu.class;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(int row, int col) {
        return col == COLUMN_MENU;
    }

    /** {@inheritDoc} */
    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
/*
        if (columnIndex == COLUMN_MENU) return;

        SymbolTable.VariableData variable = _logixNGs.get(rowIndex);

        switch (columnIndex) {
            case COLUMN_NAME:
                variable._name = (String) value;
                break;
            case COLUMN_TYPE:
                variable._initialValueType = (SymbolTable.InitialValueType) value;
                break;
            case COLUMN_DATA:
                variable._initialValueData = (String) value;
                break;
            case COLUMN_MENU:
                // Do nothing
                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
*/
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _logixNGs.size()) throw new IllegalArgumentException("Invalid row");

        LogixNG logixNG = _logixNGs.get(rowIndex);

        switch (columnIndex) {
            case COLUMN_SYSTEM_NAME:
                return logixNG.getSystemName();
            case COLUMN_USER_NAME:
                return logixNG.getUserName();
            case COLUMN_PANEL_NAME:
                return logixNG.getPositionable().getEditor().getName();
            case COLUMN_POSITIONABLE_NAME:
                return logixNG.getPositionable().getNameString();
            case COLUMN_POS_X:
                return logixNG.getPositionable().getX();
            case COLUMN_POS_Y:
                return logixNG.getPositionable().getY();
            case COLUMN_MENU:
                return Menu.Edit;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    public void setColumnForMenu(JTable table) {
        JComboBox<Menu> comboBox = new JComboBox<>();
        table.setRowHeight(comboBox.getPreferredSize().height);
        table.getColumnModel().getColumn(COLUMN_MENU)
                .setPreferredWidth((comboBox.getPreferredSize().width) + 4);
    }


    public static enum Menu {
        Edit(Bundle.getMessage("InlineLogixNGsTableModel_TableMenuEdit")),
        Delete(Bundle.getMessage("InlineLogixNGsTableModel_TableMenuDelete"));

        private final String _descr;

        private Menu(String descr) {
            _descr = descr;
        }

        @Override
        public String toString() {
            return _descr;
        }
    }


    public static class MenuCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {

            if (value == null) value = Menu.Edit;

            if (! (value instanceof Menu)) {
                throw new IllegalArgumentException("value is not an Menu: " + value.getClass().getName());
            }
            setText(((Menu) value).toString());
            return this;
        }
    }


    public static class MenuCellEditor extends AbstractCellEditor
            implements TableCellEditor, ActionListener {

        JTable _table;
        InlineLogixNGsTableModel _tableModel;

        public MenuCellEditor(JTable table, InlineLogixNGsTableModel tableModel) {
            _table = table;
            _tableModel = tableModel;
        }

        @Override
        public Object getCellEditorValue() {
            return Menu.Edit;
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {

            if (value == null) value = Menu.Edit;

            if (! (value instanceof Menu)) {
                throw new IllegalArgumentException("value is not an Menu: " + value.getClass().getName());
            }

            JComboBox<Menu> menuComboBox = new JComboBox<>();

            for (Menu menu : Menu.values()) {
                menuComboBox.addItem(menu);
            }
            JComboBoxUtil.setupComboBoxMaxRows(menuComboBox);

            menuComboBox.setSelectedItem(value);
            menuComboBox.addActionListener(this);

            return menuComboBox;
        }

        @Override
        @SuppressWarnings("unchecked")  // Not possible to check that event.getSource() is instanceof JComboBox<Menu>
        public void actionPerformed(ActionEvent event) {
            if (! (event.getSource() instanceof JComboBox)) {
                throw new IllegalArgumentException("value is not an InitialValueType: " + event.getSource().getClass().getName());
            }
            JComboBox<Menu> menuComboBox =
                    (JComboBox<Menu>) event.getSource();
            int row = _table.getSelectedRow();
            Menu menu = menuComboBox.getItemAt(menuComboBox.getSelectedIndex());

            switch (menu) {
                case Edit:
                    edit(row);
                    break;
                case Delete:
                    delete(row);
                    break;
                default:
                    // Do nothing
            }
            // Remove focus from combo box
//            if (_tableModel._variables.size() > 0) _table.editCellAt(row, COLUMN_NAME);
        }

        /**
         * Check if edit of a conditional is in progress.
         *
         * @return true if this is the case, after showing dialog to user
         */
        private boolean checkEditConditionalNG() {
            if (_tableModel._inEditLogixNGMode) {
                // Already editing a LogixNG, ask for completion of that edit
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("Error_InlineLogixNGInEditMode"), // NOI18N
                        Bundle.getMessage("ErrorTitle"), // NOI18N
                        JOptionPane.ERROR_MESSAGE);
                _tableModel._logixNGEditor.bringToFront();
                return true;
            }
            return false;
        }

        private void edit(int row) {
            if (checkEditConditionalNG()) return;

            LogixNG logixNG = _tableModel._logixNGs.get(row);
            LogixNGEditor logixNGEditor =
                    new LogixNGEditor(null, logixNG.getSystemName());
            logixNGEditor.addEditorEventListener((HashMap<String, String> data) -> {
                _tableModel._inEditLogixNGMode = false;
            });
            logixNGEditor.bringToFront();
            _tableModel._inEditLogixNGMode = true;
            _tableModel._logixNGEditor = logixNGEditor;
        }

        private void delete(int row) {
            LogixNG logixNG = _tableModel._logixNGs.get(row);

            if (_tableModel._inEditLogixNGMode) {
                // Already editing a bean, ask for completion of that edit
                JOptionPane.showMessageDialog(null,
                        Bundle.getMessage("Error_InlineLogixNGInEditMode",
                                logixNG.getSystemName()),
                        Bundle.getMessage("ErrorTitle"),
                        JOptionPane.ERROR_MESSAGE);
                if (_tableModel._logixNGEditor != null) {
                    _tableModel._logixNGEditor.bringToFront();
                }
                return;
            }

            DeleteBean<LogixNG> deleteBean = new DeleteBean<>(
                    InstanceManager.getDefault(LogixNG_Manager.class));

            boolean hasChildren = logixNG.getNumConditionalNGs() > 0;

            deleteBean.delete(logixNG, hasChildren, (t)->{deleteBean(t);},
                    (t,list)->{logixNG.getListenerRefsIncludingChildren(list);},
                    jmri.jmrit.logixng.LogixNG_UserPreferences.class.getName());
        }

        private void deleteBean(LogixNG logixNG) {
            logixNG.setEnabled(false);
            try {
                InstanceManager.getDefault(LogixNG_Manager.class).deleteBean(logixNG, "DoDelete");
                logixNG.getPositionable().setLogixNG(null);
                _tableModel.fireTableDataChanged();
            } catch (PropertyVetoException e) {
                //At this stage the DoDelete shouldn't fail, as we have already done a can delete, which would trigger a veto
                log.error("{} : Could not Delete.", e.getMessage());
            }
        }
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InlineLogixNGsTableModel.class);
}
