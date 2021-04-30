package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers an OBlock.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionOBlock extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<OBlock> _oblockHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private DirectOperation _operationDirect = DirectOperation.None;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    private String _oblockConstant = "";
    private NamedBeanHandle<Memory> _oblockMemoryHandle;

    public ActionOBlock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionOBlock copy = new ActionOBlock(sysName, userName);
        copy.setComment(getComment());
        copy.setAddressing(_addressing);
        if (_oblockHandle != null) copy.setOBlock(_oblockHandle);
        copy.setReference(_reference);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationDirect(_operationDirect);
        copy.setOperationReference(_operationReference);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationFormula(_operationFormula);
        copy.setOBlockConstant(_oblockConstant);
        if (_oblockMemoryHandle != null) copy.setOBlockMemory(_oblockMemoryHandle);
        return manager.registerAction(copy);
    }

    public void setOBlock(@Nonnull String oblockName) {
        assertListenersAreNotRegistered(log, "setOBlock");
        OBlock oblock = InstanceManager.getDefault(OBlockManager.class).getNamedBean(oblockName);
        if (oblock != null) {
            ActionOBlock.this.setOBlock(oblock);
        } else {
            removeOBlock();
            log.error("OBlock \"{}\" is not found", oblockName);
        }
    }

    public void setOBlock(@Nonnull OBlock oblock) {
        assertListenersAreNotRegistered(log, "setOBlock");
        ActionOBlock.this.setOBlock(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(oblock.getDisplayName(), oblock));
    }

    public void setOBlock(@Nonnull NamedBeanHandle<OBlock> handle) {
        assertListenersAreNotRegistered(log, "setOBlock");
        _oblockHandle = handle;
        InstanceManager.getDefault(OBlockManager.class).addVetoableChangeListener(this);
    }

    public void removeOBlock() {
        assertListenersAreNotRegistered(log, "removeOBlock");
        if (_oblockHandle != null) {
            InstanceManager.getDefault(OBlockManager.class).removeVetoableChangeListener(this);
            _oblockHandle = null;
        }
    }

    public NamedBeanHandle<OBlock> getOBlock() {
        return _oblockHandle;
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

    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseLockFormula();
    }

    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }

    public void setOperationDirect(DirectOperation state) {
        _operationDirect = state;
    }

    public DirectOperation getOperationDirect() {
        return _operationDirect;
    }

    public void setOperationReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _operationReference = reference;
    }

    public String getOperationReference() {
        return _operationReference;
    }

    public void setOperationLocalVariable(@Nonnull String localVariable) {
        _operationLocalVariable = localVariable;
    }

    public String getOperationLocalVariable() {
        return _operationLocalVariable;
    }

    public void setOperationFormula(@Nonnull String formula) throws ParserException {
        _operationFormula = formula;
        parseLockFormula();
    }

    public String getLockFormula() {
        return _operationFormula;
    }

    private void parseLockFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
        }
    }

    public void setOBlockConstant(@Nonnull String constant) {
        _oblockConstant = constant;
    }

    public String getOBlockConstant() {
        return _oblockConstant;
    }

    public void setOBlockMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setOBlockMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setOBlockMemory(memory);
        } else {
            removeOBlockMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setOBlockMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setOBlockMemory");
        setOBlockMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void setOBlockMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setOBlockMemory");
        _oblockMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public NamedBeanHandle<Memory> getOBlockMemory() {
        return _oblockMemoryHandle;
    }

    public void removeOBlockMemory() {
        assertListenersAreNotRegistered(log, "removeOBlockMemory");
        if (_oblockMemoryHandle != null) {
            _oblockMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    private void addRemoveVetoListener() {
        if ((_oblockMemoryHandle != null)) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof OBlock) {
                if (evt.getOldValue().equals(getOBlock().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionOBlock_OBlockInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getOBlockMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionOBlock_MemoryInUseVeto", getDisplayName()), e); // NOI18N
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
    public boolean isExternal() {
        return true;
    }

    private String getNewLock() throws JmriException {

        switch (_operationAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _operationReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_operationLocalVariable), false);

            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        OBlock oblock;

        switch (_addressing) {
            case Direct:
                oblock = _oblockHandle != null ? _oblockHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                oblock = InstanceManager.getDefault(OBlockManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                oblock = InstanceManager.getDefault(OBlockManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                oblock = _expressionNode != null ?
                        InstanceManager.getDefault(OBlockManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (oblock == null) {
            return;
        }

        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewLock() : null;

        DirectOperation oper;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            oper = _operationDirect;
        } else {
            oper = DirectOperation.valueOf(name);
        }

        if (_operationDirect != DirectOperation.None) {

            // Variables used in lambda must be effectively final
            DirectOperation theOper = oper;

            ThreadingUtil.runOnLayoutWithJmriException(() -> {
                switch (theOper) {
                    case None:
                        break;
                    case Deallocate:
                        oblock.deAllocate(null);
                        break;
                    case SetValue:
                        oblock.setValue(_oblockConstant);
                        break;
                    case SetError:
                        oblock.setError(true);
                        break;
                    case ClearError:
                        oblock.setError(false);
                        break;
                    case SetOutOfService:
                        oblock.setOutOfService(true);
                        break;
                    case ClearOutOfService:
                        oblock.setOutOfService(false);
                        break;
                    case CopyFromMemory:
                        if (_oblockMemoryHandle != null) {
                            oblock.setValue(_oblockMemoryHandle.getBean().getValue());
                        }
                        break;
                    case CopyToMemory:
                        if (_oblockMemoryHandle != null) {
                            Memory memory = _oblockMemoryHandle.getBean();
                            memory.setValue(oblock.getValue());
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("invalid oper state: " + theOper.name());
                }
            });
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
        return Bundle.getMessage(locale, "ActionOBlock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state = "";

        switch (_addressing) {
            case Direct:
                String oblockName;
                if (_oblockHandle != null) {
                    oblockName = _oblockHandle.getBean().getDisplayName();
                } else {
                    oblockName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", oblockName);
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

        switch (_operationAddressing) {
            case Direct:
                if (_operationDirect == DirectOperation.SetValue) {
                    return Bundle.getMessage(locale, "ActionOBlock_Long_Value", namedBean, _oblockConstant);
                } else if (_operationDirect == DirectOperation.CopyFromMemory) {
                    String fromName = _oblockMemoryHandle == null ? "" : _oblockMemoryHandle.getName();
                    return Bundle.getMessage(locale, "ActionOBlock_Long_FromMemory", namedBean, fromName);
                } else if (_operationDirect == DirectOperation.CopyToMemory) {
                    String toName = _oblockMemoryHandle == null ? "" : _oblockMemoryHandle.getName();
                    return Bundle.getMessage(locale, "ActionOBlock_Long_ToMemory", toName, namedBean);
                } else {
                    state = Bundle.getMessage(locale, "AddressByDirect", _operationDirect._text);
                }
                break;

            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;

            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;

            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _operationAddressing.name());
        }

        return Bundle.getMessage(locale, "ActionOBlock_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    public enum DirectOperation {
        None(""),
        Deallocate(Bundle.getMessage("ActionOBlock_Deallocate")),
        SetValue(Bundle.getMessage("ActionOBlock_SetValue")),
        SetError(Bundle.getMessage("ActionOBlock_SetError")),
        ClearError(Bundle.getMessage("ActionOBlock_ClearError")),
        SetOutOfService(Bundle.getMessage("ActionOBlock_SetOutOfService")),
        ClearOutOfService(Bundle.getMessage("ActionOBlock_ClearOutOfService")),
        CopyFromMemory(Bundle.getMessage("ActionOBlock_CopyFromMemory")),
        CopyToMemory(Bundle.getMessage("ActionOBlock_CopyToMemory"));

        private final String _text;

        private DirectOperation(String text) {
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
        log.debug("getUsageReport :: ActionOBlock: bean = {}, report = {}", cdl, report);
        if (getOBlock() != null && bean.equals(getOBlock().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
        if (getOBlockMemory() != null && bean.equals(getOBlockMemory().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionOBlock.class);

}
