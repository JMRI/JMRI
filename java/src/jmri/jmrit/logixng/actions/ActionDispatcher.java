package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.dispatcher.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.DispatcherTrainInfoManager;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action triggers a Dispather ActiveTrain.
 *
 * @author Daniel Bergqvist Copyright 2021
 * @author Dave Sand Copyright 2021
 */
public class ActionDispatcher extends AbstractDigitalAction implements VetoableChangeListener {

    private NamedBeanAddressing _addressing = NamedBeanAddressing.Direct;
    private String _trainInfoFileName = "";
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

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private boolean _resetOption = false;
    private boolean _terminateOption = false;
    private int _trainPriority = 5;

    public ActionDispatcher(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionDispatcher copy = new ActionDispatcher(sysName, userName);
        copy.setComment(getComment());

        copy.setAddressing(_addressing);
        copy.setTrainInfoFileName(_trainInfoFileName);
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

        copy.setResetOption(_resetOption);
        copy.setTerminateOption(_terminateOption);
        copy.setTrainPriority(_trainPriority);

        return manager.registerAction(copy);
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


    public void setOperationAddressing(NamedBeanAddressing addressing) throws ParserException {
        _operationAddressing = addressing;
        parseOperFormula();
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
        parseOperFormula();
    }

    public String getOperFormula() {
        return _operationFormula;
    }

    private void parseOperFormula() throws ParserException {
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


    public void setTrainPriority(int trainPriority) {
        _trainPriority = trainPriority;
    }

    public int getTrainPriority() {
        return _trainPriority;
    }

    public void setResetOption(boolean resetOption) {
        _resetOption = resetOption;
    }

    public boolean getResetOption() {
        return _resetOption;
    }

    public void setTerminateOption(boolean terminateOption) {
        _terminateOption = terminateOption;
    }

    public boolean getTerminateOption() {
        return _terminateOption;
    }


    @Override
    public void vetoableChange(java.beans.PropertyChangeEvent evt) throws java.beans.PropertyVetoException {
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }


    private String getNewOper() throws JmriException {

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

    private String getNewData() throws JmriException {
        switch (_dataAddressing) {
            case Direct:
                switch(_operationDirect) {
                    case TrainPriority:
                        return String.valueOf(getTrainPriority());

                    case ResetWhenDoneOption:
                        return getResetOption() ? "true" : "false";

                    case TerminateWhenDoneOption:
                        return getTerminateOption() ? "true" : "false";

                    default:
                        return "";
                }

            case Reference:
                return ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _dataReference);

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);

            case Formula:
                return _operationExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(
                                        getConditionalNG().getSymbolTable()), false)
                        : "";

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _dataAddressing.name());
        }
    }


    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        String trainInfoFileName = "";

        switch (_addressing) {
            case Direct:
                trainInfoFileName = _trainInfoFileName;
                break;

            case Reference:
                trainInfoFileName = ReferenceUtil.getReference(
                        getConditionalNG().getSymbolTable(), _reference);
                break;

            case LocalVariable:
                SymbolTable symbolTable = getConditionalNG().getSymbolTable();
                trainInfoFileName = TypeConversionUtil
                                .convertToString(symbolTable.getValue(_localVariable), false);
                break;

            case Formula:
                trainInfoFileName = _expressionNode != null ?
                        TypeConversionUtil.convertToString(_expressionNode.calculate(
                                getConditionalNG().getSymbolTable()), false)
                        : "";
                break;

            default:
                throw new IllegalArgumentException("invalid _addressing state: " + _addressing.name());
        }

        if (trainInfoFileName.isEmpty()) {
            return;
        }

        ActiveTrain activeTrain = DispatcherTrainInfoManager.getActiveTrain(trainInfoFileName);

        String name = (_operationAddressing != NamedBeanAddressing.Direct)
                ? getNewOper() : null;

        DirectOperation oper;
        if ((_operationAddressing == NamedBeanAddressing.Direct)) {
            oper = _operationDirect;
        } else {
            oper = DirectOperation.valueOf(name);
        }

        String newData = getNewData();

        switch (oper) {
            case LoadTrainFromFile:
                if (activeTrain == null) {
                    activeTrain = DispatcherTrainInfoManager.createActiveTrain(trainInfoFileName);
                    if (activeTrain == null) {
                        log.warn("DispatcherAction: Unable to create an active train");
                    }
                } else {
                    log.warn("DispatcherAction: The active train already exists");
                }
                return;

            case TerminateTrain:
                DispatcherTrainInfoManager.terminateActiveTrain(trainInfoFileName);
                return;

            case TrainPriority:
                if (activeTrain != null) {
                    int newInt = Integer.parseInt(newData);
                    if (newInt < 0) newInt = 0;
                    if (newInt > 100) newInt = 100;
                    activeTrain.setPriority(newInt);
                }
                return;

            case ResetWhenDoneOption:
                if (activeTrain != null) {
                    if (newData.equals("true") || newData.equals("false")) {
                        boolean reset = newData.equals("true") ? true : false;
                        activeTrain.setResetWhenDone(reset);
                    }
                }
                return;

            case TerminateWhenDoneOption:
                if (activeTrain != null) {
                    if (newData.equals("true") || newData.equals("false")) {
                        boolean term = newData.equals("true") ? true : false;
                        activeTrain.setTerminateWhenDone(term);
                    }
                }
                return;
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
        return Bundle.getMessage(locale, "ActionDispatcher_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {

// Start train using train info file {abc.xml}
// Terminate train {transit/name}
// Set priority for train {} to {nnn}
// {[Enable|Disable]} "reset when done" for train {}}
// {[Enable|Disable]} "terminate when done" for train using {}}

        String fileName;
        String state = "";

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

        switch (_operationAddressing) {
            case Direct:
                switch (_operationDirect) {
                    case LoadTrainFromFile:
                        return Bundle.getMessage("ActionDispatcher_Long_LoadTrain", fileName);

                    case TerminateTrain:
                        return Bundle.getMessage("ActionDispatcher_Long_Terminate", fileName);

                    case TrainPriority:
                        return getLongDataDescription(locale, "ActionDispatcher_Long_Priority",
                                fileName, String.valueOf(getTrainPriority()));
                    case ResetWhenDoneOption:
                        return getLongDataDescription(locale, "ActionDispatcher_Long_ResetOption",
                                fileName, getResetOption() ? Bundle.getMessage("ActionDispatcher_Long_Enable") :
                                Bundle.getMessage("ActionDispatcher_Long_Disable"));
                    case TerminateWhenDoneOption:
                        return getLongDataDescription(locale, "ActionDispatcher_Long_TerminateOption",
                                fileName, getResetOption() ? Bundle.getMessage("ActionDispatcher_Long_Enable") :
                                Bundle.getMessage("ActionDispatcher_Long_Disable"));

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

        return Bundle.getMessage(locale, "ActionDispatcher_Long", fileName, state);
    }

    private String getLongDataDescription(Locale locale, String bundleKey, String fileName, String value) {
        switch (_dataAddressing) {
            case Direct:
                return Bundle.getMessage(locale, bundleKey, fileName, value);
            case Reference:
                return Bundle.getMessage(locale, bundleKey, fileName, Bundle.getMessage("AddressByReference", _dataReference));
            case LocalVariable:
                return Bundle.getMessage(locale, bundleKey, fileName, Bundle.getMessage("AddressByLocalVariable", _dataLocalVariable));
            case Formula:
                return Bundle.getMessage(locale, bundleKey, fileName, Bundle.getMessage("AddressByFormula", _dataFormula));
            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
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
        LoadTrainFromFile(Bundle.getMessage("ActionDispatcher_LoadTrainFromFile")),
        TerminateTrain(Bundle.getMessage("ActionDispatcher_TerminateTrain")),
        TrainPriority(Bundle.getMessage("ActionDispatcher_TrainPriority")),
        ResetWhenDoneOption(Bundle.getMessage("ActionDispatcher_ResetWhenDoneOption")),
        TerminateWhenDoneOption(Bundle.getMessage("ActionDispatcher_TerminateWhenDoneOption"));

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
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionDispatcher.class);

}
