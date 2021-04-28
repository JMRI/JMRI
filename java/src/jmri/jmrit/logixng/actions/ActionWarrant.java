package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a warrant.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionWarrant extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private NamedBeanHandle<Warrant> _warrantHandle;
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

    private String _trainIdName = "";
    private ControlAutoTrain _controlAutoTrain = ControlAutoTrain.Halt;

    public ActionWarrant(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionWarrant copy = new ActionWarrant(sysName, userName);
        copy.setComment(getComment());
        copy.setAddressing(_addressing);
        if (_warrantHandle != null) copy.setWarrant(_warrantHandle);
        copy.setReference(_reference);
        copy.setLocalVariable(_localVariable);
        copy.setFormula(_formula);
        copy.setOperationAddressing(_operationAddressing);
        copy.setOperationDirect(_operationDirect);
        copy.setOperationReference(_operationReference);
        copy.setOperationLocalVariable(_operationLocalVariable);
        copy.setOperationFormula(_operationFormula);
        copy.setTrainIdName(_trainIdName);
        copy.setControlAutoTrain(_controlAutoTrain);
        return manager.registerAction(copy);
    }

    public void setWarrant(@Nonnull String warrantName) {
        assertListenersAreNotRegistered(log, "setWarrant");
        Warrant warrant = InstanceManager.getDefault(WarrantManager.class).getNamedBean(warrantName);
        if (warrant != null) {
            ActionWarrant.this.setWarrant(warrant);
        } else {
            removeWarrant();
            log.error("Warrant \"{}\" is not found", warrantName);
        }
    }

    public void setWarrant(@Nonnull Warrant warrant) {
        assertListenersAreNotRegistered(log, "setWarrant");
        ActionWarrant.this.setWarrant(InstanceManager.getDefault(NamedBeanHandleManager.class)
                .getNamedBeanHandle(warrant.getDisplayName(), warrant));
    }

    public void setWarrant(@Nonnull NamedBeanHandle<Warrant> handle) {
        assertListenersAreNotRegistered(log, "setWarrant");
        _warrantHandle = handle;
        InstanceManager.getDefault(WarrantManager.class).addVetoableChangeListener(this);
    }

    public void removeWarrant() {
        assertListenersAreNotRegistered(log, "removeWarrant");
        if (_warrantHandle != null) {
            InstanceManager.getDefault(WarrantManager.class).removeVetoableChangeListener(this);
            _warrantHandle = null;
        }
    }

    public NamedBeanHandle<Warrant> getWarrant() {
        return _warrantHandle;
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

    public void setTrainIdName(@Nonnull String trainIdName) {
        _trainIdName = trainIdName;
    }

    public String getTrainIdName() {
        return _trainIdName;
    }

    public void setControlAutoTrain(ControlAutoTrain controlAutoTrain) {
        _controlAutoTrain = controlAutoTrain;
    }

    public ControlAutoTrain getControlAutoTrain() {
        return _controlAutoTrain;
    }

    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
        if ("CanDelete".equals(evt.getPropertyName())) { // No I18N
            if (evt.getOldValue() instanceof Warrant) {
                if (evt.getOldValue().equals(getWarrant().getBean())) {
                    PropertyChangeEvent e = new PropertyChangeEvent(this, "DoNotDelete", null, null);
                    throw new PropertyVetoException(Bundle.getMessage("ActionWarrant_WarrantInUseVeto", getDisplayName()), e); // NOI18N
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
        Warrant warrant;

        switch (_addressing) {
            case Direct:
                warrant = _warrantHandle != null ? _warrantHandle.getBean() : null;
                break;

            case Reference:
                String ref = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                warrant = InstanceManager.getDefault(WarrantManager.class)
                        .getNamedBean(ref);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                warrant = InstanceManager.getDefault(WarrantManager.class)
                        .getNamedBean(TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false));
                break;

            case Formula:
                warrant = _expressionNode != null ?
                        InstanceManager.getDefault(WarrantManager.class)
                                .getNamedBean(TypeConversionUtil
                                        .convertToString(_expressionNode.calculate(
                                                getConditionalNG().getSymbolTable()), false))
                        : null;
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (warrant == null) {
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

            ThreadingUtil.runOnLayout(() -> {
                String msg;
                String err;
                switch (theOper) {
                    case None:
                        break;
                    case AllocateWarrantRoute:
                        msg = warrant.allocateRoute(false, null);
                        if (msg != null) {
                            log.warn("Warrant {} - {}", warrant.getDisplayName(), msg);  // NOI18N
                        }
                        break;

                    case DeallocateWarrant:
                        warrant.deAllocate();
                        break;

                    case SetRouteTurnouts:
                        msg = warrant.setRoute(false, null);
                        if (msg != null) {
                            log.warn("Warrant {} unable to Set Route - {}", warrant.getDisplayName(), msg);  // NOI18N
                        }
                        break;

                    case AutoRunTrain:
                        jmri.jmrit.logix.WarrantTableFrame frame = jmri.jmrit.logix.WarrantTableFrame.getDefault();
                        err = frame.runTrain(warrant, Warrant.MODE_RUN);
                        if (err != null) {
//                             errorList.add("runAutoTrain error - " + err);  // NOI18N
                            log.warn("runAutoTrain error - {}", err);  // NOI18N
                            warrant.stopWarrant(true, true);
                        }
                        break;

                    case ManuallyRunTrain:
                            err = warrant.setRoute(false, null);
                            if (err == null) {
                                err = warrant.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
                            }
                            if (err != null) {
//                                 errorList.add("runManualTrain error - " + err);  // NOI18N
                                log.warn("runManualTrain error - {}", err);  // NOI18N
                            }
                        break;

                    case ControlAutoTrain:
                        int controlAction = 0;
                        switch (_controlAutoTrain) {
                            case Halt:
                                controlAction = Warrant.HALT;
                                break;
                            case Resume:
                                controlAction = Warrant.RESUME;
                                break;
                            case Abort:
                                controlAction = Warrant.ABORT;
                                break;
                            default:
                                throw new IllegalArgumentException("invalid train control action: " + _controlAutoTrain);
                        }
                        if (!warrant.controlRunTrain(controlAction)) {
                            log.warn("Train {} not running  - {}", warrant.getSpeedUtil().getRosterId(), warrant.getDisplayName());  // NOI18N
                        }
                        break;
                    case SetTrainId:
                        if(!warrant.getSpeedUtil().setAddress(_trainIdName)) {
//                             errorList.add("invalid train ID in action - " + action.getDeviceName());  // NOI18N
                            log.warn("invalid train ID in action - {}", warrant.getDisplayName());  // NOI18N
                        }
                        break;
                    case SetTrainName:
                        warrant.setTrainName(_trainIdName);
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
        return Bundle.getMessage(locale, "ActionWarrant_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean;
        String state = "";

        switch (_addressing) {
            case Direct:
                String warrantName;
                if (_warrantHandle != null) {
                    warrantName = _warrantHandle.getBean().getDisplayName();
                } else {
                    warrantName = Bundle.getMessage(locale, "BeanNotSelected");
                }
                namedBean = Bundle.getMessage(locale, "AddressByDirect", warrantName);
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
                if (_operationDirect == DirectOperation.SetTrainId) {
                    return Bundle.getMessage(locale, "ActionWarrant_Long_Train_Id", namedBean, _trainIdName);
                } else if (_operationDirect == DirectOperation.SetTrainName) {
                    return Bundle.getMessage(locale, "ActionWarrant_Long_Train_Name", namedBean, _trainIdName);
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

        return Bundle.getMessage(locale, "ActionWarrant_Long", namedBean, state);
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
        AllocateWarrantRoute(Bundle.getMessage("ActionWarrant_AllocateWarrantRoute")),
        DeallocateWarrant(Bundle.getMessage("ActionWarrant_DeallocateWarrant")),
        SetRouteTurnouts(Bundle.getMessage("ActionWarrant_SetRouteTurnouts")),
        AutoRunTrain(Bundle.getMessage("ActionWarrant_AutoRunTrain")),
        ManuallyRunTrain(Bundle.getMessage("ActionWarrant_ManuallyRunTrain")),
        ControlAutoTrain(Bundle.getMessage("ActionWarrant_ControlAutoTrain")),
        SetTrainId(Bundle.getMessage("ActionWarrant_SetTrainId")),
        SetTrainName(Bundle.getMessage("ActionWarrant_SetTrainName"));

        private final String _text;

        private DirectOperation(String text) {
            this._text = text;
        }

        @Override
        public String toString() {
            return _text;
        }

    }

    public enum ControlAutoTrain {
        Halt(Bundle.getMessage("ActionWarrant_Halt_AutoTrain")),
        Resume(Bundle.getMessage("ActionWarrant_Resume_AutoTrain")),
        Abort(Bundle.getMessage("ActionWarrant_Abort_AutoTrain"));

        private final String _text;

        private ControlAutoTrain(String text) {
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
        log.debug("getUsageReport :: ActionWarrant: bean = {}, report = {}", cdl, report);
        if (getWarrant() != null && bean.equals(getWarrant().getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionBlock.class);

}
