package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.util.ThreadingUtil;

/**
 * This action sets the value of a memory.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionMemory extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Memory> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);
    private final LogixNG_SelectNamedBean<Memory> _selectOtherMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);
//    private NamedBeanHandle<Memory> _otherMemoryHandle;
    private MemoryOperation _memoryOperation = MemoryOperation.SetToString;
    private String _otherConstantValue = "";
    private String _otherLocalVariable = "";
    private String _otherFormula = "";
    private ExpressionNode _otherExpressionNode;
    private boolean _listenToMemory = true;

    private final LogixNG_SelectTable _selectTable =
            new LogixNG_SelectTable(this, () -> {return _memoryOperation == MemoryOperation.CopyTableCellToMemory;});


    public ActionMemory(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionMemory copy = new ActionMemory(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectOtherMemoryNamedBean.copy(copy._selectOtherMemoryNamedBean);
        copy.setMemoryOperation(_memoryOperation);
        copy.setOtherConstantValue(_otherConstantValue);
//        copy.setOtherTableCell(_otherTableCell);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setOtherFormula(_otherFormula);
        copy.setListenToMemory(_listenToMemory);
        _selectTable.copy(copy._selectTable);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Memory> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectOtherMemoryNamedBean() {
        return _selectOtherMemoryNamedBean;
    }

    public void setMemoryOperation(MemoryOperation state) throws ParserException {
        _memoryOperation = state;
        parseOtherFormula();
    }

    public MemoryOperation getMemoryOperation() {
        return _memoryOperation;
    }

    // Constant tab
    public void setOtherConstantValue(String constantValue) {
        _otherConstantValue = constantValue;
    }

    public String getConstantValue() {
        return _otherConstantValue;
    }

    public LogixNG_SelectTable getSelectTable() {
        return _selectTable;
    }

    public void setListenToMemory(boolean listenToMemory) {
        this._listenToMemory = listenToMemory;
    }

    public boolean getListenToMemory() {
        return _listenToMemory;
    }

    // Variable tab
    public void setOtherLocalVariable(@Nonnull String localVariable) {
        assertListenersAreNotRegistered(log, "setOtherLocalVariable");
        _otherLocalVariable = localVariable;
    }

    public String getOtherLocalVariable() {
        return _otherLocalVariable;
    }

    // Formula tab
    public void setOtherFormula(String formula) throws ParserException {
        _otherFormula = formula;
        parseOtherFormula();
    }

    public String getOtherFormula() {
        return _otherFormula;
    }

    private void parseOtherFormula() throws ParserException {
        if (_memoryOperation == MemoryOperation.CalculateFormula) {
            Map<String, Variable> variables = new HashMap<>();
            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _otherExpressionNode = parser.parseExpression(_otherFormula);
        } else {
            _otherExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        final ConditionalNG conditionalNG = getConditionalNG();

        Memory memory = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (memory == null) {
//            log.warn("memory is null");
            return;
        }

        AtomicReference<JmriException> ref = new AtomicReference<>();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            switch (_memoryOperation) {
                case SetToNull:
                    memory.setValue(null);
                    break;

                case SetToString:
                    memory.setValue(_otherConstantValue);
                    break;

                case CopyTableCellToMemory:
                    Object value = _selectTable.evaluateTableData(conditionalNG);
                    memory.setValue(value);
                    break;

                case CopyVariableToMemory:
                    Object variableValue = conditionalNG
                                    .getSymbolTable().getValue(_otherLocalVariable);
                    memory.setValue(variableValue);
                    break;

                case CopyMemoryToMemory:
                    Memory otherMemory = _selectOtherMemoryNamedBean.evaluateNamedBean(conditionalNG);
                    if (otherMemory != null) {
                        memory.setValue(otherMemory.getValue());
                    } else {
                        log.warn("setMemory should copy memory to memory but other memory is null");
                    }
                    break;

                case CalculateFormula:
                    if (_otherFormula.isEmpty()) {
                        memory.setValue(null);
                    } else {
                        try {
                            if (_otherExpressionNode == null) {
                                return;
                            }
                            memory.setValue(_otherExpressionNode.calculate(
                                    conditionalNG.getSymbolTable()));
                        } catch (JmriException e) {
                            ref.set(e);
                        }
                    }
                    break;

                default:
                    throw new IllegalArgumentException("_memoryOperation has invalid value: {}" + _memoryOperation.name());
            }
        });

        if (ref.get() != null) throw ref.get();
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
        return Bundle.getMessage(locale, "ActionMemory_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);

        String copyToMemoryName = _selectOtherMemoryNamedBean.getDescription(locale);

        switch (_memoryOperation) {
            case SetToNull:
                return Bundle.getMessage(locale, "ActionMemory_Long_Null", namedBean);
            case SetToString:
                return Bundle.getMessage(locale, "ActionMemory_Long_Value", namedBean, _otherConstantValue);
            case CopyVariableToMemory:
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyVariableToMemory", namedBean, _otherLocalVariable);
            case CopyMemoryToMemory:
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyMemoryToMemory", namedBean, copyToMemoryName);
            case CopyTableCellToMemory:
                String tableName = _selectTable.getTableNameDescription(locale);
                String rowName = _selectTable.getTableRowDescription(locale);
                String columnName = _selectTable.getTableColumnDescription(locale);
                return Bundle.getMessage(locale, "ActionMemory_Long_CopyTableCellToMemory", namedBean, tableName, rowName, columnName);
            case CalculateFormula:
                return Bundle.getMessage(locale, "ActionMemory_Long_Formula", namedBean, _otherFormula);
            default:
                throw new IllegalArgumentException("_memoryOperation has invalid value: " + _memoryOperation.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            if (_listenToMemory) {
                _selectOtherMemoryNamedBean.addPropertyChangeListener("value", this);
            }
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered && _listenToMemory) {
            _selectOtherMemoryNamedBean.removePropertyChangeListener("value", this);
        }
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


    public enum MemoryOperation {
        SetToNull(Bundle.getMessage("ActionMemory_MemoryOperation_SetToNull")),
        SetToString(Bundle.getMessage("ActionMemory_MemoryOperation_SetToString")),
        CopyVariableToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyVariableToMemory")),
        CopyMemoryToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyMemoryToMemory")),
        CopyTableCellToMemory(Bundle.getMessage("ActionMemory_MemoryOperation_CopyTableCellToMemory")),
        CalculateFormula(Bundle.getMessage("ActionMemory_MemoryOperation_CalculateFormula"));

        private final String _text;

        private MemoryOperation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectOtherMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemory.class);

}
