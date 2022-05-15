package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Block;
import jmri.Sensor;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a block.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionBlock extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Block> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Block.class, InstanceManager.getDefault(BlockManager.class), this);

    private final LogixNG_SelectEnum<DirectOperation> _selectEnum =
            new LogixNG_SelectEnum<>(this, DirectOperation.values(), DirectOperation.SetOccupied, this);

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private String _blockValue = "";

    public ActionBlock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionBlock copy = new ActionBlock(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);
        copy.setBlockValue(_blockValue);

        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Block> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<DirectOperation> getSelectEnum() {
        return _selectEnum;
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

    private Object getNewData() throws JmriException {

        switch (_dataAddressing) {
            case Direct:
                return _blockValue;

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return symbolTable.getValue(_dataLocalVariable);

            case Formula:
                return _dataExpressionNode != null
                        ? _dataExpressionNode.calculate(
                                getConditionalNG().getSymbolTable())
                        : null;

            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Block block = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (block == null) {
            return;
        }

        DirectOperation oper = _selectEnum.evaluateEnum(getConditionalNG());

        // Variables used in lambda must be effectively final
        DirectOperation theOper = oper;

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            Sensor sensor;
            LayoutBlock lblk;

            switch (theOper) {
                case SetOccupied:
                    sensor = block.getSensor();
                    if (sensor != null) {
                        try {
                            sensor.setKnownState(Sensor.ACTIVE);
                        } catch (JmriException ex) {
                            log.debug("Exception setting sensor active");
                        }
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorSensor", block.getDisplayName()));
                    }
                    break;
                case SetNotOccupied:
                    sensor = block.getSensor();
                    if (sensor != null) {
                        try {
                            sensor.setKnownState(Sensor.INACTIVE);
                        } catch (JmriException ex) {
                            log.debug("Exception setting sensor inactive");
                        }
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorSensor", block.getDisplayName()));
                    }
                    break;
                case SetAltColorOn:
                    lblk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(block);
                    if (lblk != null) {
                        lblk.setUseExtraColor(true);
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorLayoutBlock", block.getDisplayName()));
                    }
                    break;
                case SetAltColorOff:
                    lblk = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(block);
                    if (lblk != null) {
                        lblk.setUseExtraColor(false);
                    } else {
                        throw new JmriException(Bundle.getMessage("ActionBlock_ErrorLayoutBlock", block.getDisplayName()));
                    }
                    break;
                case SetNullValue:
                    block.setValue(null);
                    break;
                case SetValue:
                    block.setValue(getNewData());
                    break;
                default:
                    throw new IllegalArgumentException("invalid oper state: " + theOper.name());
            }
        });
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
        return Bundle.getMessage(locale, "ActionBlock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        if (_selectEnum.getAddressing() == NamedBeanAddressing.Direct) {
            if (_selectEnum.getEnum() == DirectOperation.SetValue) {
                String bundleKey = "ActionBlock_Long_Value";
                switch (_dataAddressing) {
                    case Direct:
                        return Bundle.getMessage(locale, bundleKey, namedBean, _blockValue);
                    case Reference:
                        return Bundle.getMessage(locale, bundleKey, namedBean, Bundle.getMessage("AddressByReference", _dataReference));
                    case LocalVariable:
                        return Bundle.getMessage(locale, bundleKey, namedBean, Bundle.getMessage("AddressByLocalVariable", _dataLocalVariable));
                    case Formula:
                        return Bundle.getMessage(locale, bundleKey, namedBean, Bundle.getMessage("AddressByFormula", _dataFormula));
                    default:
                        throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
                }
            }
        }

        return Bundle.getMessage(locale, "ActionBlock_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectNamedBean.registerListeners();
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _selectEnum.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    public enum DirectOperation {
        SetOccupied(Bundle.getMessage("ActionBlock_SetOccupied")),
        SetNotOccupied(Bundle.getMessage("ActionBlock_SetNotOccupied")),
        SetAltColorOn(Bundle.getMessage("ActionBlock_SetAltColorOn")),
        SetAltColorOff(Bundle.getMessage("ActionBlock_SetAltColorOff")),
        SetNullValue(Bundle.getMessage("ActionBlock_SetNullValue")),
        SetValue(Bundle.getMessage("ActionBlock_SetValue"));

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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlock.class);

}
