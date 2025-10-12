package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;

/**
 * This expression checks the flank of the change of the state of a sensor.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ExpressionSensorEdge extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Sensor> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Sensor.class, InstanceManager.getDefault(SensorManager.class), this);

    private final LogixNG_SelectEnum<SensorState> _selectEnumFromState =
            new LogixNG_SelectEnum<>(this, SensorState.values(), SensorState.Active, this);

    private final LogixNG_SelectEnum<SensorState> _selectEnumToState =
            new LogixNG_SelectEnum<>(this, SensorState.values(), SensorState.Active, this);

    private boolean _onlyTrueOnce = false;

    SensorState lastSensorState = null;
    SensorState currentSensorState = null;

    public ExpressionSensorEdge(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _selectNamedBean.setOnlyDirectAddressingAllowed();
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionSensorEdge copy = new ExpressionSensorEdge(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnumFromState.copy(copy._selectEnumFromState);
        _selectEnumToState.copy(copy._selectEnumToState);
        copy.setOnlyTrueOnce(_onlyTrueOnce);
        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<Sensor> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<SensorState> getSelectEnumFromState() {
        return _selectEnumFromState;
    }

    public LogixNG_SelectEnum<SensorState> getSelectEnumToState() {
        return _selectEnumToState;
    }

    public void setOnlyTrueOnce(boolean onlyTrueOnce) {
        _onlyTrueOnce = onlyTrueOnce;
    }

    public boolean getOnlyTrueOnce() {
        return _onlyTrueOnce;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        Sensor sensor = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (sensor == null) return false;

        SensorState checkSensorFromState = _selectEnumFromState.evaluateEnum(getConditionalNG());
        SensorState checkSensorToState = _selectEnumToState.evaluateEnum(getConditionalNG());

        boolean result = (lastSensorState == checkSensorFromState)
                && (currentSensorState == checkSensorToState);

        if (_onlyTrueOnce) {
            lastSensorState = null;
            currentSensorState = null;
        }

        return result;
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
        return Bundle.getMessage(locale, "SensorEdge_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String fromState = _selectEnumFromState.getDescription(locale);
        String toState = _selectEnumToState.getDescription(locale);

        if (_onlyTrueOnce) {
            return Bundle.getMessage(locale, "SensorEdge_LongOnlyTrueOnce", namedBean, fromState, toState);
        } else {
            return Bundle.getMessage(locale, "SensorEdge_Long", namedBean, fromState, toState);
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
        if (!_listenersAreRegistered) {
            _selectNamedBean.addPropertyChangeListener("KnownState", this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener("KnownState", this);
            _selectNamedBean.unregisterListeners();
            _listenersAreRegistered = false;
            lastSensorState = null;
            currentSensorState = null;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if ("KnownState".equals(evt.getPropertyName())) {
            Object oldState = evt.getOldValue();
            Object newState = evt.getNewValue();
            lastSensorState = oldState != null ? SensorState.get((int) oldState) : null;
            currentSensorState = newState != null ? SensorState.get((int) newState) : null;
            getConditionalNG().execute();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum SensorState {
        Unknown(Sensor.INACTIVE, Bundle.getMessage("BeanStateUnknown")),
        Inconsistent(Sensor.ACTIVE, Bundle.getMessage("BeanStateInconsistent")),
        Inactive(Sensor.INACTIVE, Bundle.getMessage("SensorStateInactive")),
        Active(Sensor.ACTIVE, Bundle.getMessage("SensorStateActive"));

        private final int _id;
        private final String _text;

        private SensorState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public SensorState get(int id) {
            switch (id) {
                case Sensor.UNKNOWN:
                    return Inactive;

                case Sensor.INCONSISTENT:
                    return Active;

                case Sensor.INACTIVE:
                    return Inactive;

                case Sensor.ACTIVE:
                    return Active;

                default:
                    return null;
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
        log.debug("getUsageReport :: ExpressionSensorEdge: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSensorEdge.class);
}
