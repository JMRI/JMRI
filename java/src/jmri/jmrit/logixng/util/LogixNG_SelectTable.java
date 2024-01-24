package jmri.jmrit.logixng.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.AbstractBase;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Select table for LogixNG actions and expressions.
 * @author Daniel Bergqvist (C) 2022
 */
public class LogixNG_SelectTable implements VetoableChangeListener {

    private final AbstractBase _base;
    private final InUse _inUse;

    private NamedBeanAddressing _tableNameAddressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<NamedTable> _tableHandle;
    private String _tableNameReference = "";
    private NamedBeanHandle<Memory> _tableNameMemoryHandle;
    private String _tableNameLocalVariable = "";
    private String _tableNameFormula = "";
    private ExpressionNode _tableNameExpressionNode;
    private LogixNG_SelectTable _tableNameSelectTable;

    private NamedBeanAddressing _tableRowAddressing = NamedBeanAddressing.Direct;
    private String _tableRowName = "";
    private String _tableRowReference = "";
    private NamedBeanHandle<Memory> _tableRowMemoryHandle;
    private String _tableRowLocalVariable = "";
    private String _tableRowFormula = "";
    private ExpressionNode _tableRowExpressionNode;
    private LogixNG_SelectTable _tableRowSelectTable;

    private NamedBeanAddressing _tableColumnAddressing = NamedBeanAddressing.Direct;
    private String _tableColumnName = "";
    private String _tableColumnReference = "";
    private NamedBeanHandle<Memory> _tableColumnMemoryHandle;
    private String _tableColumnLocalVariable = "";
    private String _tableColumnFormula = "";
    private ExpressionNode _tableColumnExpressionNode;
    private LogixNG_SelectTable _tableColumnSelectTable;


    public LogixNG_SelectTable(AbstractBase base, InUse inUse) {
        _base = base;
        _inUse = inUse;
    }


    public void copy(LogixNG_SelectTable copy) throws ParserException {
        copy.setTableNameAddressing(_tableNameAddressing);
        if (_tableHandle != null) copy.setTable(_tableHandle);
        copy.setTableNameLocalVariable(_tableNameLocalVariable);
        copy.setTableNameReference(_tableNameReference);
        copy.setTableNameMemory(_tableNameMemoryHandle);
        copy.setTableNameFormula(_tableNameFormula);
        if (_tableNameSelectTable != null) {
            _tableNameSelectTable.copy(copy._tableNameSelectTable);
        }

        copy.setTableRowAddressing(_tableRowAddressing);
        copy.setTableRowName(_tableRowName);
        copy.setTableRowLocalVariable(_tableRowLocalVariable);
        copy.setTableRowReference(_tableRowReference);
        copy.setTableRowMemory(_tableRowMemoryHandle);
        copy.setTableRowFormula(_tableRowFormula);
        if (_tableRowSelectTable != null) {
            _tableRowSelectTable.copy(copy._tableRowSelectTable);
        }

        copy.setTableColumnAddressing(_tableColumnAddressing);
        copy.setTableColumnName(_tableColumnName);
        copy.setTableColumnLocalVariable(_tableColumnLocalVariable);
        copy.setTableColumnReference(_tableColumnReference);
        copy.setTableColumnMemory(_tableColumnMemoryHandle);
        copy.setTableColumnFormula(_tableColumnFormula);
        if (_tableColumnSelectTable != null) {
            _tableColumnSelectTable.copy(copy._tableColumnSelectTable);
        }
    }

    public void setTableNameAddressing(@Nonnull NamedBeanAddressing addressing) {
        this._tableNameAddressing = addressing;
        synchronized(this) {
            if ((_tableNameAddressing == NamedBeanAddressing.Table) && (_tableNameSelectTable == null)) {
                _tableNameSelectTable = new LogixNG_SelectTable(_base, _inUse);
            }
        }
    }

    public NamedBeanAddressing getTableNameAddressing() {
        return _tableNameAddressing;
    }

    public void setTable(@Nonnull NamedBeanHandle<NamedTable> handle) {
        _base.assertListenersAreNotRegistered(log, "setTable");
        _tableHandle = handle;
        InstanceManager.getDefault(NamedTableManager.class).addVetoableChangeListener(this);
    }

    public void setTable(@Nonnull NamedTable table) {
        _base.assertListenersAreNotRegistered(log, "setTable");
        setTable(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(table.getDisplayName(), table));
    }

    public void removeTable() {
        _base.assertListenersAreNotRegistered(log, "setTable");
        if (_tableHandle != null) {
            InstanceManager.getDefault(NamedTableManager.class).removeVetoableChangeListener(this);
            _tableHandle = null;
        }
    }

