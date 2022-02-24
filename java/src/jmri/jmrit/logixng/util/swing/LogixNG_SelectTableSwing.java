package jmri.jmrit.logixng.util.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.swing.LogixNG_DataDialog;
import jmri.jmrit.logixng.swing.SwingConfiguratorInterface;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.swing.BeanSelectPanel;

/**
 * Swing class for jmri.jmrit.logixng.util.LogixNG_SelectTable.
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectTableSwing {

    private final JDialog _dialog;
    private final LogixNG_DataDialog _logixNG_DataDialog;
    private WindowFocusListener _focusListener;

    private JLabel _tableNameLabel;
    private JLabel _rowNameLabel;
    private JLabel _columnNameLabel;

    private JButton _selectTableNameButton;
    private JButton _selectRowNameButton;
    private JButton _selectColumnNameButton;

    private NamedBeanAddressing _tableNameAddressing = NamedBeanAddressing.Direct;
    private BeanSelectPanel<NamedTable> _compareToTableBeanPanel;
    private JTextField _tableNameReferenceTextField;
    private JTextField _tableNameLocalVariableTextField;
    private JTextField _tableNameFormulaTextField;

    private NamedBeanAddressing _tableRowAddressing = NamedBeanAddressing.Direct;
    private JComboBox<String> _tableRowNameComboBox;
    private JTextField _tableRowNameTextField;
    private JTextField _tableRowReferenceTextField;
    private JTextField _tableRowLocalVariableTextField;
    private JTextField _tableRowFormulaTextField;

    private NamedBeanAddressing _tableColumnAddressing = NamedBeanAddressing.Direct;
    private JComboBox<String> _tableColumnNameComboBox;
    private JTextField _tableColumnNameTextField;
    private JTextField _tableColumnReferenceTextField;
    private JTextField _tableColumnLocalVariableTextField;
    private JTextField _tableColumnFormulaTextField;


    public LogixNG_SelectTableSwing(
            @Nonnull JDialog dialog, @Nonnull SwingConfiguratorInterface swi) {
        _logixNG_DataDialog = new LogixNG_DataDialog(swi);
        _dialog = dialog;
    }

    private String getTableNameDescription() {
        String namedBean;
        switch (_tableNameAddressing) {
            case Direct:
                String tableName;
                if (_compareToTableBeanPanel.getNamedBean() != null) {
                    tableName = _compareToTableBeanPanel.getNamedBean().getDisplayName();
                } else {
                    tableName = Bundle.getMessage("BeanNotSelected");   // NOI18N
                }
                namedBean = Bundle.getMessage("AddressByDirect", tableName);    // NOI18N
                break;

            case Reference:
                namedBean = Bundle.getMessage("AddressByReference", _tableNameReferenceTextField.getText());    // NOI18N
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage("AddressByLocalVariable", _tableNameLocalVariableTextField.getText());    // NOI18N
                break;

            case Formula:
                namedBean = Bundle.getMessage("AddressByFormula", _tableNameFormulaTextField.getText());    // NOI18N
                break;

            default:
                throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name()); // NOI18N
        }
        return Bundle.getMessage("LogixNG_SelectTableSwing_Table", namedBean);  // NOI18N
    }

    private String getTableRowDescription() {
        String row;
        switch (_tableRowAddressing) {
            case Direct:
                String rowName =
                        _tableNameAddressing == NamedBeanAddressing.Direct
                        ? _tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex())
                        : _tableRowNameTextField.getText();
                row = Bundle.getMessage("AddressByDirect", rowName);   // NOI18N
                break;

            case Reference:
                row = Bundle.getMessage("AddressByReference", _tableRowReferenceTextField.getText());   // NOI18N
                break;

            case LocalVariable:
                row = Bundle.getMessage("AddressByLocalVariable", _tableRowLocalVariableTextField.getText());   // NOI18N
                break;

            case Formula:
                row = Bundle.getMessage("AddressByFormula", _tableRowFormulaTextField.getText());   // NOI18N
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());   // NOI18N
        }
        return Bundle.getMessage("LogixNG_SelectTableSwing_Row", row);  // NOI18N
    }

    private String getTableColumnDescription() {
        String column;
        switch (_tableColumnAddressing) {
            case Direct:
                String columnName =
                        _tableNameAddressing == NamedBeanAddressing.Direct
                        ? _tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex())
                        : _tableColumnNameTextField.getText();
                column = Bundle.getMessage("AddressByDirect", columnName); // NOI18N
                break;

            case Reference:
                column = Bundle.getMessage("AddressByReference", _tableColumnReferenceTextField.getText()); // NOI18N
                break;

            case LocalVariable:
                column = Bundle.getMessage("AddressByLocalVariable", _tableColumnLocalVariableTextField.getText()); // NOI18N
                break;

            case Formula:
                column = Bundle.getMessage("AddressByFormula", _tableColumnFormulaTextField.getText()); // NOI18N
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableColumnAddressing.name());   // NOI18N
        }
        return Bundle.getMessage("LogixNG_SelectTableSwing_Column", column);    // NOI18N
    }

    private void selectTableNameFinished() {
        _tableNameAddressing = _logixNG_DataDialog.getAddressing();
        _tableNameLabel.setText(getTableNameDescription());
    }

    private void selectTableRowFinished() {
        _tableRowAddressing = _logixNG_DataDialog.getAddressing();
        _rowNameLabel.setText(getTableRowDescription());
    }

    private void selectTableColumnFinished() {
        _tableColumnAddressing = _logixNG_DataDialog.getAddressing();
        _columnNameLabel.setText(getTableColumnDescription());
    }

    private void setupRowOrColumnNameComboBox(@CheckForNull LogixNG_SelectTable selectTable) {
        String rowName = selectTable != null ? selectTable.getTableRowName() : null;
        String columnName = selectTable != null ? selectTable.getTableColumnName() : null;

        _tableRowNameComboBox.removeAllItems();
        _tableColumnNameComboBox.removeAllItems();

        NamedTable table = _compareToTableBeanPanel.getNamedBean();
        if (table != null) {
            for (int row=0; row <= table.numRows(); row++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(row, 0);
                if ((header != null) && (!header.toString().isEmpty())) {
                    _tableRowNameComboBox.addItem(header.toString());
                }
            }
            for (int column=0; column <= table.numColumns(); column++) {
                // If the header is null or empty, treat the row as a comment
                Object header = table.getCell(0, column);
                if ((header != null) && (!header.toString().isEmpty())) {
                    _tableColumnNameComboBox.addItem(header.toString());
                }
            }
            _tableRowNameComboBox.setSelectedItem(rowName);
            _tableColumnNameComboBox.setSelectedItem(columnName);
        }
    }


    public JPanel createPanel(@CheckForNull LogixNG_SelectTable selectTable) {

        _compareToTableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);

        _tableNameLabel = new JLabel();
        _rowNameLabel = new JLabel();
        _columnNameLabel = new JLabel();

        _tableNameAddressing = NamedBeanAddressing.Direct;
        _compareToTableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        _compareToTableBeanPanel.getBeanCombo().addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(selectTable);
        });

        _tableRowNameComboBox = new JComboBox<>();
        _tableRowNameTextField = new JTextField(30);
        _tableColumnNameComboBox = new JComboBox<>();
        _tableColumnNameTextField = new JTextField(30);

        _selectTableNameButton = new JButton(Bundle.getMessage("LogixNG_SelectTableSwing_Select"));     // NOI18N
        _selectRowNameButton = new JButton(Bundle.getMessage("LogixNG_SelectTableSwing_Select"));       // NOI18N
        _selectColumnNameButton = new JButton(Bundle.getMessage("LogixNG_SelectTableSwing_Select"));    // NOI18N

        _tableNameReferenceTextField = new JTextField(30);
        _tableNameLocalVariableTextField = new JTextField(30);
        _tableNameFormulaTextField = new JTextField(30);

        _tableRowReferenceTextField = new JTextField(30);
        _tableRowLocalVariableTextField = new JTextField(30);
        _tableRowFormulaTextField = new JTextField(30);

        _tableColumnReferenceTextField = new JTextField(30);
        _tableColumnLocalVariableTextField = new JTextField(30);
        _tableColumnFormulaTextField = new JTextField(30);

        _selectTableNameButton.addActionListener((evt) -> {
            _logixNG_DataDialog.showDialog(
                    Bundle.getMessage("LogixNG_SelectTableSwing_SelectTable"),
                    _tableNameAddressing,
                    _compareToTableBeanPanel,
                    _tableNameReferenceTextField,
                    _tableNameLocalVariableTextField,
                    _tableNameFormulaTextField,
                    this::selectTableNameFinished);
        });
        _selectRowNameButton.addActionListener((evt) -> {
            if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                _logixNG_DataDialog.showDialog(
                        Bundle.getMessage("LogixNG_SelectTableSwing_SelectRow"),
                        _tableRowAddressing,
                        _tableRowNameComboBox,
                        _tableRowReferenceTextField,
                        _tableRowLocalVariableTextField,
                        _tableRowFormulaTextField,
                        this::selectTableRowFinished);
            } else {
                _logixNG_DataDialog.showDialog(
                        Bundle.getMessage("LogixNG_SelectTableSwing_SelectRow"),
                        _tableRowAddressing,
                        _tableRowNameTextField,
                        _tableRowReferenceTextField,
                        _tableRowLocalVariableTextField,
                        _tableRowFormulaTextField,
                        this::selectTableRowFinished);
            }
        });
        _selectColumnNameButton.addActionListener((evt) -> {
            if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                _logixNG_DataDialog.showDialog(
                        Bundle.getMessage("LogixNG_SelectTableSwing_SelectColumn"),
                        _tableColumnAddressing,
                        _tableColumnNameComboBox,
                        _tableColumnReferenceTextField,
                        _tableColumnLocalVariableTextField,
                        _tableColumnFormulaTextField,
                        this::selectTableColumnFinished);
            } else {
                _logixNG_DataDialog.showDialog(
                        Bundle.getMessage("LogixNG_SelectTableSwing_SelectColumn"),
                        _tableColumnAddressing,
                        _tableColumnNameTextField,
                        _tableColumnReferenceTextField,
                        _tableColumnLocalVariableTextField,
                        _tableColumnFormulaTextField,
                        this::selectTableColumnFinished);
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new java.awt.GridBagLayout());

        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridwidth = 1;
        constraints.gridheight = 1;
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.EAST;
        panel.add(_selectTableNameButton, constraints);
        constraints.gridy = 1;
        panel.add(_selectRowNameButton, constraints);
        constraints.gridy = 2;
        panel.add(_selectColumnNameButton, constraints);
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        panel.add(_tableNameLabel, constraints);
        constraints.gridy = 1;
        panel.add(_rowNameLabel, constraints);
        constraints.gridy = 2;
        panel.add(_columnNameLabel, constraints);
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        panel.add(new JLabel("  "), constraints);
        constraints.gridx = 2;
        constraints.gridy = 0;
        constraints.anchor = java.awt.GridBagConstraints.WEST;
        panel.add(_selectTableNameButton, constraints);
        constraints.gridy = 1;
        panel.add(_selectRowNameButton, constraints);
        constraints.gridy = 2;
        panel.add(_selectColumnNameButton, constraints);


        if (selectTable != null) {
            _tableNameAddressing = selectTable.getTableNameAddressing();

            switch (_tableNameAddressing) {
                case Direct:
                    if (selectTable.getTable() != null) {
                        _compareToTableBeanPanel.setDefaultNamedBean(selectTable.getTable().getBean());
                    }
                    break;
                case Reference: _tableNameReferenceTextField.setText(selectTable.getTableNameReference()); break;
                case LocalVariable: _tableNameLocalVariableTextField.setText(selectTable.getTableNameLocalVariable()); break;
                case Formula: _tableNameFormulaTextField.setText(selectTable.getTableNameFormula()); break;
                default: throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());    // NOI18N
            }

            _tableRowAddressing = selectTable.getTableRowAddressing();
            switch (_tableRowAddressing) {
                case Direct:
                    if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                        _tableRowNameComboBox.setSelectedItem(selectTable.getTableRowName());
                    } else {
                        _tableRowNameTextField.setText(selectTable.getTableRowName());
                    }
                    break;
                case Reference: _tableRowReferenceTextField.setText(selectTable.getTableRowReference()); break;
                case LocalVariable: _tableRowLocalVariableTextField.setText(selectTable.getTableRowLocalVariable()); break;
                case Formula: _tableRowFormulaTextField.setText(selectTable.getTableRowFormula()); break;
                default: throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());  // NOI18N
            }

            _tableColumnAddressing = selectTable.getTableColumnAddressing();
            switch (_tableColumnAddressing) {
                case Direct:
                    if (_tableNameAddressing == NamedBeanAddressing.Direct) {
                        _tableColumnNameComboBox.setSelectedItem(selectTable.getTableColumnName());
                    } else {
                        _tableColumnNameTextField.setText(selectTable.getTableColumnName());
                    }
                    break;
                case Reference: _tableColumnReferenceTextField.setText(selectTable.getTableColumnReference()); break;
                case LocalVariable: _tableColumnLocalVariableTextField.setText(selectTable.getTableColumnLocalVariable()); break;
                case Formula: _tableColumnFormulaTextField.setText(selectTable.getTableColumnFormula()); break;
                default: throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());    // NOI18N
            }
        }

        _tableNameLabel.setText(getTableNameDescription());
        _rowNameLabel.setText(getTableRowDescription());
        _columnNameLabel.setText(getTableColumnDescription());

        _focusListener = new WindowFocusListener(){
            @Override
            public void windowGainedFocus(WindowEvent e) {
                _logixNG_DataDialog.checkOpenDialog();
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                // Do nothing
            }
        };
        _dialog.addWindowFocusListener(_focusListener);

        return panel;
    }

    public boolean validate(@Nonnull LogixNG_SelectTable selectTable, @Nonnull List<String> errorMessages) {

        try {
            switch (_tableNameAddressing) {
                case Direct:
                    NamedTable table = _compareToTableBeanPanel.getNamedBean();
                    if (table != null) selectTable.setTable(table);
                    else selectTable.removeTable();
                    break;
                case Reference: selectTable.setTableNameReference(_tableNameReferenceTextField.getText()); break;
                case LocalVariable: selectTable.setTableNameLocalVariable(_tableNameLocalVariableTextField.getText()); break;
                case Formula: selectTable.setTableNameFormula(_tableNameFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());
            }

            String rowName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex())
                    : _tableRowNameTextField.getText();
            switch (_tableRowAddressing) {
                case Direct: selectTable.setTableRowName(rowName); break;
                case Reference: selectTable.setTableRowReference(_tableRowReferenceTextField.getText()); break;
                case LocalVariable: selectTable.setTableRowLocalVariable(_tableRowLocalVariableTextField.getText()); break;
                case Formula: selectTable.setTableRowFormula(_tableRowFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
            }

            String columnName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex())
                    : _tableColumnNameTextField.getText();
            switch (_tableColumnAddressing) {
                case Direct: selectTable.setTableColumnName(columnName); break;
                case Reference: selectTable.setTableColumnReference(_tableColumnReferenceTextField.getText()); break;
                case LocalVariable: selectTable.setTableColumnLocalVariable(_tableColumnLocalVariableTextField.getText()); break;
                case Formula: selectTable.setTableColumnFormula(_tableColumnFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());
            }
        } catch (IllegalArgumentException e) {
            errorMessages.add("Invalid value: " + e.getMessage());
            return false;
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }
        return true;
    }

    public void updateObject(@Nonnull LogixNG_SelectTable selectTable) {

        try {
            selectTable.setTableNameAddressing(_tableNameAddressing);
            switch (_tableNameAddressing) {
                case Direct:
                    NamedTable table = _compareToTableBeanPanel.getNamedBean();
                    if (table != null) selectTable.setTable(table);
                    else selectTable.removeTable();
                    break;
                case Reference: selectTable.setTableNameReference(_tableNameReferenceTextField.getText()); break;
                case LocalVariable: selectTable.setTableNameLocalVariable(_tableNameLocalVariableTextField.getText()); break;
                case Formula: selectTable.setTableNameFormula(_tableNameFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());
            }

            selectTable.setTableRowAddressing(_tableRowAddressing);
            String rowName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableRowNameComboBox.getItemAt(_tableRowNameComboBox.getSelectedIndex())
                    : _tableRowNameTextField.getText();
            switch (_tableRowAddressing) {
                case Direct: selectTable.setTableRowName(rowName); break;
                case Reference: selectTable.setTableRowReference(_tableRowReferenceTextField.getText()); break;
                case LocalVariable: selectTable.setTableRowLocalVariable(_tableRowLocalVariableTextField.getText()); break;
                case Formula: selectTable.setTableRowFormula(_tableRowFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
            }

            selectTable.setTableColumnAddressing(_tableColumnAddressing);
            String columnName =
                    _tableNameAddressing == NamedBeanAddressing.Direct
                    ? _tableColumnNameComboBox.getItemAt(_tableColumnNameComboBox.getSelectedIndex())
                    : _tableColumnNameTextField.getText();
            switch (_tableColumnAddressing) {
                case Direct: selectTable.setTableColumnName(columnName); break;
                case Reference: selectTable.setTableColumnReference(_tableColumnReferenceTextField.getText()); break;
                case LocalVariable: selectTable.setTableColumnLocalVariable(_tableColumnLocalVariableTextField.getText()); break;
                case Formula: selectTable.setTableColumnFormula(_tableColumnFormulaTextField.getText()); break;
                default: throw new IllegalArgumentException("invalid _tableColumnAddressing: " + _tableColumnAddressing.name());
            }
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("LocalVariable_Short");
    }

    public boolean canClose() {
        if (_logixNG_DataDialog.checkOpenDialog()) {
            JOptionPane.showMessageDialog(_dialog,
                    Bundle.getMessage("Error_InSelectMode"), // NOI18N
                    Bundle.getMessage("ErrorTitle"), // NOI18N
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        return true;
    }

    public void dispose() {
        _logixNG_DataDialog.dispose();
        _dialog.removeWindowFocusListener(_focusListener);
    }

}
