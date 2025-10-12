package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;

/**
 * This action sets the state of a sensor.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionSensor extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Sensor> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Sensor.class, InstanceManager.getDefault(SensorManager.class), this);

    private final LogixNG_SelectEnum<SensorState> _selectEnum =
            new LogixNG_SelectEnum<>(this, SensorState.values(), SensorState.Active, this);


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
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Sensor> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<SensorState> getSelectEnum() {
        return _selectEnum;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Sensor sensor = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (sensor == null) return;

        SensorState state = _selectEnum.evaluateEnum(getConditionalNG());

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
        String state = _selectEnum.getDescription(locale);

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
        _selectNamedBean.registerListeners();
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _selectEnum.unregisterListeners();
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

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionSensor.class);
}
