package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.Block;
import jmri.Sensor;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
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
public class ActionBlock extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Block> _blockHandle;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private DirectOperation _operationDirect = DirectOperation.SetOccupied;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

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
        copy.setAddressing(_addressing);
        if (_blockHandle != null) copy.setBlock(_blockHandle);
        copy.setReference(_reference);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);

        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationDirect(_operationDirect);
        copy.setOperationReference(_operationReference);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationFormula(_operationFormula);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);
        copy.setBlockValue(_blockValue);

        return manager.registerAction(copy);
    }

    public void setBlock(@Nonnull String blockName) {
        assertListenersAreNotRegistered(log, "setBlock");
        Block block = InstanceManager.getDefault(jmri.BlockManager.class).getNamedBean(blockName);
        if (block != null) {
            ActionBlock.this.setBlock(block);
        } else {
            removeBlock();
            log.error("Block \"{}\" is not found", blockName);
        }
    }

    public void setBlock(@Nonnull Block block) {
        assertListenersAreNotRegistered(log, "setBlock");
        ActionBlock.this.setBlock(InstanceManager.getDefault(NamedBeanHandleManager.class)
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


    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseOperationFormula();
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
        parseOperationFormula();
    }

     public String getOperationFormula() {
        return _operationFormula;
    }

    private void parseOperationFormula() throws ParserException {
        if (_operationAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _operationExpressionNode = parser.parseExpression(_operationFormula);
        } else {
            _operationExpressionNode = null;
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


    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Block) {
                if (evt.getOldValue().equals(getBlock().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionBlock_BlockInUseVeto", getDisplayName()), e); // NOI18N
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getNewOperation() throws JmriException {

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
                throw new IllegalArgumentException("invalid _addressing state: " + _dataAddressing.name());
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Block block;

        switch (_addressing) {
            case Direct:
                block = _blockHandle != null ? _blockHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                block = InstanceManager.getDefault(jmri.BlockManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
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
            return;
        }

        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewOperation() : null;

        DirectOperation oper;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            oper = _operationDirect;
        } else {
            oper = DirectOperation.valueOf(name);
        }


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
        String namedBean;
        String state = "";

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

        switch (_operationAddressing) {
            case Direct:
                if (_operationDirect == DirectOperation.SetValue) {
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
        log.debug("getUsageReport :: ActionBlock: bean = {}, report = {}", cdl, report);
        if (getBlock() != null && bean.equals(getBlock().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlock.class);

}
