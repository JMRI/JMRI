package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.Block;
import jmri.BlockManager;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.LogixNG_SelectString;
import jmri.jmrit.logixng.util.parser.*;

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

    private final LogixNG_SelectEnum<BlockState> _selectEnum =
            new LogixNG_SelectEnum<>(this, BlockState.values(), BlockState.Occupied, this);

    private final LogixNG_SelectString _selectBlockValue =
            new LogixNG_SelectString(this, this);


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
        _selectEnum.copy(copy._selectEnum);
        _selectBlockValue.copy(copy._selectBlockValue);

        copy.set_Is_IsNot(_is_IsNot);

        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<Block> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<BlockState> getSelectEnum() {
        return _selectEnum;
    }

    public LogixNG_SelectString getSelectBlockValue() {
        return _selectBlockValue;
    }

    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }

    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
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
        ConditionalNG conditionalNG = getConditionalNG();

        Block block = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (block == null) return false;

        BlockState checkBlockState = _selectEnum.evaluateEnum(conditionalNG);

        int currentState = block.getState();

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
                String blockValue = _selectBlockValue.evaluateValue(conditionalNG);
                currentState = blockValue.equals(block.getValue())
                        ? BlockState.ValueMatches.getID() : 0;
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

        if (_selectEnum.isDirectAddressing()) {
            BlockState blockState = _selectEnum.getEnum();

            if (blockState == BlockState.ValueMatches) {
                String bundleKey = "Block_Long_Value";
                String equalsString = _is_IsNot == Is_IsNot_Enum.Is ? Bundle.getMessage("Block_Equal") : Bundle.getMessage("Block_NotEqual");
                return Bundle.getMessage(locale, bundleKey, namedBean, equalsString, _selectBlockValue.getDescription(locale));
            } else if (blockState == BlockState.Other) {
                state = Bundle.getMessage(locale, "AddressByDirect", blockState._text);
                return Bundle.getMessage(locale, "Block_Long", namedBean, "", state);
            } else {
                state = Bundle.getMessage(locale, "AddressByDirect", blockState._text);
            }
        } else {
            state = _selectEnum.getDescription(locale);
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
            _selectEnum.registerListeners();
            _selectBlockValue.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener(this);
            _selectNamedBean.unregisterListeners();
            _selectEnum.unregisterListeners();
            _selectBlockValue.unregisterListeners();
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
