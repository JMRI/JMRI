package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Block;
import jmri.BlockManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
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
 * </ul>
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ExpressionBlock extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Block> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Block.class, InstanceManager.getDefault(BlockManager.class), this);

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;

    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private BlockState _blockState = BlockState.Occupied;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private String _blockValue = "";

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

        _selectNamedBean.copy(copy._selectNamedBean);

        copy.set_Is_IsNot(_is_IsNot);

        copy.setStateAddressing(_stateAddressing);
        copy.setBeanState(_blockState);
        copy.setStateReference(_stateReference);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateFormula(_stateFormula);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);
        copy.setBlockValue(_blockValue);
        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<Block> getSelectNamedBean() {
        return _selectNamedBean;
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


    public void setDataAddressing(NamedBeanAddressing addressing) throws ParserException {
        _dataAddressing = addressing;
        parseDataFormula();
    }

    public NamedBeanAddressing getDataAddressing() {
        return _dataAddressing;
    }

    public void setDataReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _dataReference = reference;
    }

    public String getDataReference() {
        return _dataReference;
    }

    public void setDataLocalVariable(@Nonnull String localVariable) {
        _dataLocalVariable = localVariable;
    }

    public String getDataLocalVariable() {
        return _dataLocalVariable;
    }

    public void setDataFormula(@Nonnull String formula) throws ParserException {
        _dataFormula = formula;
        parseDataFormula();
    }

    public String getDataFormula() {
        return _dataFormula;
    }

    private void parseDataFormula() throws ParserException {
        if (_dataAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _dataExpressionNode = parser.parseExpression(_dataFormula);
        } else {
            _dataExpressionNode = null;
        }
    }


    public void setBlockValue(@Nonnull String value) {
        _blockValue = value;
    }

    public String getBlockValue() {
        return _blockValue;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
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

    private String getNewData() throws JmriException {

        switch (_dataAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);

            case LocalVariable:
                SymbolTable symbolTable =
                        getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);

            case Formula:
                return _dataExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _dataAddressing.name());
        }
    }

    /**
     * A block is considered to be allocated if the related layout block has use extra color enabled.
     * @param block The block whose allocation state is requested.
     * @return true if the layout block is using the extra color.
     */
    public boolean isBlockAllocated(Block block) {
        boolean result = false;
        LayoutBlock layoutBlock = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(block);
        if (layoutBlock != null) {
            result = layoutBlock.getUseExtraColor();
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        Block block = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (block == null) return false;

        BlockState checkBlockState;

        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            checkBlockState = _blockState;
        } else {
            checkBlockState = BlockState.valueOf(getNewState());
        }

        int currentState = block.getState();
        Object currentValue = null;

        switch (checkBlockState) {
            case Other:
                if (currentState != Block.OCCUPIED && currentState != Block.UNOCCUPIED) {
                    currentState = BlockState.Other.getID();
                } else {
                    currentState = 0;
                }
                break;

            case Allocated:
                boolean cuurrentAllocation = isBlockAllocated(block);
                currentState = cuurrentAllocation ? BlockState.Allocated.getID() : 0;
                break;

            case ValueMatches:
                currentValue = block.getValue();
                if (_dataAddressing == NamedBeanAddressing.Direct) {
                    currentState = _blockValue.equals(currentValue) ? BlockState.ValueMatches.getID() : 0;
                } else {
                    currentState = getNewData().equals(currentValue) ? BlockState.ValueMatches.getID() : 0;
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
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

        switch (_stateAddressing) {
            case Direct:
                if (_blockState == BlockState.ValueMatches) {
                    String bundleKey = "Block_Long_Value";
                    String equalsString = _is_IsNot == Is_IsNot_Enum.Is ? Bundle.getMessage("Block_Equal") : Bundle.getMessage("Block_NotEqual");
                    switch (_dataAddressing) {
                        case Direct:
                            return Bundle.getMessage(locale, bundleKey, namedBean, equalsString, _blockValue);
                        case Reference:
                            return Bundle.getMessage(locale, bundleKey, namedBean, equalsString, Bundle.getMessage("AddressByReference", _dataReference));
                        case LocalVariable:
                            return Bundle.getMessage(locale, bundleKey, namedBean, equalsString, Bundle.getMessage("AddressByLocalVariable", _dataLocalVariable));
                        case Formula:
                            return Bundle.getMessage(locale, bundleKey, namedBean, equalsString, Bundle.getMessage("AddressByFormula", _dataFormula));
                        default:
                            throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
                    }
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
        if (!_listenersAreRegistered) {
            _selectNamedBean.addPropertyChangeListener(this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener(this);
            _selectNamedBean.unregisterListeners();
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

    public enum BlockState {
        Occupied(2, Bundle.getMessage("Block_StateOccupied")),
        NotOccupied(4, Bundle.getMessage("Block_StateNotOccupied")),
        Other(-1, Bundle.getMessage("Block_StateOther")),
        Allocated(-2, Bundle.getMessage("Block_Allocated")),
        ValueMatches(-3, Bundle.getMessage("Block_ValueMatches"));

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

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionBlock: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionBlock.class);

}
