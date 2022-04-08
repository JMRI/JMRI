package jmri.jmrit.logixng.actions;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
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
public class ActionOBlock extends AbstractDigitalAction {

    private final LogixNG_SelectNamedBean<OBlock> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, OBlock.class, InstanceManager.getDefault(OBlockManager.class));

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private DirectOperation _operationDirect = DirectOperation.Deallocate;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private String _oblockValue = "";

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
        _selectNamedBean.copy(copy._selectNamedBean);

        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationDirect(_operationDirect);
        copy.setOperationReference(_operationReference);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationFormula(_operationFormula);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);
        copy.setOBlockValue(_oblockValue);

        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<OBlock> getSelectNamedBean() {
        return _selectNamedBean;
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

    public void setOBlockValue(@Nonnull String value) {
        _oblockValue = value;
    }

    public String getOBlockValue() {
        return _oblockValue;
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

    private String getNewData(ConditionalNG conditionalNG) throws JmriException {

        switch (_dataAddressing) {
            case Direct:
                return _oblockValue;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _dataReference);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);

            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _dataAddressing.name());
        }
    }


    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        OBlock oblock = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (oblock == null) {
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

        final ConditionalNG conditionalNG = getConditionalNG();

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch (theOper) {
                case Deallocate:
                    oblock.deAllocate(null);
                    break;
                case SetValue:
                    oblock.setValue(getNewData(conditionalNG));
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
        return Bundle.getMessage(locale, "ActionOBlock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = "";

        switch (_operationAddressing) {
            case Direct:
                if (_operationDirect == DirectOperation.SetValue) {
                    String bundleKey = "ActionOBlock_Long_Value";
                    switch (_dataAddressing) {
                        case Direct:
                            return Bundle.getMessage(locale, bundleKey, namedBean, _oblockValue);
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
        Deallocate(Bundle.getMessage("ActionOBlock_Deallocate")),
        SetValue(Bundle.getMessage("ActionOBlock_SetValue")),
        SetError(Bundle.getMessage("ActionOBlock_SetError")),
        ClearError(Bundle.getMessage("ActionOBlock_ClearError")),
        SetOutOfService(Bundle.getMessage("ActionOBlock_SetOutOfService")),
        ClearOutOfService(Bundle.getMessage("ActionOBlock_ClearOutOfService"));

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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionOBlock.class);

}
