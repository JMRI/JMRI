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
import jmri.util.swing.BeanSelectPanel;
import jmri.util.swing.JComboBoxUtil;

/**
 * Configures an TableForEach object with a Swing JPanel.
 * 
 * @author Daniel Bergqvist Copyright 2021
 */
public class TableForEachSwing extends AbstractDigitalActionSwing {

    private JTabbedPane _tabbedTablePane;
    private JPanel _panelTable;
    private JPanel _panelReference;
    private JPanel _panelLocalVariable;
    private JPanel _panelFormula;
    private BeanSelectPanel<NamedTable> tableBeanPanel;
    private JComboBox<TableRowOrColumn> _tableRowOrColumnComboBox;
    private JTextField _referenceTextField;
    private JTextField _localVariableTextField;
    private JTextField _formulaTextField;
    
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
        
        _tabbedTablePane = new JTabbedPane();
        _panelTable = new javax.swing.JPanel();
        _panelReference = new javax.swing.JPanel();
        _panelLocalVariable = new javax.swing.JPanel();
        _panelFormula = new javax.swing.JPanel();
        
        _tabbedTablePane.addTab(NamedBeanAddressing.Direct.toString(), _panelTable);
        _tabbedTablePane.addTab(NamedBeanAddressing.Reference.toString(), _panelReference);
        _tabbedTablePane.addTab(NamedBeanAddressing.LocalVariable.toString(), _panelLocalVariable);
        _tabbedTablePane.addTab(NamedBeanAddressing.Formula.toString(), _panelFormula);
        
        _tabbedTablePane.addChangeListener((evt) -> {
            boolean isPanelTable = (_tabbedTablePane.getSelectedComponent() == _panelTable);
            _rowOrColumnNameComboBox.setVisible(isPanelTable);
            _rowOrColumnNameTextField.setVisible(!isPanelTable);
        });
        
        tableBeanPanel = new BeanSelectPanel<>(InstanceManager.getDefault(NamedTableManager.class), null);
        _panelTable.add(tableBeanPanel);
        
        tableBeanPanel.getBeanCombo().addActionListener((evt) -> {
            setupRowOrColumnNameComboBox(action != null ? action.getRowOrColumnName() : null);
        });
        
        _referenceTextField = new JTextField();
        _referenceTextField.setColumns(20);
        _panelReference.add(_referenceTextField);
        
        _localVariableTextField = new JTextField();
        _localVariableTextField.setColumns(20);
        _panelLocalVariable.add(_localVariableTextField);
        
        _formulaTextField = new JTextField();
        _formulaTextField.setColumns(20);
        _panelFormula.add(_formulaTextField);
        
        tabbedPanesPanel.add(new JLabel(Bundle.getMessage("TableForEachSwing_Table")));
        
        tabbedPanesPanel.add(_tabbedTablePane);
        
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
        
        if (action != null) {
            _tableRowOrColumnComboBox.setSelectedItem(action.getRowOrColumn());
            
            switch (action.getAddressing()) {
                case Direct: _tabbedTablePane.setSelectedComponent(_panelTable); break;
                case Reference: _tabbedTablePane.setSelectedComponent(_panelReference); break;
                case LocalVariable: _tabbedTablePane.setSelectedComponent(_panelLocalVariable); break;
                case Formula: _tabbedTablePane.setSelectedComponent(_panelFormula); break;
                default: throw new IllegalArgumentException("invalid _addressing state: " + action.getAddressing().name());
            }
            
            if (action.getTable() != null) {
                tableBeanPanel.setDefaultNamedBean(action.getTable().getBean());
            }
            
            _referenceTextField.setText(action.getTableReference());
            _localVariableTextField.setText(action.getTableLocalVariable());
            _formulaTextField.setText(action.getTableFormula());
            
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
        _rowOrColumnNameComboBox.removeAllItems();
        NamedTable table = tableBeanPanel.getNamedBean();
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
            _rowOrColumnNameComboBox.setSelectedItem(rowOrColumnName);
        }
    }
    
    /** {@inheritDoc} */
    @Override
    public boolean validate(@Nonnull List<String> errorMessages) {
        // Create a temporary action to test formula
        TableForEach action = new TableForEach("IQDA1", null);
        
        try {
            if (_tabbedTablePane.getSelectedComponent() == _panelTable) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedTablePane.getSelectedComponent() == _panelReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedTablePane.getSelectedComponent() == _panelLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedTablePane.getSelectedComponent() == _panelFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setTableFormula(_formulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPane has unknown selection");
            }
            
            if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnName) {
                action.setAddressing(NamedBeanAddressing.Direct);
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
            } else if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnFormula) {
                action.setRowOrColumnFormula(_formulaRowOrColumnTextField.getText());
                action.setAddressing(NamedBeanAddressing.Formula);
            } else {
                throw new IllegalArgumentException("_tabbedRowOrColumnPane has unknown selection");
            }
        } catch (ParserException e) {
            errorMessages.add("Cannot parse formula: " + e.getMessage());
            return false;
        }
        return true;
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
        
        try {
            if (_tabbedTablePane.getSelectedComponent() == _panelTable) {
                action.setAddressing(NamedBeanAddressing.Direct);
                NamedTable table = tableBeanPanel.getNamedBean();
                if (table != null) {
                    NamedBeanHandle<NamedTable> handle
                            = InstanceManager.getDefault(NamedBeanHandleManager.class)
                                    .getNamedBeanHandle(table.getDisplayName(), table);
                    action.setTable(handle);
                } else {
                    action.removeTable();
                }
            } else if (_tabbedTablePane.getSelectedComponent() == _panelReference) {
                action.setAddressing(NamedBeanAddressing.Reference);
                action.setTableReference(_referenceTextField.getText());
            } else if (_tabbedTablePane.getSelectedComponent() == _panelLocalVariable) {
                action.setAddressing(NamedBeanAddressing.LocalVariable);
                action.setTableLocalVariable(_localVariableTextField.getText());
            } else if (_tabbedTablePane.getSelectedComponent() == _panelFormula) {
                action.setAddressing(NamedBeanAddressing.Formula);
                action.setTableFormula(_formulaTextField.getText());
            } else {
                throw new IllegalArgumentException("_tabbedPaneTurnoutState has unknown selection");
            }
            
            if (_tabbedRowOrColumnPane.getSelectedComponent() == _panelRowOrColumnName) {
                action.setRowOrColumnAddressing(NamedBeanAddressing.Direct);
                if (_tabbedTablePane.getSelectedComponent() == _panelTable) {
                    if (_rowOrColumnNameComboBox.getSelectedIndex() != -1) {
                        action.setRowOrColumnName(_rowOrColumnNameComboBox.getItemAt(_rowOrColumnNameComboBox.getSelectedIndex()));
                    } else {
                        action.setRowOrColumnName("");
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
