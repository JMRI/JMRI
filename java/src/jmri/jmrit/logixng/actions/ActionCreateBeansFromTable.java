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
 * @author Daniel Bergqvist Copyright 2022
 */
public class ActionCreateBeansFromTable extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private boolean _onlyCreatableTypes = true;
    private NamedBeanType _namedBeanType = NamedBeanType.Light;
    private final LogixNG_SelectNamedBean<NamedTable> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, NamedTable.class, InstanceManager.getDefault(NamedTableManager.class), this);
    private TableRowOrColumn _tableRowOrColumn = TableRowOrColumn.Row;
    private String _rowOrColumnSystemName = "";
    private String _rowOrColumnUserName = "";
    private boolean _includeCellsWithoutHeader = false;
    private final List<Map.Entry<NamedBean, String>> _namedBeansEntries = new ArrayList<>();
    private boolean _moveUserName = false;
    private boolean _updateToUserName = false;
    private boolean _removeOldBean = false;

    public ActionCreateBeansFromTable(String sys, String user)
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
        ActionCreateBeansFromTable copy = new ActionCreateBeansFromTable(sysName, userName);
        copy.setComment(getComment());
        copy.setOnlyCreatableTypes(_onlyCreatableTypes);
        copy.setNamedBeanType(_namedBeanType);
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setTableRowOrColumn(_tableRowOrColumn);
        copy.setRowOrColumnSystemName(_rowOrColumnSystemName);
        copy.setRowOrColumnUserName(_rowOrColumnUserName);
        copy.setIncludeCellsWithoutHeader(_includeCellsWithoutHeader);
        copy.setMoveUserName(_moveUserName);
        copy.setUpdateToUserName(_updateToUserName);
        copy.setRemoveOldBean(_removeOldBean);

        for (var entry : _namedBeansEntries) {
            copy._namedBeansEntries.add(
                    new HashMap.SimpleEntry<>(entry.getKey(), entry.getValue()));
        }

        return manager.registerAction(copy);
    }

    /**
     * Get whenever to show only types that can be created with this action.
     * @return true if show only types that can be created, false otherwise
     */
    public boolean isOnlyCreatableTypes() {
        return _onlyCreatableTypes;
    }

    /**
     * Set whenever to show only types that can be created with this action.
     * @param onlyCreatableTypes true show only types that can be created,
     *                           false otherwise
     */
    public void setOnlyCreatableTypes(boolean onlyCreatableTypes) {
        _onlyCreatableTypes = onlyCreatableTypes;
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
    public String getRowOrColumnSystemName() {
        return _rowOrColumnSystemName;
    }

    /**
     * Set name of row or column
     * @param rowOrColumnName name of row or column
     */
    public void setRowOrColumnSystemName(@Nonnull String rowOrColumnName) {
        if (rowOrColumnName == null) throw new IllegalArgumentException("Row/column name is null");
        _rowOrColumnSystemName = rowOrColumnName;
    }

    /**
     * Get name of row or column
     * @return name of row or column
     */
    public String getRowOrColumnUserName() {
        return _rowOrColumnUserName;
    }

    /**
     * Set name of row or column
     * @param rowOrColumnName name of row or column
     */
    public void setRowOrColumnUserName(@Nonnull String rowOrColumnName) {
        if (rowOrColumnName == null) throw new IllegalArgumentException("Row/column name is null");
        _rowOrColumnUserName = rowOrColumnName;
    }

    /**
     * Get whenever to include cells that doesn't have a header.
     * Cells without headers can be used to use some cells in the table
     * as comments.
     * @return true if include cells that doesn't have a header, false otherwise
     */
    public boolean isIncludeCellsWithoutHeader() {
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

    /**
     * Get whenever to move the user name to the new bean.
     * @return true if username should be moved, false otherwise
     */
    public boolean isMoveUserName() {
        return _moveUserName;
    }

    /**
     * Set whenever to move the user name to the new bean.
     * @param isMoveUserName true if username should be moved, false otherwise
     */
    public void setMoveUserName(boolean isMoveUserName) {
        _moveUserName = isMoveUserName;
    }

    /**
     * Get whenever to use the user name for beans that already uses the system name.
     * @return true if update beans to use user name, false otherwise
     */
    public boolean isUpdateToUserName() {
        return _updateToUserName;
    }

    /**
     * Set whenever to use the user name for beans that already uses the system name.
     * @param updateToUserName true if update beans to use user name, false otherwise
     */
    public void setUpdateToUserName(boolean updateToUserName) {
        _updateToUserName = updateToUserName;
    }

    /**
     * Get whenever to remove the old bean.
     * @return true if remove old bean, false otherwise
     */
    public boolean isRemoveOldBean() {
        return _removeOldBean;
    }

    /**
     * Set whenever to remove the old bean.
     * @param removeOldBean true if remove old bean, false otherwise
     */
    public void setRemoveOldBean(boolean removeOldBean) {
        _removeOldBean = removeOldBean;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    private List<BeanName> getItems() {
        List<BeanName> items = new ArrayList<>();

        if (_selectNamedBean.getNamedBean() == null) {
            log.error("No table name is given");
            return items;   // The list is empty
        }
        if (_rowOrColumnSystemName.isEmpty()) {
            log.error("rowOrColumnSystemName is empty string");
            return items;   // The list is empty
        }

        NamedTable table = _selectNamedBean.getBean();

        if (_tableRowOrColumn == TableRowOrColumn.Row) {
            int systemNameRow = table.getRowNumber(_rowOrColumnSystemName);
            int userNameRow = table.getRowNumber(_rowOrColumnUserName);
            for (int column=1; column <= table.numColumns(); column++) {
                // If the header is null or empty, treat the row as a comment
                // unless _includeRowColumnWithoutHeader is true
                Object header = table.getCell(0, column);
//                System.out.format("Row header: %s%n", header);
                if (_includeCellsWithoutHeader
                        || ((header != null) && (!header.toString().isEmpty()))) {
                    Object systemNameCell = table.getCell(systemNameRow, column);
                    Object userNameCell = table.getCell(userNameRow, column);
                    if (systemNameCell != null && !systemNameCell.toString().isBlank()) {
                        if (userNameCell != null && !userNameCell.toString().isBlank()) {
                            items.add(new BeanName(systemNameCell.toString(), userNameCell.toString()));
                        } else {
                            items.add(new BeanName(systemNameCell.toString(), null));
                        }
                    }
                }
            }
        } else {
            int systemNameColumn = table.getColumnNumber(_rowOrColumnSystemName);
            int userNameColumn = table.getColumnNumber(_rowOrColumnUserName);
            for (int row=1; row <= table.numRows(); row++) {
                // If the header is null or empty, treat the row as a comment
                // unless _includeRowColumnWithoutHeader is true
                Object header = table.getCell(row, 0);
//                System.out.format("Column header: %s%n", header);
                if (_includeCellsWithoutHeader
                        || ((header != null) && (!header.toString().isEmpty()))) {
                    Object systemNameCell = table.getCell(row, systemNameColumn);
                    Object userNameCell = table.getCell(row, userNameColumn);
                    if (systemNameCell != null && !systemNameCell.toString().isBlank()) {
                        if (userNameCell != null && !userNameCell.toString().isBlank()) {
                            items.add(new BeanName(systemNameCell.toString(), userNameCell.toString()));
                        } else {
                            items.add(new BeanName(systemNameCell.toString(), null));
                        }
                    }
                }
            }
        }
        return items;
    }

    private void moveUserName(
            NamedBean oldNameBean,
            NamedBean newNameBean,
            String userName)
            throws JmriException {

        NamedBeanHandleManager nbMan = InstanceManager.getDefault(NamedBeanHandleManager.class);

        if (nbMan.inUse(oldNameBean.getSystemName(), oldNameBean)) {
            if (_updateToUserName) {
                nbMan.updateBeanFromSystemToUser(oldNameBean);
            }
        }

        oldNameBean.setUserName(null);
        newNameBean.setUserName(userName);
        nbMan.moveBean(oldNameBean, newNameBean, userName);
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        List<BeanName> items = getItems();
        for (BeanName beanName : items) {
            NamedBean sysBean = _namedBeanType.getManager().getBySystemName(beanName._systemName);
            NamedBean userBean = null;
            if (beanName._userName != null && !beanName._userName.isBlank()) {
                userBean = _namedBeanType.getManager().getByUserName(beanName._userName);
            }

            // Create new bean if it doesn't exists
            if (sysBean == null) {
                if (_namedBeanType.getCreateBean() == null) {
                    throw new JmriException(Bundle.getMessage(
                            "ActionCreateBeansFromTable_Exception_CreateBeanNotSupported",
                            _namedBeanType.getName(true)));
                }

                String userName = userBean != null ? null : beanName._userName;
                try {
                    sysBean = _namedBeanType.getCreateBean().createBean(beanName._systemName, userName);
                } catch (IllegalArgumentException e) {
                    throw new JmriException(Bundle.getMessage(
                            "ActionCreateBeansFromTable_Exception_CantCreateBean2",
                            beanName._systemName, e.getLocalizedMessage()));
                }
                if (sysBean == null) {
                    throw new JmriException(Bundle.getMessage(
                            "ActionCreateBeansFromTable_Exception_CantCreateBean",
                            beanName._systemName));
                }
            }

            if (userBean == null || sysBean == userBean) continue;

            if (!_moveUserName) {
                throw new JmriException(Bundle.getMessage("ActionCreateBeansFromTable_Exception_CantMoveUserName"));
            }

            moveUserName(userBean, sysBean, beanName._userName);

            // Remove old bean if desired
            if (_removeOldBean) {
                try {
                    _namedBeanType.getDeleteBean().deleteBean(userBean, "CanDelete");
                } catch (java.beans.PropertyVetoException e) {
                    if (e.getPropertyChangeEvent().getPropertyName().equals("DoNotDelete")) { // NOI18N
                        throw new JmriException(String.format("Cannot delete bean: %s", e.getPropertyChangeEvent().getOldValue()), e);
                    }
                }
                try {
                    _namedBeanType.getDeleteBean().deleteBean(userBean, "DoDelete");
                } catch (java.beans.PropertyVetoException e) {
                    throw new JmriException(String.format("Cannot delete bean: %s", e.getPropertyChangeEvent().getOldValue()), e);
                }
            }
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
        return Bundle.getMessage(locale, "ActionCreateBeansFromTable_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String tableName = _selectNamedBean.getDescription(locale);
        String includeCellsWithoutHeaderStr = _includeCellsWithoutHeader
                ? Bundle.getMessage(locale, "ActionCreateBeansFromTable_FlagStr",
                        Bundle.getMessage(locale, "ActionCreateBeansFromTable_IncludeCellsWithoutHeader"))
                : "";
        String includeMoveUserNameStr = _moveUserName
                ? Bundle.getMessage(locale, "ActionCreateBeansFromTable_FlagStr",
                        Bundle.getMessage(locale, "ActionCreateBeansFromTable_MoveUserName"))
                : "";
        String updateToUserNameStr = _updateToUserName
                ? Bundle.getMessage(locale, "ActionCreateBeansFromTable_FlagStr",
                        Bundle.getMessage(locale, "ActionCreateBeansFromTable_UpdateToUserName"))
                : "";
        String includeRemoveOldBeanStr = _removeOldBean
                ? Bundle.getMessage(locale, "ActionCreateBeansFromTable_FlagStr",
                        Bundle.getMessage(locale, "ActionCreateBeansFromTable_RemoveOldBean"))
                : "";

        return Bundle.getMessage(locale, "ActionCreateBeansFromTable_Long",
                _namedBeanType.getName(true).toLowerCase(),
                tableName,
                _tableRowOrColumn.getOpposite().toStringLowerCase(),
                _tableRowOrColumn.toStringLowerCase(),
                _rowOrColumnSystemName,
                _rowOrColumnUserName,
                includeCellsWithoutHeaderStr,
                includeMoveUserNameStr,
                updateToUserNameStr,
                includeRemoveOldBeanStr);
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
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ActionCreateBeansFromTable: bean = {}, report = {}", cdl, report);
        if (_selectNamedBean.getBean() != null) {
            if (bean.equals(_selectNamedBean.getBean())) {
                report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
            }
        }
    }


    private static class BeanName {
        final String _systemName;
        final String _userName;

        BeanName(String systemName, String userName) {
            _systemName = systemName;
            _userName = userName;
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionCreateBeansFromTable.class);

}
