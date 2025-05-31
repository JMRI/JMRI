package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logix.Warrant;
import jmri.jmrit.logix.WarrantManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
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
 * @author Pete Cressman Copyright (C) 2022
 */
public class ActionWarrant extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Warrant> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Warrant.class, InstanceManager.getDefault(WarrantManager.class), this);

    private final LogixNG_SelectEnum<DirectOperation> _selectEnum =
            new LogixNG_SelectEnum<>(this, DirectOperation.values(), DirectOperation.AllocateWarrantRoute, this);

    private final LogixNG_SelectNamedBean<Memory> _selectMemoryNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Memory.class, InstanceManager.getDefault(MemoryManager.class), this);

    private NamedBeanAddressing _dataAddressing = NamedBeanAddressing.Direct;
    private String _dataReference = "";
    private String _dataLocalVariable = "";
    private String _dataFormula = "";
    private ExpressionNode _dataExpressionNode;

    private String _trainData = "";
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
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectMemoryNamedBean.copy(copy._selectMemoryNamedBean);
        _selectEnum.copy(copy._selectEnum);

        copy.setDataAddressing(_dataAddressing);
        copy.setDataReference(_dataReference);
        copy.setDataLocalVariable(_dataLocalVariable);
        copy.setDataFormula(_dataFormula);

        copy.setTrainData(_trainData);
        copy.setControlAutoTrain(_controlAutoTrain);

        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Warrant> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectNamedBean<Memory> getSelectMemoryNamedBean() {
        return _selectMemoryNamedBean;
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

    public void setTrainData(@Nonnull String trainData) {
        _trainData = trainData;
    }

    public String getTrainData() {
        return _trainData;
    }

    public void setControlAutoTrain(ControlAutoTrain controlAutoTrain) {
        _controlAutoTrain = controlAutoTrain;
    }

    public ControlAutoTrain getControlAutoTrain() {
        return _controlAutoTrain;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }


    private String getNewData(DirectOperation theOper, SymbolTable symbolTable)
            throws JmriException {

        switch (_dataAddressing) {
            case Direct:
                switch(theOper) {
                    case SetTrainId:
                    case SetTrainName:
                        return _trainData;
                    case ControlAutoTrain:
                        return _controlAutoTrain.name();
                    default:
                        return "";
                }

            case Reference:
                return ReferenceUtil.getReference(symbolTable, _dataReference);

            case LocalVariable:
                return TypeConversionUtil
                        .convertToString(symbolTable.getValue(_dataLocalVariable), false);

            case Formula:
                return _dataExpressionNode != null
                        ? TypeConversionUtil.convertToString(
                                _dataExpressionNode.calculate(symbolTable), false)
                        : null;

            default:
                throw new IllegalArgumentException("invalid _dataAddressing state: " + _dataAddressing.name());
        }
    }


    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        final ConditionalNG conditionalNG = getConditionalNG();
        Warrant warrant = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (warrant == null) {
            return;
        }

        SymbolTable symbolTable = conditionalNG.getSymbolTable();

        // Variables used in lambda must be effectively final
        DirectOperation theOper = _selectEnum.evaluateEnum(conditionalNG);

        if (!theOper.equals(DirectOperation.GetTrainLocation)) {
            if (warrant.getRunMode() == Warrant.MODE_RUN && !theOper.equals(DirectOperation.ControlAutoTrain)) {
                throw new JmriException("Cannot \"" + theOper.toString() + "\" when warrant is running - " + warrant.getDisplayName());  // NOI18N
//                log.info("Cannot \"{}\" when warrant is running - {}", theOper.toString(), warrant.getDisplayName());
//                return;
            }
        }

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            String msg;
            String err;

            switch (theOper) {
                case AllocateWarrantRoute:
                    warrant.allocateRoute(false, null);
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
                        warrant.stopWarrant(true, true);
                        throw new JmriException("runAutoTrain error - " + err);  // NOI18N
                    }
                    break;

                case ManuallyRunTrain:
                        err = warrant.setRoute(false, null);
                        if (err == null) {
                            err = warrant.setRunMode(Warrant.MODE_MANUAL, null, null, null, false);
                        }
                        if (err != null) {
                            throw new JmriException("runManualTrain error - " + err);  // NOI18N
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
                        case Stop:
                            controlAction = Warrant.STOP;
                            break;
                        case EStop:
                            controlAction = Warrant.ESTOP;
                            break;
                        case SpeedUp:
                            controlAction = Warrant.SPEED_UP;
                            break;
                        case MoveToNext:
                            controlAction = Warrant.RETRY_FWD;
                            break;
                        case Abort:
                            controlAction = Warrant.ABORT;
                            break;
                        default:
                            throw new IllegalArgumentException("invalid train control action: " + _controlAutoTrain);
                    }
                    if (!warrant.controlRunTrain(controlAction)) {
                        log.info("Warrant {} {}({}) failed. - {}", warrant.getDisplayName(),
                                theOper.toString(), _controlAutoTrain.toString(), warrant.getMessage());
                        throw new JmriException("Warrant " + warrant.getDisplayName() + " "
                              + theOper.toString() +"(" + _controlAutoTrain.toString() + ") failed. "
                              + warrant.getMessage());
                    }
                    break;

                case SetTrainId:
                    if(!warrant.getSpeedUtil().setAddress(getNewData(theOper, symbolTable))) {
                        throw new JmriException("invalid train ID in action - " + warrant.getDisplayName());  // NOI18N
                    }
                    break;

                case SetTrainName:
                    warrant.setTrainName(getNewData(theOper, symbolTable));
                    break;

                case GetTrainLocation:
                    Memory memory = _selectMemoryNamedBean.evaluateNamedBean(conditionalNG);
                    if (memory != null) {
                        memory.setValue(warrant.getCurrentBlockName());
                    } else {
                        throw new JmriException("Memory for GetTrainLocation is null for warrant - " + warrant.getDisplayName());  // NOI18N
                    }
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
        return Bundle.getMessage(locale, "ActionWarrant_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);
        String getLocationMemory = _selectMemoryNamedBean.getDescription(locale);

        if (_selectEnum.getAddressing() == NamedBeanAddressing.Direct) {
            if (_selectEnum.getEnum() != null) {
                switch (_selectEnum.getEnum()) {
                    case SetTrainId:
                        return getLongDataDescription(locale, "ActionWarrant_Long_Train_Id", namedBean, _trainData);
                    case SetTrainName:
                        return getLongDataDescription(locale, "ActionWarrant_Long_Train_Name", namedBean, _trainData);
                    case ControlAutoTrain:
                        return getLongDataDescription(locale, "ActionWarrant_Long_Control", namedBean, _controlAutoTrain.name());
                    case GetTrainLocation:
                        return getLongDataDescription(locale, "ActionWarrant_Long_Location", namedBean, getLocationMemory);
                    default:
                        // Fall thru and handle it in the end of the method
                }
            }
        }

        return Bundle.getMessage(locale, "ActionWarrant_Long", namedBean, state);
    }

    private String getLongDataDescription(Locale locale, String bundleKey, String namedBean, String value) {
        switch (_dataAddressing) {
            case Direct:
                return Bundle.getMessage(locale, bundleKey, namedBean, value);
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
        _selectMemoryNamedBean.addPropertyChangeListener("value", this);
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _selectEnum.unregisterListeners();
        _selectMemoryNamedBean.removePropertyChangeListener("value", this);
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
        SetTrainName(Bundle.getMessage("ActionWarrant_SetTrainName")),
        GetTrainLocation(Bundle.getMessage("ActionWarrant_GetTrainLocation"));

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
        Abort(Bundle.getMessage("ActionWarrant_Abort_AutoTrain")),
        Stop(Bundle.getMessage("ActionWarrant_Stop_AutoTrain")),
        EStop(Bundle.getMessage("ActionWarrant_EStop_AutoTrain")),
        MoveToNext(Bundle.getMessage("ActionWarrant_MoveToNext_AutoTrain")),
        SpeedUp(Bundle.getMessage("ActionWarrant_SpeedUp_AutoTrain"));

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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
        _selectMemoryNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionWarrant.class);

}