    public NamedBeanHandle<NamedTable> getTable() {
        return _tableHandle;
    }

    public void setTableNameReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableNameReference = reference;
    }

    public String getTableNameReference() {
        return _tableNameReference;
    }

    public void setTableNameMemory(@Nonnull String memoryName) {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setTableNameMemory(memory);
        } else {
            removeTableNameMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setTableNameMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        _tableNameMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setTableNameMemory(@Nonnull Memory memory) {
        setTableNameMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeTableNameMemory() {
        if (_tableNameMemoryHandle != null) {
            _tableNameMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getTableNameMemory() {
        return _tableNameMemoryHandle;
    }

    public void setTableNameLocalVariable(@Nonnull String localVariable) {
        _tableNameLocalVariable = localVariable;
    }

    public String getTableNameLocalVariable() {
        return _tableNameLocalVariable;
    }

    public void setTableNameFormula(@Nonnull String formula) throws ParserException {
        _tableNameFormula = formula;
        parseTableNameFormula();
    }

    public String getTableNameFormula() {
        return _tableNameFormula;
    }

    private void parseTableNameFormula() throws ParserException {
        if (_tableNameAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableNameExpressionNode = parser.parseExpression(_tableNameFormula);
        } else {
            _tableNameExpressionNode = null;
        }
    }

    public LogixNG_SelectTable getSelectTableName() {
        synchronized(this) {
            if (_tableNameSelectTable == null) {
                _tableNameSelectTable = new LogixNG_SelectTable(_base, _inUse);
            }
        }
        return _tableNameSelectTable;
    }

    public void setTableRowAddressing(@Nonnull NamedBeanAddressing addressing) {
        this._tableRowAddressing = addressing;
        synchronized(this) {
            if ((_tableRowAddressing == NamedBeanAddressing.Table) && (_tableRowSelectTable == null)) {
                _tableRowSelectTable = new LogixNG_SelectTable(_base, _inUse);
            }
        }
    }

    public NamedBeanAddressing getTableRowAddressing() {
        return _tableRowAddressing;
    }

    /**
     * Get name of row
     * @return name
     */
    public String getTableRowName() {
        return _tableRowName;
    }

    /**
     * Set name of column
     * @param rowName name
     */
    public void setTableRowName(@Nonnull String rowName) {
        _tableRowName = rowName;
    }

    public void setTableRowReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableRowReference = reference;
    }

    public String getTableRowReference() {
        return _tableRowReference;
    }

    public void setTableRowMemory(@Nonnull String memoryName) {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setTableRowMemory(memory);
        } else {
            removeTableRowMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setTableRowMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        _tableRowMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setTableRowMemory(@Nonnull Memory memory) {
        setTableRowMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeTableRowMemory() {
        if (_tableRowMemoryHandle != null) {
            _tableRowMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getTableRowMemory() {
        return _tableRowMemoryHandle;
    }

    public void setTableRowLocalVariable(@Nonnull String localVariable) {
        _tableRowLocalVariable = localVariable;
    }

    public String getTableRowLocalVariable() {
        return _tableRowLocalVariable;
    }

    public void setTableRowFormula(@Nonnull String formula) throws ParserException {
        _tableRowFormula = formula;
        parseTableRowFormula();
    }

    public String getTableRowFormula() {
        return _tableRowFormula;
    }

    private void parseTableRowFormula() throws ParserException {
        if (_tableRowAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableRowExpressionNode = parser.parseExpression(_tableRowFormula);
        } else {
            _tableRowExpressionNode = null;
        }
    }

    public LogixNG_SelectTable getSelectTableRow() {
        synchronized(this) {
            if (_tableRowSelectTable == null) {
                _tableRowSelectTable = new LogixNG_SelectTable(_base, _inUse);
            }
        }
        return _tableRowSelectTable;
    }

    public void setTableColumnAddressing(@Nonnull NamedBeanAddressing addressing) {
        this._tableColumnAddressing = addressing;
        synchronized(this) {
            if ((_tableColumnAddressing == NamedBeanAddressing.Table) && (_tableColumnSelectTable == null)) {
                _tableColumnSelectTable = new LogixNG_SelectTable(_base, _inUse);
            }
        }
    }

    public NamedBeanAddressing getTableColumnAddressing() {
        return _tableColumnAddressing;
    }

    public void setTableColumnReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _tableColumnReference = reference;
    }

    public String getTableColumnReference() {
        return _tableColumnReference;
    }

    public void setTableColumnMemory(@Nonnull String memoryName) {
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setTableColumnMemory(memory);
        } else {
            removeTableColumnMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setTableColumnMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        _tableColumnMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setTableColumnMemory(@Nonnull Memory memory) {
        setTableColumnMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeTableColumnMemory() {
        if (_tableColumnMemoryHandle != null) {
            _tableColumnMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getTableColumnMemory() {
        return _tableColumnMemoryHandle;
    }

    public void setTableColumnLocalVariable(@Nonnull String localVariable) {
        _tableColumnLocalVariable = localVariable;
    }

    public String getTableColumnLocalVariable() {
        return _tableColumnLocalVariable;
    }

    public void setTableColumnFormula(@Nonnull String formula) throws ParserException {
        _tableColumnFormula = formula;
        parseTableColumnFormula();
    }

    public String getTableColumnFormula() {
        return _tableColumnFormula;
    }

    private void parseTableColumnFormula() throws ParserException {
        if (_tableColumnAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _tableColumnExpressionNode = parser.parseExpression(_tableColumnFormula);
        } else {
            _tableColumnExpressionNode = null;
        }
    }

    /**
     * Get name of column
     * @return name
     */
    public String getTableColumnName() {
        return _tableColumnName;
    }

    /**
     * Set name of column
     * @param columnName name
     */
    public void setTableColumnName(@Nonnull String columnName) {
        _tableColumnName = columnName;
    }

    public LogixNG_SelectTable getSelectTableColumn() {
        synchronized(this) {
            if (_tableColumnSelectTable == null) {
                _tableColumnSelectTable = new LogixNG_SelectTable(_base, _inUse);
            }
        }
        return _tableColumnSelectTable;
    }

    private void addRemoveVetoListener() {
        if ((_tableNameMemoryHandle != null) || (_tableRowMemoryHandle != null) || (_tableColumnMemoryHandle != null)) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName()) && _inUse.isInUse()) { // No I18N
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    throw new PropertyVetoException(_base.getDisplayName(), evt);
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                boolean doVeto = false;
                if ((_tableNameAddressing == NamedBeanAddressing.Memory) && (_tableNameMemoryHandle != null) && evt.getOldValue().equals(_tableNameMemoryHandle.getBean())) {
                    doVeto = true;
                }
                if ((_tableRowAddressing == NamedBeanAddressing.Memory) && (_tableRowMemoryHandle != null) && evt.getOldValue().equals(_tableRowMemoryHandle.getBean())) {
                    doVeto = true;
                }
                if ((_tableColumnAddressing == NamedBeanAddressing.Memory) && (_tableColumnMemoryHandle != null) && evt.getOldValue().equals(_tableColumnMemoryHandle.getBean())) {
                    doVeto = true;
                }
                if (doVeto) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("MemoryInUseMemoryExpressionVeto", _base.getDisplayName()), e); // NOI18N
                }
            }
        } else if ("DoDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof NamedTable) {
                if (evt.getOldValue().equals(getTable().getBean())) {
                    removeTable();
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(_tableNameMemoryHandle.getBean())) {
                    removeTableNameMemory();
                }
                if (evt.getOldValue().equals(_tableRowMemoryHandle.getBean())) {
                    removeTableRowMemory();
                }
                if (evt.getOldValue().equals(_tableColumnMemoryHandle.getBean())) {
                    removeTableColumnMemory();
                }
            }
        }
    }

    private NamedTable evaluateTableBean(ConditionalNG conditionalNG) throws JmriException {

        if (_tableNameAddressing == NamedBeanAddressing.Direct) {
            return _tableHandle != null ? _tableHandle.getBean() : null;
        } else {
            String name;

            switch (_tableNameAddressing) {
                case Reference:
                    name = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _tableNameReference);
                    break;

                case Memory:
                    name = TypeConversionUtil
                            .convertToString(_tableNameMemoryHandle.getBean().getValue(), false);
                    break;

                case LocalVariable:
                    SymbolTable symbolTable = conditionalNG.getSymbolTable();
                    name = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_tableNameLocalVariable), false);
                    break;

                case Formula:
                    name = _tableNameExpressionNode  != null
                            ? TypeConversionUtil.convertToString(
                                    _tableNameExpressionNode .calculate(
                                            conditionalNG.getSymbolTable()), false)
                            : null;
                    break;

                case Table:
                    name = TypeConversionUtil.convertToString(
                            _tableNameSelectTable.evaluateTableData(conditionalNG), false);
                    break;

                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _tableNameAddressing.name());
            }

            NamedTable table = null;
            if (name != null) {
                table = InstanceManager.getDefault(NamedTableManager.class)
                        .getNamedBean(name);
            }
            return table;
        }
    }

    private String evaluateTableRow(ConditionalNG conditionalNG) throws JmriException {

        switch (_tableRowAddressing) {
            case Direct:
                return _tableRowName;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _tableRowReference);

            case Memory:
                return TypeConversionUtil
                        .convertToString(_tableRowMemoryHandle.getBean().getValue(), false);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_tableRowLocalVariable), false);

            case Formula:
                return _tableRowExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _tableRowExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : null;

                case Table:
                    return TypeConversionUtil.convertToString(
                            _tableRowSelectTable.evaluateTableData(conditionalNG), false);

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _tableRowAddressing.name());
        }
    }

    private String evaluateTableColumn(ConditionalNG conditionalNG) throws JmriException {

        switch (_tableColumnAddressing) {
            case Direct:
                return _tableColumnName;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _tableColumnReference);

            case Memory:
                return TypeConversionUtil
                        .convertToString(_tableColumnMemoryHandle.getBean().getValue(), false);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_tableColumnLocalVariable), false);

            case Formula:
                return _tableColumnExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _tableColumnExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : null;

                case Table:
                    return TypeConversionUtil.convertToString(
                            _tableColumnSelectTable.evaluateTableData(conditionalNG), false);

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _tableColumnAddressing.name());
        }
    }

    public Object evaluateTableData(ConditionalNG conditionalNG) throws JmriException {
        return evaluateTableBean(conditionalNG).getCell(
                evaluateTableRow(conditionalNG), evaluateTableColumn(conditionalNG));
    }

    public void evaluateAndSetTableData(ConditionalNG conditionalNG, Object value)
            throws JmriException {
        evaluateTableBean(conditionalNG).setCell(
                value,
                evaluateTableRow(conditionalNG),
                evaluateTableColumn(conditionalNG));
    }

    public String getTableNameDescription(Locale locale) {
        String namedBean;

        String memoryName;
        if (_tableNameMemoryHandle != null) {
            memoryName = _tableNameMemoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_tableNameAddressing) {
            case Direct:
                String tableName;
                if (_tableHandle != null) {
                    tableName = _tableHandle.getBean().getDisplayName();
                } else {
                    tableName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", tableName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _tableNameReference);
                break;

            case Memory:
                namedBean = Bundle.getMessage(locale, "AddressByMemory", memoryName);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _tableNameLocalVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _tableNameFormula);
                break;

            case Table:
                namedBean = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _tableNameSelectTable.getTableNameDescription(locale),
                        _tableNameSelectTable.getTableRowDescription(locale),
                        _tableNameSelectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _tableNameAddressing: " + _tableNameAddressing.name());
        }
        return namedBean;
    }

    public String getTableRowDescription(Locale locale) {
        String row;

        String memoryName;
        if (_tableRowMemoryHandle != null) {
            memoryName = _tableRowMemoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_tableRowAddressing) {
            case Direct:
                row = Bundle.getMessage(locale, "AddressByDirect", _tableRowName);
                break;

            case Reference:
                row = Bundle.getMessage(locale, "AddressByReference", _tableRowReference);
                break;

            case Memory:
                row = Bundle.getMessage(locale, "AddressByMemory", memoryName);
                break;

            case LocalVariable:
                row = Bundle.getMessage(locale, "AddressByLocalVariable", _tableRowLocalVariable);
                break;

            case Formula:
                row = Bundle.getMessage(locale, "AddressByFormula", _tableRowFormula);
                break;

            case Table:
                row = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _tableRowSelectTable.getTableNameDescription(locale),
                        _tableRowSelectTable.getTableRowDescription(locale),
                        _tableRowSelectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableRowAddressing.name());
        }
        return row;
    }

    public String getTableColumnDescription(Locale locale) {
        String column;

        String memoryName;
        if (_tableColumnMemoryHandle != null) {
            memoryName = _tableColumnMemoryHandle.getName();
        } else {
            memoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

        switch (_tableColumnAddressing) {
            case Direct:
                column = Bundle.getMessage(locale, "AddressByDirect", _tableColumnName);
                break;

            case Reference:
                column = Bundle.getMessage(locale, "AddressByReference", _tableColumnReference);
                break;

            case Memory:
                column = Bundle.getMessage(locale, "AddressByMemory", memoryName);
                break;

            case LocalVariable:
                column = Bundle.getMessage(locale, "AddressByLocalVariable", _tableColumnLocalVariable);
                break;

            case Formula:
                column = Bundle.getMessage(locale, "AddressByFormula", _tableColumnFormula);
                break;

            case Table:
                column = Bundle.getMessage(
                        locale,
                        "AddressByTable",
                        _tableColumnSelectTable.getTableNameDescription(locale),
                        _tableColumnSelectTable.getTableRowDescription(locale),
                        _tableColumnSelectTable.getTableColumnDescription(locale));
                break;

            default:
                throw new IllegalArgumentException("invalid _tableRowAddressing: " + _tableColumnAddressing.name());
        }
        return column;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LogixNG_SelectTable.class);
}
