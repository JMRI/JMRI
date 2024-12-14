package jmri.jmrit.logixng.actions.swing;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.actions.TableForEach;
import jmri.jmrit.logixng.TableRowOrColumn;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.swing.LogixNG_SelectNamedBeanSwing;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an TableForEach object with a Swing JPanel.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class TableForEachSwing extends AbstractDigitalActionSwing {

    private LogixNG_SelectNamedBeanSwing<NamedTable> _selectNamedBeanSwing;

    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;

    private JLabel _panelRowOrColumnLabel;
    private JTabbedPane _tabbedRowOrColumnPane;
    private JPanel _panelRowOrColumnName;
    private JPanel _panelRowOrColumnReference;
    private JPanel _panelRowOrColumnLocalVariable;
    private JPanel _panelRowOrColumnFormula;
    private JComboBox<String> _rowOrColumnNameComboBox;
    private JTextField _rowOrColumnNameTextField;

    private JTextField _referenceRowOrColumnTextField;
    private JTextField _localRowOrColumnVariableTextField;
    private JTextField _formulaRowOrColumnTextField;

    private JTextField _localVariable;

    @Override
    protected void createPanel(@CheckForNull Base object, @Nonnull JPanel buttonPanel) {
        if ((object != null) && !(object instanceof TableForEach)) {
            throw new IllegalArgumentException("object must be an TableForEach but is a: "+object.getClass().getName());
        }

        TableForEach action = (TableForEach)object;

        panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        _selectNamedBeanSwing = new LogixNG_SelectNamedBeanSwing<>(
                InstanceManager.getDefault(NamedTableManager.class), getJDialog(), this);

        _panelRowOrColumnLabel = new JLabel(Bundle.getMessage("TableForEachSwing_RowName"));

        _tableRowOrColumnComboBox = new JComboBox<>();
        for (TableRowOrColumn item : TableRowOrColumn.values()) {
            _tableRowOrColumnComboBox.addItem(item);
        }
        JComboBoxUtil.setupComboBoxMaxRows(_tableRowOrColumnComboBox);
        _tableRowOrColumnComboBox.addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(action != null ? action.getRowOrColumnName() : null);

            if (_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()) == TableRowOrColumn.Row) {
                _panelRowOrColumnLabel.setText(Bundle.getMessage("TableForEachSwing_RowName"));
            } else {
                _panelRowOrColumnLabel.setText(Bundle.getMessage("TableForEachSwing_ColumnName"));
            }
        });

        JPanel tableRowOrColumnPanel = new JPanel();
        tableRowOrColumnPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_RowOrColumn")));
        tableRowOrColumnPanel.add(_tableRowOrColumnComboBox);
        panel.add(tableRowOrColumnPanel);

        JPanel tabbedPanesPanel = new JPanel();
        tabbedPanesPanel.setLayout(new BoxLayout(tabbedPanesPanel, BoxLayout.X_AXIS));

        JPanel tabbedTablePane;

        if (action != null) {
            tabbedTablePane = _selectNamedBeanSwing.createPanel(action.getSelectNamedBean());
        } else {
            tabbedTablePane = _selectNamedBeanSwing.createPanel(null);
        }

        _selectNamedBeanSwing.addAddressingListener((evt) -> {
            boolean isDirectAddressing = (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct);
            _rowOrColumnNameComboBox.setVisible(isDirectAddressing);
            _rowOrColumnNameTextField.setVisible(!isDirectAddressing);
        });

        _selectNamedBeanSwing.getBeanSelectPanel().getBeanCombo().addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(action != null ? action.getRowOrColumnName() : null);
        });

        tabbedPanesPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_Table")));

        tabbedPanesPanel.add(tabbedTablePane);

        _tabbedRowOrColumnPane = new JTabbedPane();
        _panelRowOrColumnName = new javax.swing.JPanel();
        _panelRowOrColumnReference = new javax.swing.JPanel();
        _panelRowOrColumnLocalVariable = new javax.swing.JPanel();
        _panelRowOrColumnFormula = new javax.swing.JPanel();

        _tabbedRowOrColumnPane.addTab(NamedBeanAddressing.Direct.toString(), _panelRowOrColumnName);
        _tabbedRowOrColumnPane.addTab(NamedBeanAddressing.Reference.toString(), _panelRowOrColumnReference);
        _tabbedRowOrColumnPane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelRowOrColumnLocalVariable);
        _tabbedRowOrColumnPane.addTab(NamedBeanAddressing.Formula.toString(), _panelRowOrColumnFormula);

        _rowOrColumnNameComboBox = new JComboBox<>();
        _panelRowOrColumnName.add(_rowOrColumnNameComboBox);
        _rowOrColumnNameTextField = new JTextField(20);
        _rowOrColumnNameTextField.setVisible(false);
        _panelRowOrColumnName.add(_rowOrColumnNameTextField);
        JComboBoxUtil.setupComboBoxMaxRows(_rowOrColumnNameComboBox);

        _referenceRowOrColumnTextField = new JTextField();
        _referenceRowOrColumnTextField.setColumns(20);
        _panelRowOrColumnReference.add(_referenceRowOrColumnTextField);

        _localRowOrColumnVariableTextField = new JTextField();
        _localRowOrColumnVariableTextField.setColumns(20);
        _panelRowOrColumnLocalVariable.add(_localRowOrColumnVariableTextField);

        _formulaRowOrColumnTextField = new JTextField();
        _formulaRowOrColumnTextField.setColumns(20);
        _panelRowOrColumnFormula.add(_formulaRowOrColumnTextField);

        tabbedPanesPanel.add(_panelRowOrColumnLabel);

        tabbedPanesPanel.add(_tabbedRowOrColumnPane);

        panel.add(tabbedPanesPanel);

        JPanel localVariablePanel = new JPanel();
        localVariablePanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_LocalVariable")));
        _localVariable = new JTextField(20);
        localVariablePanel.add(_localVariable);
        panel.add(localVariablePanel);

        JPanel infoPanel = new JPanel();
        infoPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_Info")));
        panel.add(infoPanel);

        if (action != null) {
            _tableRowOrColumnComboBox.setSelectedItem(action.getRowOrColumn());

            switch (action.getRowOrColumnAddressing()) {
                case Direct: _tabbedRowOrColumnPane.setSelectedComponent(_panelRowOrColumnName); break;
                case Reference: _tabbedRowOrColumnPane.setSelectedComponent(_panelRowOrColumnReference); break;
                case LocalVariable: _tabbedRowOrColumnPane.setSelectedComponent(_panelRowOrColumnLocalVariable); break;
                case Formula: _tabbedRowOrColumnPane.setSelectedComponent(_panelRowOrColumnFormula); break;
                default: throw new IllegalArgumentException("invalid _rowOrColumnAddressing state: " + action.getRowOrColumnAddressing().name());
            }

            _rowOrColumnNameTextField.setText(action.getRowOrColumnName());
            _referenceRowOrColumnTextField.setText(action.getRowOrColumnReference());
            _localRowOrColumnVariableTextField.setText(action.getRowOrColumnLocalVariable());
            _formulaRowOrColumnTextField.setText(action.getRowOrColumnFormula());

            _localVariable.setText(action.getLocalVariableName());
        }
    }

    private void setupRowOrColumnNameComboBox(String rowOrColumnName) {
        if (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct) {
            _rowOrColumnNameComboBox.removeAllItems();
            NamedTable table = _selectNamedBeanSwing.getBean();
            if (table != null) {
                _rowOrColumnNameComboBox.addItem(Bundle.getMessage("TableForEach_Header"));

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
                if (rowOrColumnName == null || rowOrColumnName.isEmpty()) {    // Header row or column
                    _rowOrColumnNameComboBox.setSelectedIndex(0);
                } else {
                    _rowOrColumnNameComboBox.setSelectedItem(rowOrColumnName);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        TableForEach action = new TableForEach("IQDA1", null);

        _selectNamedBeanSwing.validate(action.getSelectNamedBean(), errorMessages);

        try {
            if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnName) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnReference) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnLocalVariable) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnFormula) {
                action.setRowOrColumnFormula(_formulaRowOrColumnTextField.getText());
                action.setRowOrColumnAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedRowOrColumnPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }

        return errorMessages.isEmpty();
    }

    /** {@inheritDoc} */
    @Override
    public MaleSocket createNewObject(@Nonnull String systemName, @CheckForNull String userName) {
        TableForEach action = new TableForEach(systemName, userName);
        updateObject(action);
        return InstanceManager.getDefault(DigitalActionManager.class).registerAction(action);
    }

    /** {@inheritDoc} */
    @Override
    public void updateObject(@Nonnull Base object) {
        if (!(object instanceof TableForEach)) {
            throw new IllegalArgumentException("object must be an TableForEach but is a: "+object.getClass().getName());
        }


        TableForEach action = (TableForEach)object;
        action.setRowOrColumn(_tableRowOrColumnComboBox.getItemAt(_tableRowOrColumnComboBox.getSelectedIndex()));
        _selectNamedBeanSwing.updateObject(action.getSelectNamedBean());

        try {
            if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnName) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.Direct);
                if (_selectNamedBeanSwing.getAddressing() == NamedBeanAddressing.Direct) {
                    if (_rowOrColumnNameComboBox.getSelectedIndex() > 0) {
                        action.setRowOrColumnName(_rowOrColumnNameComboBox.getItemAt(_rowOrColumnNameComboBox.getSelectedIndex()));
                    } else {
                        action.setRowOrColumnName("");  // Header row or column
                    }
                } else {
                    action.setRowOrColumnName(_rowOrColumnNameTextField.getText());
                }
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnReference) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.Reference);
                action.setRowOrColumnReference(_referenceRowOrColumnTextField.getText());
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnLocalVariable) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.LocalVariable);
                action.setRowOrColumnLocalVariable(_localRowOrColumnVariableTextField.getText());
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnFormula) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.Formula);
                action.setRowOrColumnFormula(_formulaRowOrColumnTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnoutState has unknown selection");
            }

            action.setLocalVariableName(_localVariable.getText());
        } catch (ParserException e) {
            throw new RuntimeException("ParserException: "+e.getMessage(), e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return Bundle.getMessage("TableForEach_Short");
    }

    @Override
    public void dispose() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TableForEachSwing.class);

}
