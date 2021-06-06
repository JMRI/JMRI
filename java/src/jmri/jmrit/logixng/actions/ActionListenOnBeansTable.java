package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;

/**
 * This action listens on some beans and runs the ConditionalNG on property change.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionListenOnBeansTable extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanType _namedBeanType = NamedBeanType.Light;
    private NamedBeanHandle<NamedTable> _tableHandle;
    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
    private String _rowOrColumnName = "";
    private boolean _includeCellsWithoutHeader = false;
    private final List<Map.Entry<NamedBean, String>> _namedBeansEntries = new ArrayList<>();

    public ActionListenOnBeansTable(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionListenOnBeansTable copy = new ActionListenOnBeansTable(sysName, userName);
        copy.setComment(getComment());
        copy.setNamedBeanType(_namedBeanType);
        copy.setTable(_tableHandle);
        copy.setTableRowOrColumn(_tableRowOrColumn);
        copy.setIncludeCellsWithoutHeader(_includeCellsWithoutHeader);
        return manager.registerAction(copy);
    }

    /**
     * Get the type of the named beans
     * @return the type of named beans
     */
    public NamedBeanType getNamedBeanType() {
        return _namedBeanType;
    }
    
    /**
     * Set the type of the named beans
     * @param namedBeanType the type of the named beans
     */
    public void setNamedBeanType(@Nonnull NamedBeanType namedBeanType) {
        if (namedBeanType == null) throw new RuntimeException("Daniel");
        _namedBeanType = namedBeanType;
    }
    
    public void setTable(@Nonnull String tableName) {
        assertListenersAreNotRegistered(log, "setTable");
        NamedTable table = InstanceManager.getDefault(NamedTableManager.class).getNamedTable(tableName);
        if (table != null) {
            setTable(table);
        } else {
            removeTable();
            log.error("table \"{}\" is not found", tableName);
        }
    }
    
    public void setTable(@Nonnull NamedBeanHandle<NamedTable> handle) {
        assertListenersAreNotRegistered(log, "setTable");
        _tableHandle = handle;
        InstanceManager.getDefault(NamedTableManager.class).addVetoableChangeListener(this);
    }
    
    public void setTable(@Nonnull NamedTable table) {
        assertListenersAreNotRegistered(log, "setTable");
        setTable(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(table.getDisplayName(), table));
    }
    
    public void removeTable() {
        assertListenersAreNotRegistered(log, "setTable");
        if (_tableHandle != null) {
            InstanceManager.getDefault(NamedTableManager.class).removeVetoableChangeListener(this);
            _tableHandle = null;
        }
    }
    
    public NamedBeanHandle<NamedTable> getTable() {
        return _tableHandle;
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
    
    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    throw new PropertyVetoException(getDisplayName(), evt);
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    removeTable();
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isExternal() {
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() {
        // Do nothing.
        // The purpose of this action is only to listen on property changes
        // of the registered beans and execute the ConditionalNG when it
        // happens.
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
        return Bundle.getMessage(locale, "ActionListenOnBeansTable_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ActionListenOnBeansTable_Long",
                _namedBeanType.toString(),
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                _tableRowOrColumn.toStringLowerCase(),
                _rowOrColumnName,
                getTable() != null ? getTable().getName() : "");
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    public List<String> getItems() {
        List<String> items = new ArrayList<>();
        
        if (_tableHandle == null) {
            log.error("tableHandle is null");
            return items;   // The list is empty
        }
        if (_rowOrColumnName.isEmpty()) {
            log.error("rowOrColumnName is empty string");
            return items;   // The list is empty
        }
        
        NamedTable table = _tableHandle.getBean();
        
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
                    if (cell != null) items.add(cell.toString());
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
                    if (cell != null && !cell.toString().isEmpty()) items.add(cell.toString());
                }
            }
        }
        return items;
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (_listenersAreRegistered) return;

        List<String> items = getItems();
        
        for (String item : items) {
            NamedBean namedBean = _namedBeanType.getManager().getNamedBean(item);
            
            if (namedBean != null) {
                Map.Entry<NamedBean, String> namedBeanEntry =
                        new HashMap.SimpleEntry<>(namedBean, _namedBeanType.getPropertyName());

                _namedBeansEntries.add(namedBeanEntry);
                namedBean.addPropertyChangeListener(_namedBeanType.getPropertyName(), this);
            } else {
                log.warn("The named bean \"{}\" cannot be found in the manager for {}", item, _namedBeanType.toString());
            }
        }
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (!_listenersAreRegistered) return;

        for (Map.Entry<NamedBean, String> namedBeanEntry : _namedBeansEntries) {
            namedBeanEntry.getKey().removePropertyChangeListener(namedBeanEntry.getValue(), this);
        }
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansTable.class);

}
