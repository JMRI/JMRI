package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.dispatcher.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.DispatcherActiveTrainManager;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.TypeConversionUtil;

/**
 * This expression checks the status or mode of an active train.
 * <p>
 * A Dispatcher ActiveTrain is a transient object.  The DispatcherActiveTrainManager is a special
 * LogiNG class which manages the relationships with expressions using the Dispatcher TrainInfo file
 * as the key. This makes it possible to add and remove ActiveTrain PropertyChange listeners.
 *
 * @author Dave Sand Copyright 2021
 */
public class ExpressionDispatcher extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _reference = "";
    private String _localVariable = "";
    private String _formula = "";
    private ExpressionNode _expressionNode;

    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    private String _trainInfoFileName = "";
    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;
    private DispatcherState _dispatcherState = DispatcherState.Mode;

    private final DispatcherActiveTrainManager _atManager;
    private boolean _activeTrainListeners = false;

    /**
     * An active train is transient.  It can be terminated manually which means the reference
     * will no longer be valid.
     */
    private ActiveTrain _activeTrain = null;


    public ExpressionDispatcher(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _atManager = InstanceManager.getDefault(DispatcherActiveTrainManager.class);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionDispatcher copy = new ExpressionDispatcher(sysName, userName);
        copy.setComment(getComment());

        copy.setTrainInfoFileName(_trainInfoFileName);
        copy.setBeanState(getBeanState());

        copy.setBeanState(_dispatcherState);
        copy.setAddressing(_addressing);
        copy.setFormula(_formula);
        copy.setLocalVariable(_localVariable);
        copy.setReference(_reference);

        copy.set_Is_IsNot(_is_IsNot);

        copy.setStateAddressing(_stateAddressing);
        copy.setStateFormula(_stateFormula);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateReference(_stateReference);


        return manager.registerExpression(copy);
    }


    public void setTrainInfoFileName(@Nonnull String fileName) {
        _trainInfoFileName = fileName;
    }

    public String getTrainInfoFileName() {
        return _trainInfoFileName;
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


    public void setBeanState(DispatcherState state) {
        _dispatcherState = state;
    }

    public DispatcherState getBeanState() {
        return _dispatcherState;
    }


    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    private String getSelectedFileName() throws JmriException {
        switch (_addressing) {
            case Direct:
                return getTrainInfoFileName();

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_localVariable), false);

            case Formula:
                return _expressionNode != null ?
                        TypeConversionUtil.convertToString(_expressionNode.calculate(
                                getConditionalNG().getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }
    }

    private String getNewState() throws JmriException {

        switch (_stateAddressing) {
            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _stateReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
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
        String trainInfoFileName = getSelectedFileName();

        if (trainInfoFileName.isEmpty()) {
            return false;
        }

        DispatcherState checkDispatcherState;
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            checkDispatcherState = _dispatcherState;
        } else {
            checkDispatcherState = DispatcherState.valueOf(getNewState());
        }

        boolean result = false;

        switch (checkDispatcherState) {
            case Automatic:
                if (_activeTrain != null) result = (_activeTrain.getMode() == ActiveTrain.AUTOMATIC);
                break;
            case Dispatched:
                if (_activeTrain != null) result = (_activeTrain.getMode() == ActiveTrain.DISPATCHED);
                break;
            case Manual:
                if (_activeTrain != null) result = (_activeTrain.getMode() == ActiveTrain.MANUAL);
                break;
            case Running:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.RUNNING);
                break;
            case Paused:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.PAUSED);
                break;
            case Waiting:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.WAITING);
                break;
            case Working:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.WORKING);
                break;
            case Ready:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.READY);
                break;
            case Stopped:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.STOPPED);
                break;
            case Done:
                if (_activeTrain != null) result = (_activeTrain.getStatus() == ActiveTrain.DONE);
                break;

            default:
                throw new UnsupportedOperationException("checkDispatcherState has unknown value: " + checkDispatcherState.name());
        }

        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return result;
        } else {
            return !result;
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
        return Bundle.getMessage(locale, "Dispatcher_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String fileName;
        String state;

        switch (_addressing) {
            case Direct:
                fileName = Bundle.getMessage(locale, "AddressByDirect", _trainInfoFileName);
                break;

            case Reference:
                fileName = Bundle.getMessage(locale, "AddressByReference", _reference);
                break;

            case LocalVariable:
                fileName = Bundle.getMessage(locale, "AddressByLocalVariable", _localVariable);
                break;

            case Formula:
                fileName = Bundle.getMessage(locale, "AddressByFormula", _formula);
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        switch (_stateAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _dispatcherState._text);
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

        return Bundle.getMessage(locale, "Dispatcher_Long", fileName, _is_IsNot.toString(), state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (! _listenersAreRegistered) {
            _atManager.addPropertyChangeListener(this);
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _atManager.removePropertyChangeListener(this);
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc}
     * ActiveTrain is created by DispatcherActiveTrainManager
     * status and mode are created by Dispatcher ActiveTrain
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case "ActiveTrain":
                manageActiveTrain(evt);
                break;

            case "status":
            case "mode":
                getConditionalNG().execute();
                break;

            default:
                log.debug("Other property changes are ignored: name = " + evt.getPropertyName());
        }
    }

    /**
     * The DispatcherActiveTrainManager keeps track of the ActiveTrains created using LogixNG.
     * When an ActiveTrain is added, ActiveTrain property change listeners are added so that the
     * expression can be notified of status and mode changes.  _activeTrain is updated with the
     * with current active train.
     * <p>
     * When an ActiveTrain is removed, the listeners are removed and _activeTrain is set to null.
     * @param event The DispatcherActiveTrainManager property change event.
     */
    private void manageActiveTrain(PropertyChangeEvent event) {
        String selectedFileName;
        try {
            selectedFileName = getSelectedFileName();
        } catch (JmriException ex) {
            log.warn("Unexpected exception, using Direct file name");
            selectedFileName = _trainInfoFileName;
        }

        String eventFileName = (String) event.getOldValue();
        if (eventFileName.isEmpty()) {
            eventFileName = (String) event.getNewValue();
        }

        if (! selectedFileName.equals(eventFileName)) return;

        ActiveTrain checkTrain = _atManager.getActiveTrain(selectedFileName);

        if (checkTrain == null) {
            if (_activeTrain != null) {
                if (_activeTrainListeners) {
                    _activeTrain.removePropertyChangeListener(this);
                    _activeTrainListeners = false;
                }
                _activeTrain = null;
            }
            return;
        }

        if (checkTrain == _activeTrain) return;

        if (_activeTrain == null) {
            _activeTrain = checkTrain;
            _activeTrainListeners = false;
        }

        if (! _activeTrainListeners) {
            _activeTrain.addPropertyChangeListener(this);
            _activeTrainListeners = true;
        }
    }


    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum DispatcherState {
        Mode(Bundle.getMessage("DispatcherSeparatorMode"), "Separator"),
        Automatic(Bundle.getMessage("DispatcherMode_Automatic"), "Mode"),
        Dispatched(Bundle.getMessage("DispatcherMode_Dispatched"), "Mode"),
        Manual(Bundle.getMessage("DispatcherMode_Manual"), "Mode"),
        Status(Bundle.getMessage("DispatcherSeparatorStatus"), "Separator"),
        Running(Bundle.getMessage("DispatcherStatus_Running"), "Status"),
        Paused(Bundle.getMessage("DispatcherStatus_Paused"), "Status"),
        Waiting(Bundle.getMessage("DispatcherStatus_Waiting"), "Status"),
        Working(Bundle.getMessage("DispatcherStatus_Working"), "Status"),
        Ready(Bundle.getMessage("DispatcherStatus_Ready"), "Status"),
        Stopped(Bundle.getMessage("DispatcherStatus_Stopped"), "Status"),
        Done(Bundle.getMessage("DispatcherStatus_Done"), "Status");

        private final String _text;
        private final String _type;

        private DispatcherState(String text, String type) {
            this._text = text;
            this._type = type;
        }

        public String getType() {
            return _type;
        }

        @Override
        public String toString() {
            return _text;
        }

    }


    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        log.debug("getUsageReport :: ExpressionDispatcher: bean = {}, report = {}", cdl, report);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionDispatcher.class);

}
