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
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * Evaluates the state of a SignalHead.
 *
 * @author Daniel Bergqvist Copyright 2020
 */
public class ActionSignalHead extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<SignalHead> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalHead.class, InstanceManager.getDefault(SignalHeadManager.class), this);

    private NamedBeanAddressing _operationAddressing = NamedBeanAddressing.Direct;
    private OperationType _operationType = OperationType.Appearance;
    private String _operationReference = "";
    private String _operationLocalVariable = "";
    private String _operationFormula = "";
    private ExpressionNode _operationExpressionNode;

    private NamedBeanAddressing _appearanceAddressing = NamedBeanAddressing.Direct;
    private int _signalHeadAppearance = SignalHead.DARK;
    private String _appearanceReference = "";
    private String _appearanceLocalVariable = "";
    private String _appearanceFormula = "";
    private ExpressionNode _appearanceExpressionNode;

    private final LogixNG_SelectNamedBean<SignalHead> _selectExampleNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, SignalHead.class, InstanceManager.getDefault(SignalHeadManager.class), this);


    public ActionSignalHead(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSignalHead copy = new ActionSignalHead(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setAppearance(_signalHeadAppearance);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationType(_operationType);
        copy.setOperationFormula(_operationFormula);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationReference(_operationReference);
        copy.setAppearanceAddressing(_appearanceAddressing);
        copy.setAppearanceFormula(_appearanceFormula);
        copy.setAppearanceLocalVariable(_appearanceLocalVariable);
        copy.setAppearanceReference(_appearanceReference);
        _selectExampleNamedBean.copy(copy._selectExampleNamedBean);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<SignalHead> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<SignalHead> getSelectExampleNamedBean() {
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

    public void setAppearanceAddressing(NamedBeanAddressing addressing) throws ParserException {
        _appearanceAddressing = addressing;
        parseAppearanceFormula();
    }

    public NamedBeanAddressing getAppearanceAddressing() {
        return _appearanceAddressing;
    }

    public void setAppearance(int appearance) {
        _signalHeadAppearance = appearance;
    }

    public int getAppearance() {
        return _signalHeadAppearance;
    }

    public void setAppearanceReference(@Nonnull String reference) {
        if ((! reference.isEmpty()) && (! ReferenceUtil.isReference(reference))) {
            throw new IllegalArgumentException("The reference \"" + reference + "\" is not a valid reference");
        }
        _appearanceReference = reference;
    }

    public String getAppearanceReference() {
        return _appearanceReference;
    }

    public void setAppearanceLocalVariable(@Nonnull String localVariable) {
        _appearanceLocalVariable = localVariable;
    }

    public String getAppearanceLocalVariable() {
        return _appearanceLocalVariable;
    }

    public void setAppearanceFormula(@Nonnull String formula) throws ParserException {
        _appearanceFormula = formula;
        parseAppearanceFormula();
    }

    public String getAppearanceFormula() {
        return _appearanceFormula;
    }

    private void parseAppearanceFormula() throws ParserException {
        if (_appearanceAddressing == NamedBeanAddressing.Formula) {
            Map<String, Variable> variables = new HashMap<>();

            RecursiveDescentParser parser = new RecursiveDescentParser(variables);
            _appearanceExpressionNode = parser.parseExpression(_appearanceFormula);
        } else {
            _appearanceExpressionNode = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    private int getAppearanceFromName(String name, SignalHead signalHead) {
        String[] keys = signalHead.getValidStateKeys();
        for (int i=0; i < keys.length; i++) {
            if (name.equals(keys[i])) return signalHead.getValidStates()[i];
        }

        throw new IllegalArgumentException("Appearance "+name+" is not valid for signal head "+signalHead.getSystemName());
    }

    private int getNewAppearance(ConditionalNG conditionalNG, SignalHead signalHead)
            throws JmriException {

        switch (_appearanceAddressing) {
            case Direct:
                return _signalHeadAppearance;

            case Reference:
                return getAppearanceFromName(ReferenceUtil.getReference(
                        conditionalNG.getSymbolTable(), _appearanceReference), signalHead);

            case LocalVariable:
                SymbolTable symbolTable = conditionalNG.getSymbolTable();
                return getAppearanceFromName(TypeConversionUtil
                        .convertToString(symbolTable.getValue(_appearanceLocalVariable), false), signalHead);

            case Formula:
                return _appearanceExpressionNode != null
                        ? getAppearanceFromName(TypeConversionUtil.convertToString(
                                _appearanceExpressionNode.calculate(
                                        conditionalNG.getSymbolTable()), false), signalHead)
                        : -1;

            default:
                throw new IllegalArgumentException("invalid _aspectAddressing state: " + _appearanceAddressing.name());
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
                    if (_appearanceExpressionNode != null) {
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

        SignalHead signalHead = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (signalHead == null) return;

        OperationType operation = getOperation(conditionalNG);

        AtomicReference<JmriException> ref = new AtomicReference<>();
        jmri.util.ThreadingUtil.runOnLayoutWithJmriException(() -> {
            try {
                switch (operation) {
                    case Appearance:
                        int newAppearance = getNewAppearance(conditionalNG, signalHead);
                        if (newAppearance != -1) {
                            signalHead.setAppearance(newAppearance);
                        }
                        break;
                    case Lit:
                        signalHead.setLit(true);
                        break;
                    case NotLit:
                        signalHead.setLit(false);
                        break;
                    case Held:
                        signalHead.setHeld(true);
                        break;
                    case NotHeld:
                        signalHead.setHeld(false);
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
        return Bundle.getMessage(locale, "SignalHead_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String operation;
        String appearance;

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

        switch (_appearanceAddressing) {
            case Direct:
                SignalHead signalHead = null;
                if (_selectNamedBean.getAddressing() == NamedBeanAddressing.Direct) {
                    if (_selectNamedBean.getNamedBeanIfDirectAddressing() != null) {
                        signalHead = _selectNamedBean.getNamedBeanIfDirectAddressing();
                    }
                } else {
                    if (_selectExampleNamedBean.getNamedBean() != null) {
                        signalHead = _selectExampleNamedBean.getNamedBeanIfDirectAddressing();
                    }
                }
                String a = "";
                if (signalHead != null) {
                    a = signalHead.getAppearanceName(_signalHeadAppearance);
                }
                appearance = Bundle.getMessage(locale, "AddressByDirect", a);
                break;

            case Reference:
                appearance = Bundle.getMessage(locale, "AddressByReference", _appearanceReference);
                break;

            case LocalVariable:
                appearance = Bundle.getMessage(locale, "AddressByLocalVariable", _appearanceLocalVariable);
                break;

            case Formula:
                appearance = Bundle.getMessage(locale, "AddressByFormula", _appearanceFormula);
                break;

            default:
                throw new IllegalArgumentException("invalid _stateAddressing state: " + _appearanceAddressing.name());
        }

        if (_operationAddressing == NamedBeanAddressing.Direct) {
            if (_operationType == OperationType.Appearance) {
                return Bundle.getMessage(locale, "SignalHead_LongAppearance", namedBean, appearance);
            } else {
                return Bundle.getMessage(locale, "SignalHead_Long", namedBean, operation);
            }
        } else {
            return Bundle.getMessage(locale, "SignalHead_LongUnknownOper", namedBean, operation, appearance);
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
        Appearance(Bundle.getMessage("SignalHeadOperationType_Appearance")),
        Lit(Bundle.getMessage("SignalHeadOperationType_Lit")),
        NotLit(Bundle.getMessage("SignalHeadOperationType_NotLit")),
        Held(Bundle.getMessage("SignalHeadOperationType_Held")),
        NotHeld(Bundle.getMessage("SignalHeadOperationType_NotHeld"));

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
        log.debug("getUsageReport :: ActionSignalHead: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectExampleNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSignalHead.class);

}
