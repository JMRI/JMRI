package jmri.jmrit.logixng.util.swing;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;
import javax.swing.table.*;

import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectStringList;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.table.ButtonEditor;
import jmri.util.table.ButtonRenderer;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectStringList.
 *
 * @author Daniel Bergqvist (C) 2025
 */
public class LogixNG_SelectStringListSwing {

    private JTabbedPane _tabbedPane;
    private JTable _dataTable;
    private StringListTableModel _dataTableModel;
    private JPanel _panelDirect;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;
    private JButton _addButton;


    public JPanel createPanel(@CheckForNull LogixNG_SelectStringList selectStrList) {

        JPanel panel = new JPanel();

        _tabbedPane = new JTabbedPane();
        _panelDirect = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        if (selectStrList != null) {
            if (selectStrList.isOnlyDirectAddressingAllowed()) {
                _tabbedPane.setEnabled(false);
                _panelLocalVariable.setEnabled(false);
                _panelFormula.setEnabled(false);
            }
        }

        _tabbedPane.addTab(NamedBeanAddressing.Direct.toString(), _panelDirect);
        _tabbedPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedPane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);

        _dataTable = new JTable() {
            //workaround for  https://bugs.openjdk.org/browse/JDK-4127936
            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getRowCount() == 0 ? super.getScrollableTracksViewportWidth()
                        : getPreferredSize().width < getParent().getWidth();
            }
        };

        if (selectStrList != null) {
            _dataTableModel = new StringListTableModel(selectStrList.getList());
        } else {
            _dataTableModel = new StringListTableModel(null);
        }
        _dataTable.setModel(_dataTableModel);
        _dataTable.getColumnModel().getColumn(0).setPreferredWidth(200);
        _dataTable.getColumnModel().getColumn(1).setPreferredWidth(
                new JButton(Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveUp")).getPreferredSize().width);
        _dataTable.getColumnModel().getColumn(2).setPreferredWidth(
                new JButton(Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveDown")).getPreferredSize().width);
        _dataTable.getColumnModel().getColumn(3).setPreferredWidth(
                new JButton(Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableRemove")).getPreferredSize().width);
        ButtonRenderer buttonRenderer = new ButtonRenderer();
        _dataTable.setDefaultRenderer(JButton.class, buttonRenderer);
        TableCellEditor buttonEditor = new ButtonEditor(new JButton());
        _dataTable.setDefaultEditor(JButton.class, buttonEditor);
        JScrollPane scrollPane = new JScrollPane(_dataTable);
        scrollPane.setPreferredSize(new java.awt.Dimension(600,200));
        JPanel tablePanel = new JPanel();
        tablePanel.setLayout(new BoxLayout(tablePanel, BoxLayout.Y_AXIS));
        tablePanel.add(scrollPane);
        _addButton = new JButton(Bundle.getMessage("ButtonAdd"));
        _addButton.addActionListener(_dataTableModel::add);
        tablePanel.add(_addButton);
        _panelDirect.add(tablePanel);

        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(30);
        _panelLocalVariable.add(_localVariableTextField);

        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(30);
        _panelFormula.add(_formulaTextField);


        if (selectStrList != null) {
            switch (selectStrList.getAddressing()) {
                case Direct: _tabbedPane.setSelectedComponent(_panelDirect); break;
                case LocalVariable: _tabbedPane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedPane.setSelectedComponent(_panelFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + selectStrList.getAddressing().name());
            }
            _localVariableTextField.setText(selectStrList.getLocalVariable());
            _formulaTextField.setText(selectStrList.getFormula());
        }

        panel.add(_tabbedPane);
        return panel;
    }

    public boolean validate(
            @Nonnull LogixNG_SelectStringList selectStrList,
            @Nonnull List<String> errorMessages) {

        try {
            selectStrList.setFormula(_formulaTextField.getText());
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStrList.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectStrList.setAddressing(NamedBeanAddressing.LocalVariable);
                selectStrList.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectStrList.setAddressing(NamedBeanAddressing.Formula);
                selectStrList.setFormula(_formulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        return errorMessages.isEmpty();
    }

    public void setEnabled(boolean value) {
        _tabbedPane.setEnabled(value);
        _dataTable.setEnabled(value);
        _localVariableTextField.setEnabled(value);
        _formulaTextField.setEnabled(value);
    }

    public void updateObject(@Nonnull LogixNG_SelectStringList selectStrList) {

        if (_tabbedPane.getSelectedComponent() == _panelDirect) {
            List<String> list = selectStrList.getList();
            list.clear();
            list.addAll(_dataTableModel.getData());
        }

        try {
            if (_tabbedPane.getSelectedComponent() == _panelDirect) {
                selectStrList.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedPane.getSelectedComponent() == _panelLocalVariable) {
                selectStrList.setAddressing(NamedBeanAddressing.LocalVariable);
                selectStrList.setLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedPane.getSelectedComponent() == _panelFormula) {
                selectStrList.setAddressing(NamedBeanAddressing.Formula);
                selectStrList.setFormula(_formulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneEnum has unknown selection");
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    public void dispose() {
    }




    private static class StringListTableModel extends AbstractTableModel {

        public static final int COLUMN_DATA = 0;
        public static final int COLUMN_MOVE_UP = 1;
        public static final int COLUMN_MOVE_DOWN = 2;
        public static final int COLUMN_REMOVE = 3;

        private final List<String> _data = new ArrayList<>();


        public StringListTableModel(List<String> data) {
            if (data != null) {
                this._data.addAll(data);
            }
        }

        /** {@inheritDoc} */
        @Override
        public int getRowCount() {
            return _data.size();
        }

        /** {@inheritDoc} */
        @Override
        public int getColumnCount() {
            return 4;
        }

        /** {@inheritDoc} */
        @Override
        public String getColumnName(int col) {
            switch (col) {
                case COLUMN_DATA:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableData");
                case COLUMN_MOVE_UP:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveUp");
                case COLUMN_MOVE_DOWN:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveDown");
                case COLUMN_REMOVE:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableRemove");
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public Class<?> getColumnClass(int col) {
            switch (col) {
                case COLUMN_DATA:
                    return String.class;
                case COLUMN_MOVE_UP:
                case COLUMN_MOVE_DOWN:
                case COLUMN_REMOVE:
                    return JButton.class;
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public void setValueAt(Object value, int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case COLUMN_DATA:
                    if (value != null) {
                        _data.set(rowIndex, value.toString());
                    } else {
                        _data.set(rowIndex, "");
                    }
                    break;
                case COLUMN_MOVE_UP:
                    if (rowIndex > 0) {
                        String s = _data.get(rowIndex-1);
                        _data.set(rowIndex-1, _data.get(rowIndex));
                        _data.set(rowIndex, s);
                        fireTableRowsUpdated(rowIndex-1, rowIndex);
                    }
                    break;
                case COLUMN_MOVE_DOWN:
                    if (rowIndex < _data.size()-1) {
                        String s = _data.get(rowIndex+1);
                        _data.set(rowIndex+1, _data.get(rowIndex));
                        _data.set(rowIndex, s);
                        fireTableRowsUpdated(rowIndex, rowIndex+1);
                    }
                    break;
                case COLUMN_REMOVE:
                    _data.remove(rowIndex);
                    fireTableRowsDeleted(rowIndex, rowIndex);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        /** {@inheritDoc} */
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex >= _data.size()) throw new IllegalArgumentException("Invalid row");

            switch (columnIndex) {
                case COLUMN_DATA:
                    return _data.get(rowIndex);
                case COLUMN_MOVE_UP:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveUp");
                case COLUMN_MOVE_DOWN:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableMoveDown");
                case COLUMN_REMOVE:
                    return Bundle.getMessage("LogixNG_SelectStringListSwing_ColumnVariableRemove");
                default:
                    throw new IllegalArgumentException("Invalid column");
            }
        }

        public void add(ActionEvent evt) {
            int row = _data.size();
            _data.add("");
            fireTableRowsInserted(row, row);
        }

        public List<String> getData() {
            return _data;
        }

    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectStringListSwing.class);
}
