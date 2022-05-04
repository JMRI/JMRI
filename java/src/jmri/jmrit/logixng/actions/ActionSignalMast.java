package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a SignalMast.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionSignalMast extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<SignalMast> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalMast.class, InstanceManager.getDefault(SignalMastManager.class), this);

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private OperationType _operationType = OperationType.Aspect;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    private NamedBeanAddressing _aspectAddressing = NamedBeanAddressing.Direct;
    private String _signalMastAspect = "";
    private String _aspectReference = "";
    private String _aspectLocalVariable = "";
    private String _aspectFormula = "";
    private ExpressionNode _aspectExpressionNode;

    private final LogixNG_SelectNamedBean<SignalMast> _selectExampleNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalMast.class, InstanceManager.getDefault(SignalMastManager.class), this);


    public ActionSignalMast(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSignalMast copy = new ActionSignalMast(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setAspect(_signalMastAspect);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationType(_operationType);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        copy.setAspectAddressing(_aspectAddressing);
        copy.setAspectFormula(_aspectFormula);
        copy.setAspectLocalVariable(_aspectLocalVariable);
        copy.setAspectReference(_aspectReference);
        _selectExampleNamedBean.copy(copy._selectExampleNamedBean);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<SignalMast> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<SignalMast> getSelectExampleNamedBean() {
        return _selectExampleNamedBean;
    }

    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseOperationFormula();
    }

    public NamedBeanAddressing getOperationAddressing() {
        return _operationAddressing;
    }

    public void setOperationType(OperationType operationType) {
        _operationType = operationType;
    }

    public OperationType getOperationType() {
        return _operationType;
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

    public void setAspectAddressing(NamedBeanAddressing addressing) throws ParserException {
        _aspectAddressing = addressing;
        parseAspectFormula();
    }

    public NamedBeanAddressing getAspectAddressing() {
        return _aspectAddressing;
    }

    public void setAspect(String aspect) {
        if (aspect == null) _signalMastAspect = "";
        else _signalMastAspect = aspect;
    }

    public String getAspect() {
        return _signalMastAspect;
    }

    public void setAspectReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _aspectReference = reference;
    }

    public String getAspectReference() {
        return _aspectReference;
    }

    public void setAspectLocalVariable(@Nonnull String localVariable) {
        _aspectLocalVariable = localVariable;
    }

    public String getAspectLocalVariable() {
        return _aspectLocalVariable;
    }

    public void setAspectFormula(@Nonnull String formula) throws ParserException {
        _aspectFormula = formula;
        parseAspectFormula();
    }

    public String getAspectFormula() {
        return _aspectFormula;
    }

    private void parseAspectFormula() throws ParserException {
        if (_aspectAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _aspectExpressionNode = parser.parseExpression(_aspectFormula);
        } else {
            _aspectExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getNewAspect(ConditionalNG conditionalNG) throws JmriException {

        switch (_aspectAddressing) {
            case Direct:
                return _signalMastAspect;

            case Reference:
                return ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _aspectReference);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_aspectLocalVariable), false);

            case Formula:
                return _aspectExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _aspectExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _aspectAddressing.name());
        }
    }

    private OperationType getOperation(ConditionalNG conditionalNG) throws JmriException {

        String oper = "";
        try {
            switch (_operationAddressing) {
                case Direct:
                    return _operationType;

                case Reference:
                    oper = ReferenceUtil.getReference(
                            conditionalNG.getSymbolTable(), _operationReference);
                    return OperationType.valueOf(oper);

                case LocalVariable:
                    SymbolTable symbolTable = conditionalNG.getSymbolTable();
                    oper = TypeConversionUtil
                            .convertToString(symbolTable.getValue(_operationLocalVariable), false);
                    return OperationType.valueOf(oper);

                case Formula:
                    if (_aspectExpressionNode != null) {
                        oper = TypeConversionUtil.convertToString(
                                _operationExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false);
                        return OperationType.valueOf(oper);
                    } else {
                        return null;
                    }
                default:
                    throw new IllegalArgumentException("invalid _addressing state: " + _operationAddressing.name());
            }
        } catch (IllegalArgumentException e) {
            throw new JmriException("Unknown operation: "+oper, e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        final ConditionalNG conditionalNG = getConditionalNG();

        SignalMast signalMast = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (signalMast == null) return;

        OperationType operation = getOperation(conditionalNG);

        AtomicReference<JmriException> ref = new AtomicReference<>();
        jmri.util.ThreadingUtil.runOnLayoutWithJmriException(() -> {
            try {
                switch (operation) {
                    case Aspect:
                        String newAspect = getNewAspect(conditionalNG);
                        if (!newAspect.isEmpty()) {
                            signalMast.setAspect(newAspect);
                        }
                        break;
                    case Lit:
                        signalMast.setLit(true);
                        break;
                    case NotLit:
                        signalMast.setLit(false);
                        break;
                    case Held:
                        signalMast.setHeld(true);
                        break;
                    case NotHeld:
                        signalMast.setHeld(false);
                        break;
                    case PermissiveSmlDisabled:
                        signalMast.setPermissiveSmlDisabled(true);
                        break;
                    case PermissiveSmlNotDisabled:
                        signalMast.setPermissiveSmlDisabled(false);
                        break;
                    default:
                        throw new JmriException("Unknown enum: "+_operationType.name());
                }
            } catch (JmriException e) {
                ref.set(e);
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
        return Bundle.getMessage(locale, "SignalMast_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String operation;
        String aspect;

        switch (_operationAddressing) {
            case Direct:
                operation = Bundle.getMessage(locale, "AddressByDirect", _operationType._text);
                break;

            case Reference:
                operation = Bundle.getMessage(locale, "AddressByReference", _operationReference);
                break;

            case LocalVariable:
                operation = Bundle.getMessage(locale, "AddressByLocalVariable", _operationLocalVariable);
                break;

            case Formula:
                operation = Bundle.getMessage(locale, "AddressByFormula", _operationFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _operationAddressing state: " + _operationAddressing.name());
        }

        switch (_aspectAddressing) {
            case Direct:
                aspect = Bundle.getMessage(locale, "AddressByDirect", _signalMastAspect);
                break;

            case Reference:
                aspect = Bundle.getMessage(locale, "AddressByReference", _aspectReference);
                break;

            case LocalVariable:
                aspect = Bundle.getMessage(locale, "AddressByLocalVariable", _aspectLocalVariable);
                break;

            case Formula:
                aspect = Bundle.getMessage(locale, "AddressByFormula", _aspectFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _aspectAddressing.name());
        }

        if (_operationAddressing == NamedBeanAddressing.Direct) {
            if (_operationType == OperationType.Aspect) {
                return Bundle.getMessage(locale, "SignalMast_LongAspect", namedBean, aspect);
            } else {
                return Bundle.getMessage(locale, "SignalMast_Long", namedBean, operation);
            }
        } else {
            return Bundle.getMessage(locale, "SignalMast_LongUnknownOper", namedBean, operation, aspect);
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
        _selectNamedBean.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
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



    public enum OperationType {
        Aspect(Bundle.getMessage("SignalMastOperationType_Aspect")),
        Lit(Bundle.getMessage("SignalMastOperationType_Lit")),
        NotLit(Bundle.getMessage("SignalMastOperationType_NotLit")),
        Held(Bundle.getMessage("SignalMastOperationType_Held")),
        NotHeld(Bundle.getMessage("SignalMastOperationType_NotHeld")),
        PermissiveSmlDisabled(Bundle.getMessage("SignalMastOperationType_PermissiveSmlDisabled")),
        PermissiveSmlNotDisabled(Bundle.getMessage("SignalMastOperationType_PermissiveSmlNotDisabled"));

        private final String _text;

        private OperationType(String text) {
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
        log.debug("getUsageReport :: ActionSignalMast: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectExampleNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalMast.class);

}
