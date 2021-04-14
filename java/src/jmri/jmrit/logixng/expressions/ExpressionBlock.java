package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Block;
import jmri.BlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * This expression evaluates the state of a Block.
 * The supported characteristics are:
 * <ul>
 *   <li>Is [not] Occupied (based on occupancy sensor state)</li>
 *   <li>Is [not] Unoccupied (based on occupancy sensor state)</li>
 *   <li>Is [not] Other (UNKNOWN, INCONSISTENT, UNDETECTED)</li>
 *   <li>Is [not] Allocated (based on the LayoutBlock useAlternateColor)</li>
 *   <li>Value [not] equals string</li>
 *   <li>Value [not] equals a memory value object.</li>
 * </ul>
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ExpressionBlock extends AbstractDigitalExpression
        implements PropertyChangeListener, VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Block> _blockHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private BlockState _blockState = BlockState.Occupied;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    private String _blockConstant = "";
    private NamedBeanHandle<Memory> _blockMemoryHandle;

    private int _eventState = 0;
    private Object _eventValue = null;
    private boolean _eventAllocated = false;

    public ExpressionBlock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionBlock copy = new ExpressionBlock(sysName, userName);
        copy.setComment(getComment());
        copy.setAddressing(_addressing);
        if (_blockHandle != null) copy.setBlock(_blockHandle);
        copy.setReference(_reference);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);
        copy.set_Is_IsNot(_is_IsNot);
        copy.setStateAddressing(_stateAddressing);
        copy.setBeanState(_blockState);
        copy.setStateReference(_stateReference);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateFormula(_stateFormula);
        copy.setBlockConstant(_blockConstant);
        if (_blockMemoryHandle != null) copy.setBlockMemory(_blockMemoryHandle);
        return manager.registerExpression(copy);
    }

    public void setBlock(@Nonnull String blockName) {
        assertListenersAreNotRegistered(log, "setBlock");
        Block block =
                InstanceManager.getDefault(BlockManager.class).getNamedBean(blockName);
        if (block != null) {
            ExpressionBlock.this.setBlock(block);
        } else {
            removeBlock();
            log.warn("block \"{}\" is not found", blockName);
        }
    }

    public void setBlock(@Nonnull Block block) {
        assertListenersAreNotRegistered(log, "setBlock");
        ExpressionBlock.this.setBlock(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(block.getDisplayName(), block));
    }

    public void setBlock(@Nonnull NamedBeanHandle<Block> handle) {
        assertListenersAreNotRegistered(log, "setBlock");
        _blockHandle = handle;
        InstanceManager.getDefault(BlockManager.class).addVetoableChangeListener(this);
    }

    public void removeBlock() {
        assertListenersAreNotRegistered(log, "removeBlock");
        if (_blockHandle != null) {
            InstanceManager.getDefault(BlockManager.class).removeVetoableChangeListener(this);
            _blockHandle = null;
        }
    }

    public NamedBeanHandle<Block> getBlock() {
        return _blockHandle;
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

    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }

    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }

    public void setStateAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseStateFormula();
    }

    public NamedBeanAddressing getStateAddressing() {
        return _stateAddressing;
    }

    public void setBeanState(BlockState state) {
        _blockState = state;
    }

    public BlockState getBeanState() {
        return _blockState;
    }

    public void setStateReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _stateReference = reference;
    }

    public String getStateReference() {
        return _stateReference;
    }

    public void setStateLocalVariable(@Nonnull String localVariable) {
        _stateLocalVariable = localVariable;
    }

    public String getStateLocalVariable() {
        return _stateLocalVariable;
    }

    public void setStateFormula(@Nonnull String formula) throws ParserException {
        _stateFormula = formula;
        parseStateFormula();
    }

    public String getStateFormula() {
        return _stateFormula;
    }

    private void parseStateFormula() throws ParserException {
        if (_stateAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _stateExpressionNode = parser.parseExpression(_stateFormula);
        } else {
            _stateExpressionNode = null;
        }
    }

    public void setBlockConstant(@Nonnull String constant) {
        _blockConstant = constant;
    }

    public String getBlockConstant() {
        return _blockConstant;
    }

    public void setBlockMemory(@Nonnull String memoryName) {
        assertListenersAreNotRegistered(log, "setBlockMemory");
        MemoryManager memoryManager = InstanceManager.getDefault(MemoryManager.class);
        Memory memory = memoryManager.getMemory(memoryName);
        if (memory != null) {
            setBlockMemory(memory);
        } else {
            removeBlockMemory();
            log.warn("memory \"{}\" is not found", memoryName);
        }
    }

    public void setBlockMemory(@Nonnull Memory memory) {
        assertListenersAreNotRegistered(log, "setBlockMemory");
        setBlockMemory(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(memory.getDisplayName(), memory));
    }

    public void setBlockMemory(@Nonnull NamedBeanHandle<Memory> handle) {
        assertListenersAreNotRegistered(log, "setBlockMemory");
        _blockMemoryHandle = handle;
        addRemoveVetoListener();
    }

    public NamedBeanHandle<Memory> getBlockMemory() {
        return _blockMemoryHandle;
    }

    public void removeBlockMemory() {
        assertListenersAreNotRegistered(log, "removeBlockMemory");
        if (_blockMemoryHandle != null) {
            _blockMemoryHandle = null;
            addRemoveVetoListener();
        }
    }

    private void addRemoveVetoListener() {
        if ((_blockMemoryHandle != null)) {
            InstanceManager.getDefault(MemoryManager.class).addVetoableChangeListener(this);
        } else {
            InstanceManager.getDefault(MemoryManager.class).removeVetoableChangeListener(this);
        }
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // NOI18N
            if (evt.getOldValue() instanceof Block) {
                if (evt.getOldValue().equals(getBlock().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Block_BlockInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
            if (evt.getOldValue() instanceof Memory) {
                if (evt.getOldValue().equals(getBlockMemory().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("Block_MemoryInUseVeto", getDisplayName()), e); // NOI18N
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

    private String getNewState() throws JmriException {

        switch (_stateAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference);

            case LocalVariable:
                SymbolTable symbolTable =
                        getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_stateLocalVariable), false);

            case Formula:
                return _stateExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _stateExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _stateAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        Block block;

        switch (_addressing) {
            case Direct:
                block = _blockHandle != null ? _blockHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                block = InstanceManager.getDefault(BlockManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable =
                        getConditionalNG().getSymbolTable();
                block = InstanceManager.getDefault(BlockManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                block = _expressionNode != null ?
                        InstanceManager.getDefault(BlockManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (block == null) {
            return false;
        }

        BlockState checkBlockState;

        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            checkBlockState = _blockState;
        } else {
            checkBlockState = BlockState.valueOf(getNewState());
        }

        int currentState = _eventState;
        switch (checkBlockState) {
            case Other:
                if (currentState != Block.OCCUPIED && currentState != Block.UNOCCUPIED) {
                    currentState = BlockState.Other.getID();
                } else {
                    currentState = 0;
                }
                break;

            case Allocated:
                currentState = _eventAllocated ? BlockState.Allocated.getID() : 0;
                break;

            case ValueMatches:
                currentState = _blockConstant.equals(_eventValue) ? BlockState.ValueMatches.getID() : 0;
                break;

            case MemoryMatches:
                currentState = 0;
                if (_blockMemoryHandle != null) {
                    Object memoryObject = _blockMemoryHandle.getBean().getValue();
                    if (memoryObject != null && memoryObject.equals(_eventValue)) {
                        currentState = BlockState.MemoryMatches.getID();
                    }
                }
                break;

            default:
                break;
        }

        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentState == checkBlockState.getID();
        } else {
            return currentState != checkBlockState.getID();
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
        return Bundle.getMessage(locale, "Block_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state;

        switch (_addressing) {
            case Direct:
                String blockName;
                if (_blockHandle != null) {
                    blockName = _blockHandle.getBean().getDisplayName();
                } else {
                    blockName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", blockName);
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

        switch (_stateAddressing) {
            case Direct:

                if (_blockState == BlockState.ValueMatches) {
                    String equalsString = _is_IsNot == Is_IsNot_Enum.Is ? Bundle.getMessage("Block_Equal") : Bundle.getMessage("Block_NotEqual");
                    return Bundle.getMessage(locale, "Block_Long_Value", _blockHandle.getName(), equalsString, _blockConstant);

                } else if (_blockState == BlockState.MemoryMatches) {
                    String memoryName = _blockMemoryHandle == null ? "" : _blockMemoryHandle.getName();
                    String equalsMemory = _is_IsNot == Is_IsNot_Enum.Is ? Bundle.getMessage("Block_Equal") : Bundle.getMessage("Block_NotEqual");
                    return Bundle.getMessage(locale, "Block_Long_Memory", namedBean, equalsMemory, memoryName);

                } else if (_blockState == BlockState.Other) {
                    state = Bundle.getMessage(locale, "AddressByDirect", _blockState._text);
                    return Bundle.getMessage(locale, "Block_Long", namedBean, "", state);

                } else {
                    state = Bundle.getMessage(locale, "AddressByDirect", _blockState._text);
                }

               break;

            case Reference:
                state = Bundle.getMessage(locale, "AddressByReference", _stateReference);
                break;

            case LocalVariable:
                state = Bundle.getMessage(locale, "AddressByLocalVariable", _stateLocalVariable);
                break;

            case Formula:
                state = Bundle.getMessage(locale, "AddressByFormula", _stateFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _stateAddressing.name());
        }


        return Bundle.getMessage(locale, "Block_Long", namedBean, _is_IsNot.toString(), state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered && (_blockHandle != null)) {
            _blockHandle.getBean().addPropertyChangeListener(this);
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _blockHandle.getBean().removePropertyChangeListener(this);
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "state":
                _eventState = (int) evt.getNewValue();
                break;
            case "value":
                _eventValue = evt.getNewValue();
                break;
            case "allocated":
                _eventAllocated = (boolean) evt.getNewValue();
                break;
            default:
                return;
        }
        if (getTriggerOnChange()) {
            getConditionalNG().execute();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    public enum BlockState {
        Occupied(2, Bundle.getMessage("Block_StateOccupied")),
        NotOccupied(4, Bundle.getMessage("Block_StateNotOccupied")),
        Other(-1, Bundle.getMessage("Block_StateOther")),
        Allocated(-2, Bundle.getMessage("Block_Allocated")),
        ValueMatches(-3, Bundle.getMessage("Block_ValueMatches")),
        MemoryMatches(-4, Bundle.getMessage("Block_MemoryMatches"));

        private final int _id;
        private final String _text;

        private BlockState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        public int getID() {
            return _id;
        }

        @Override
        public String toString() {
            return _text;
        }
    }

    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionBlock: bean = {}, report = {}", cdl, report);
        if (getBlock() != null && bean.equals(getBlock().getBean())) {
            report.add(new NamedBeanUsageReport("ConditionalNGExpression", cdl, getLongDescription()));
        }
        if (getBlockMemory() != null && bean.equals(getBlockMemory().getBean())) {
            report.add(new NamedBeanUsageReport("ConditionalNGExpression", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlock.class);

}
