package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * This action finds a table row or column.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionFindTableRowOrColumn extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<NamedTable> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, NamedTable.class, InstanceManager.getDefault(NamedTableManager.class), this);
    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
    private String _rowOrColumnName = "";
    private boolean _includeCellsWithoutHeader = false;
    private String _localVariableNamedBean;
    private String _localVariableRow;

    public ActionFindTableRowOrColumn(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _selectNamedBean.setOnlyDirectAddressingAllowed();
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionFindTableRowOrColumn copy = new ActionFindTableRowOrColumn(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setTableRowOrColumn(_tableRowOrColumn);
        copy.setRowOrColumnName(_rowOrColumnName);
        copy.setIncludeCellsWithoutHeader(_includeCellsWithoutHeader);

        copy.setLocalVariableNamedBean(_localVariableNamedBean);
        copy.setLocalVariableRow(_localVariableRow);

        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<NamedTable> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /**
     * Get tableRowOrColumn.
     * @return tableRowOrColumn
     */
    public TableRowOrColumn getTableRowOrColumn() {
        return _tableRowOrColumn;
    }

    /**
     * Set tableRowOrColumn.
     * @param tableRowOrColumn tableRowOrColumn
     */
    public void setTableRowOrColumn(@Nonnull TableRowOrColumn tableRowOrColumn) {
        _tableRowOrColumn = tableRowOrColumn;
    }

    /**
     * Get name of row or column
     * @return name of row or column
     */
    public String getRowOrColumnName() {
        return _rowOrColumnName;
    }

    /**
     * Set name of row or column
     * @param rowOrColumnName name of row or column
     */
    public void setRowOrColumnName(@Nonnull String rowOrColumnName) {
        if (rowOrColumnName == null) throw new IllegalArgumentException("Row/column name is null");
        _rowOrColumnName = rowOrColumnName;
    }

    /**
     * Set whenever to include cells that doesn't have a header.
     * Cells without headers can be used to use some cells in the table
     * as comments.
     * @return true if include cells that doesn't have a header, false otherwise
     */
    public boolean getIncludeCellsWithoutHeader() {
        return _includeCellsWithoutHeader;
    }

    /**
     * Set whenever to include cells that doesn't have a header.
     * Cells without headers can be used to use some cells in the table
     * as comments.
     * @param includeCellsWithoutHeader true if include rows/columns that
     *                                  doesn't have a header, false otherwise
     */
    public void setIncludeCellsWithoutHeader(boolean includeCellsWithoutHeader) {
        _includeCellsWithoutHeader = includeCellsWithoutHeader;
    }

    public void setLocalVariableNamedBean(String localVariableNamedBean) {
        if ((localVariableNamedBean != null) && (!localVariableNamedBean.isEmpty())) {
            this._localVariableNamedBean = localVariableNamedBean;
        } else {
            this._localVariableNamedBean = null;
        }
    }

    public String getLocalVariableNamedBean() {
        return _localVariableNamedBean;
    }

    public void setLocalVariableRow(String localVariableNewValue) {
        if ((localVariableNewValue != null) && (!localVariableNewValue.isEmpty())) {
            this._localVariableRow = localVariableNewValue;
        } else {
            this._localVariableRow = null;
        }
    }

    public String getLocalVariableRow() {
        return _localVariableRow;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    private Map<String, Object> getRow(Object value) throws JmriException {
        if (_selectNamedBean.getNamedBean() == null) {
            log.error("No table name is given");
            return null;    // No row found
        }
        if (_rowOrColumnName.isEmpty()) {
            log.error("rowOrColumnName is empty string");
            return null;    // No row found
        }

        NamedTable table = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (_tableRowOrColumn == TableRowOrColumn.Row) {
            int row = table.getRowNumber(_rowOrColumnName);
            for (int column=1; column <= table.numColumns(); column++) {
                // If the header is null or empty, treat the row as a comment
                // unless _includeRowColumnWithoutHeader is true
                Object header = table.getCell(0, column);
//                System.out.format("Row header: %s%n", header);
                if (_includeCellsWithoutHeader
                        || ((header != null) && (!header.toString().isEmpty()))) {
                    Object cell = table.getCell(row, column);
                    if ((cell != null) && (cell.equals(value))) {
                        Map<String, Object> rowData = new HashMap<>();

                        for (int rowIndex=1; rowIndex <= table.numRows(); rowIndex++) {
                            Object subHeader = table.getCell(rowIndex, 0);
                            if ((subHeader != null) && (!subHeader.toString().isEmpty())) {
                                rowData.put(subHeader.toString(), table.getCell(rowIndex, column));
                            }
                        }
                        return rowData;
                    }
                }
            }
        } else {
            int column = table.getColumnNumber(_rowOrColumnName);
            for (int row=1; row <= table.numRows(); row++) {
                // If the header is null or empty, treat the row as a comment
                // unless _includeRowColumnWithoutHeader is true
                Object header = table.getCell(row, 0);
//                System.out.format("Column header: %s%n", header);
                if (_includeCellsWithoutHeader
                        || ((header != null) && (!header.toString().isEmpty()))) {
                    Object cell = table.getCell(row, column);
                    if ((cell != null) && (cell.equals(value))) {
                        Map<String, Object> columnData = new HashMap<>();

                        for (int colIndex=1; colIndex <= table.numColumns(); colIndex++) {
                            Object subHeader = table.getCell(0, colIndex);
                            if ((subHeader != null) && (!subHeader.toString().isEmpty())) {
                                columnData.put(subHeader.toString(), table.getCell(row, colIndex));
                            }
                        }
                        return columnData;
                    }
                }
            }
        }
        return null;    // No row found
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        SymbolTable symbolTable = getConditionalNG().getSymbolTable();
        Object value;
        if ((_localVariableNamedBean != null) && (_localVariableRow != null)) {
            value = symbolTable.getValue(_localVariableNamedBean);
            symbolTable.setValue(_localVariableRow, getRow(value));
        }
    }

    @Override
    public FemaleSocket getChild(int index) throws IllegalArgumentException, UnsupportedOperationException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public int getChildCount() {
        return 0;
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionFindTableRowOrColumn_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String tableName = _selectNamedBean.getDescription(locale);
        return Bundle.getMessage(locale, "ActionFindTableRowOrColumn_Long",
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                tableName,
                _tableRowOrColumn.toStringLowerCase(),
                _rowOrColumnName,
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                _localVariableNamedBean);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (_listenersAreRegistered) return;

        _selectNamedBean.registerListeners();
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (!_listenersAreRegistered) return;

        _selectNamedBean.unregisterListeners();
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
//        System.out.format("Table: Property: %s, Bean: %s, Listen: %b%n", evt.getPropertyName(), ((NamedBean)evt.getSource()).getDisplayName(), _listenOnAllProperties);
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
/*
        log.debug("getUsageReport :: ActionListenOnBeans: bean = {}, report = {}", cdl, report);
        for (NamedBeanReference namedBeanReference : _namedBeanReferences.values()) {
            if (namedBeanReference._handle != null) {
                if (bean.equals(namedBeanReference._handle.getBean())) {
                    report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
                }
            }
        }
*/
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionFindTableRowOrColumn.class);

}
