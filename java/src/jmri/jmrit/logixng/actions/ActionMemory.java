package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.LogixNG_SelectTable;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the value of a memory.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionMemory extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Memory> _memoryHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private NamedBeanHandle<Memory> _otherMemoryHandle;
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
        if (_memoryHandle != null) copy.setMemory(_memoryHandle);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);
        if (_otherMemoryHandle != null) copy.setOtherMemory(_otherMemoryHandle);
        copy.setMemoryOperation(_memoryOperation);
        copy.setOtherConstantValue(_otherConstantValue);
//        copy.setOtherTableCell(_otherTableCell);
        copy.setOtherLocalVariable(_otherLocalVariable);
        copy.setOtherFormula(_otherFormula);
        copy.setListenToMemory(_listenToMemory);
        _selectTable.copy(copy._selectTable);
        return manager.registerAction(copy);
    }

    public void setMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setMemory");
        Memory memory = InstanceManager.getDefault(MemoryManager.class).getMemory(memoryName);
        if (memory != null) {
            setMemory(memory);
        } else {
            removeMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setMemory");
        _memoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setMemory");
        setMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeMemory() {
        assertListenersAreNotRegistered(log, "removeMemory");
        if (_memoryHandle != null) {
            _memoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getMemory() {
        return _memoryHandle;
    }


    public void setAddressing(NamedBeanAddressing addressing) throws ParserException {
        _addressing = addressing;
        parseFormula();
    }

    public NamedBeanAddressing getAddressing() {
        return _addressing;
    }

    public void setReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _reference = reference;
    }

    public String getReference() {
        return _reference;
    }

    public void setLocalVariable(@Nonnull String localVariable) {
        _localVariable = localVariable;
    }

    public String getLocalVariable() {
        return _localVariable;
    }

    public void setFormula(@Nonnull String formula) throws ParserException {
        _formula = formula;
        parseFormula();
    }

    public String getFormula() {
        return _formula;
    }

    private void parseFormula() throws ParserException {
        if (_addressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _expressionNode = parser.parseExpression(_formula);
        } else {
            _expressionNode = null;
        }
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

    // Memory tab
    public void setOtherMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setOtherMemory(memory);
        } else {
            removeOtherMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setOtherMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        _otherMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public void setOtherMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setOtherMemory");
        setOtherMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void removeOtherMemory() {
        assertListenersAreNotRegistered(log, "removeOtherMemory");
        if (_otherMemoryHandle != null) {
            _otherMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    public NamedBeanHandle<Memory> getOtherMemory() {
        return _otherMemoryHandle;
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

    private void addRemoveVetoListener() {
        if ((_memoryHandle != null) || (_otherMemoryHandle != null)) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Memory) {
                boolean doVeto = false;
                if ((_memoryHandle != null) && evt.getOldValue().equals(_memoryHandle.getBean())) {
                    doVeto = true;
                }
                if ((_memoryOperation == MemoryOperation.CopyMemoryToMemory)
                        && (_otherMemoryHandle != null)
                        && evt.getOldValue().equals(_otherMemoryHandle.getBean())) {
                    doVeto = true;
                }
                if (doVeto) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionMemory_MemoryInUseMemoryActionVeto", getDisplayName()), e); // NOI18N
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {

        Memory memory;

//        System.out.format("ActionLight.execute: %s%n", getLongDescription());

        switch (_addressing) {
            case Direct:
                memory = _memoryHandle != null ? _memoryHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                memory = InstanceManager.getDefault(MemoryManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                memory = InstanceManager.getDefault(MemoryManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                memory = _expressionNode != null ?
                        InstanceManager.getDefault(MemoryManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

//        System.out.format("ActionMemory.execute: Memory: %s%n", memory);

        if (memory == null) {
//            log.warn("memory is null");
            return;
        }

        AtomicReference<JmriException> ref = new AtomicReference<>();

        final ConditionalNG conditionalNG = getConditionalNG();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {

            switch (_memoryOperation) {
                case SetToNull:
                    memory.setValue(null);
                    break;

                case SetToString:
                    memory.setValue(_otherConstantValue);
                    break;

                case CopyTableCellToMemory:
                    Object value = _selectTable.evaluateTableData(getConditionalNG());
                    memory.setValue(value);
                    break;

                case CopyVariableToMemory:
                    Object variableValue = conditionalNG
                                    .getSymbolTable().getValue(_otherLocalVariable);
                    memory.setValue(variableValue);
                    break;

                case CopyMemoryToMemory:
                    if (_otherMemoryHandle != null) {
                        memory.setValue(_otherMemoryHandle.getBean().getValue());
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
        String namedBean;

        switch (_addressing) {
            case Direct:
                String memoryName;
                if (_memoryHandle != null) {
                    memoryName = _memoryHandle.getBean().getDisplayName();
                } else {
                    memoryName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", memoryName);
                break;

            case Reference:
                namedBean = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                namedBean = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                namedBean = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        String copyToMemoryName;
        if (_otherMemoryHandle != null) {
            copyToMemoryName = _otherMemoryHandle.getBean().getDisplayName();
        } else {
            copyToMemoryName = Bundle.getMessage(locale, "BeanNotSelected");
        }

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
        if (!_listenersAreRegistered && (_otherMemoryHandle != null)) {
            if (_listenToMemory) {
                _otherMemoryHandle.getBean().addPropertyChangeListener("value", this);
            }
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            if (_listenToMemory && (_otherMemoryHandle != null)) {
                _otherMemoryHandle.getBean().removePropertyChangeListener("value", this);
            }
            _listenersAreRegistered = false;
        }
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
        log.debug("getUsageReport :: ActionMemory: bean = {}, report = {}", cdl, report);
        if (getMemory() != null && bean.equals(getMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
        if (getOtherMemory() != null && bean.equals(getOtherMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionMemory.class);

}
