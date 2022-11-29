package jmri.jmrit.logixng.tools.swing;

import java.util.*;

import javax.swing.table.AbstractTableModel;

import jmri.jmrit.logixng.SymbolTable;
import jmri.jmrit.logixng.SymbolTable.Symbol;

/**
 * Table model for the current symbol table while debugging
 * @author Daniel Bergqvist Copyright 2020
 */
public class DebuggerSymbolTableModel extends AbstractTableModel {

    public static final int COLUMN_NAME = 0;
    public static final int COLUMN_VALUE = 1;

    private final List<Symbol> _symbols = new ArrayList<>();
    private SymbolTable _symbolTable = null;


    public void update(SymbolTable symbolTable) {
        _symbolTable = symbolTable;
        _symbols.clear();
        for (Symbol s : symbolTable.getSymbols().values()) {
            _symbols.add(s);
        }
        fireTableDataChanged();
    }

    /** {@inheritDoc} */
    @Override
    public int getRowCount() {
        return _symbols.size();
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount() {
        return 2;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case COLUMN_NAME:
                return Bundle.getMessage("ColumnSymbolName");
            case COLUMN_VALUE:
                return Bundle.getMessage("ColumnSymbolValue");
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {
            case COLUMN_NAME:
            case COLUMN_VALUE:
                return String.class;
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
/*
        Symbol symbol = _symbols.get(rowIndex);

        switch (columnIndex) {
            case COLUMN_VALUE:
                _conditionalNG.getStack().setValueAtIndex(symbol.getIndex(), value);
                break;
            default:
                throw new IllegalArgumentException("Invalid column");
        }
*/
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex >= _symbols.size()) throw new IllegalArgumentException("Invalid row");

        switch (columnIndex) {
            case COLUMN_NAME:
                return _symbols.get(rowIndex).getName();
            case COLUMN_VALUE:
                if (_symbolTable == null) return "";
                return _symbolTable.getValue(_symbols.get(rowIndex).getName());
            default:
                throw new IllegalArgumentException("Invalid column");
        }
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DebuggerSymbolTableModel.class);
}
