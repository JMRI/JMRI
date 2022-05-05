package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * This action listens on some beans and runs the ConditionalNG on property change.
 *
 * @author Daniel Bergqvist Copyright 2019
 */
public class ActionListenOnBeansTable extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanType _namedBeanType = NamedBeanType.Light;
    private final LogixNG_SelectNamedBean<NamedTable> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, NamedTable.class, InstanceManager.getDefault(NamedTableManager.class), this);
    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
    private String _rowOrColumnName = "";
    private boolean _includeCellsWithoutHeader = false;
    private boolean _listenOnAllProperties = false;
    private final List<Map.Entry<NamedBean, String>> _namedBeansEntries = new ArrayList<>();

    public ActionListenOnBeansTable(String sys, String user)
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
        ActionListenOnBeansTable copy = new ActionListenOnBeansTable(sysName, userName);
        copy.setComment(getComment());
        copy.setNamedBeanType(_namedBeanType);
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setTableRowOrColumn(_tableRowOrColumn);
        copy.setRowOrColumnName(_rowOrColumnName);
        copy.setIncludeCellsWithoutHeader(_includeCellsWithoutHeader);

        for (var entry : _namedBeansEntries) {
            copy._namedBeansEntries.add(
                    new HashMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
        }

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

    public boolean getListenOnAllProperties() {
        return _listenOnAllProperties;
    }

    public void setListenOnAllProperties(boolean listenOnAllProperties) {
        _listenOnAllProperties = listenOnAllProperties;
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

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
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
        String tableName = _selectNamedBean.getDescription(locale);
        return Bundle.getMessage(locale, "ActionListenOnBeansTable_Long",
                _namedBeanType.toString(),
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                _tableRowOrColumn.toStringLowerCase(),
                _rowOrColumnName,
                tableName);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    public List<String> getItems() {
        List<String> items = new ArrayList<>();

        if (_selectNamedBean.getNamedBean() == null) {
            log.error("No table name is given");
            return items;   // The list is empty
        }
        if (_rowOrColumnName.isEmpty()) {
            log.error("rowOrColumnName is empty string");
            return items;   // The list is empty
        }

        NamedTable table = _selectNamedBean.getNamedBean().getBean();

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
                if (!_listenOnAllProperties
                        && (_namedBeanType.getPropertyName() != null)) {
                    namedBean.addPropertyChangeListener(_namedBeanType.getPropertyName(), this);
                } else {
                    namedBean.addPropertyChangeListener(this);
                }
            } else {
                log.warn("The named bean \"{}\" cannot be found in the manager for {}", item, _namedBeanType.toString());
            }
        }
        _selectNamedBean.registerListeners();
        _listenersAreRegistered = true;
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (!_listenersAreRegistered) return;

        for (Map.Entry<NamedBean, String> namedBeanEntry : _namedBeansEntries) {
            if (!_listenOnAllProperties
                    && (namedBeanEntry.getValue() != null)) {
                namedBeanEntry.getKey().removePropertyChangeListener(namedBeanEntry.getValue(), this);
            } else {
                namedBeanEntry.getKey().removePropertyChangeListener(this);
            }
            namedBeanEntry.getKey().removePropertyChangeListener(namedBeanEntry.getValue(), this);
        }
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionListenOnBeansTable.class);

}
