package jmri.jmrit.logixng.actions;

import java.util.*;

import javax.annotation.Nonnull;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.ReferenceUtil;
import jmri.jmrit.logixng.util.parser.*;
import jmri.jmrit.logixng.util.parser.ExpressionNode;
import jmri.jmrit.logixng.util.parser.RecursiveDescentParser;
import jmri.util.ThreadingUtil;
import jmri.util.TypeConversionUtil;

/**
 * This action sets the state of a sensor.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionSensor extends AbstractDigitalAction {

    private final LogixNG_SelectNamedBean<Sensor> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Sensor.class, InstanceManager.getDefault(SensorManager.class));
    private NamedBeanAddressing _stateAddressing = NamedBeanAddressing.Direct;
    private SensorState _sensorState = SensorState.Active;
    private String _stateReference = "";
    private String _stateLocalVariable = "";
    private String _stateFormula = "";
    private ExpressionNode _stateExpressionNode;

    public ActionSensor(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionSensor copy = new ActionSensor(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        copy.setBeanState(_sensorState);
        copy.setStateAddressing(_stateAddressing);
        copy.setStateFormula(_stateFormula);
        copy.setStateLocalVariable(_stateLocalVariable);
        copy.setStateReference(_stateReference);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Sensor> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public void setStateAddressing(NamedBeanAddressing addressing) throws ParserException {
        _stateAddressing = addressing;
        parseStateFormula();
    }

    public NamedBeanAddressing getStateAddressing() {
        return _stateAddressing;
    }

    public void setBeanState(SensorState state) {
        _sensorState = state;
    }

    public SensorState getBeanState() {
        return _sensorState;
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

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
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
    public void execute() throws JmriException {
        Sensor sensor = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (sensor == null) {
//            log.warn("sensor is null");
            return;
        }

        String name = (_stateAddressing != NamedBeanAddressing.Direct)
                ? getNewState() : null;

        SensorState state;
        if ((_stateAddressing == NamedBeanAddressing.Direct)) {
            state = _sensorState;
        } else {
            state = SensorState.valueOf(name);
        }

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (state == SensorState.Toggle) {
                if (sensor.getKnownState() == Sensor.INACTIVE) {
                    sensor.setCommandedState(Sensor.ACTIVE);
                } else {
                    sensor.setCommandedState(Sensor.INACTIVE);
                }
            } else {
                sensor.setCommandedState(state.getID());
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
        return Bundle.getMessage(locale, "Sensor_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

        switch (_stateAddressing) {
            case Direct:
                state = Bundle.getMessage(locale, "AddressByDirect", _sensorState._text);
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

        return Bundle.getMessage(locale, "Sensor_Long", namedBean, state);
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


    // This constant is only used internally in SensorState but must be outside
    // the enum.
    private static final int TOGGLE_ID = -1;


    public enum SensorState {
        Inactive(Sensor.INACTIVE, Bundle.getMessage("SensorStateInactive")),
        Active(Sensor.ACTIVE, Bundle.getMessage("SensorStateActive")),
        Toggle(TOGGLE_ID, Bundle.getMessage("SensorToggleStatus")),
        Unknown(Sensor.UNKNOWN, Bundle.getMessage("BeanStateUnknown")),
        Inconsistent(Sensor.INCONSISTENT, Bundle.getMessage("BeanStateInconsistent"));

        private final int _id;
        private final String _text;

        private SensorState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public SensorState get(int id) {
            switch (id) {
                case Sensor.UNKNOWN:
                    return Unknown;

                case Sensor.INCONSISTENT:
                    return Inconsistent;

                case Sensor.INACTIVE:
                    return Inactive;

                case Sensor.ACTIVE:
                    return Active;

                case TOGGLE_ID:
                    return Toggle;

                default:
                    throw new IllegalArgumentException("invalid sensor state");
            }
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
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSensor.class);
}
