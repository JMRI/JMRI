package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.util.ThreadingUtil;

/**
 * This action turns power on or off.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionPower extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectEnum<PowerState> _selectEnum =
            new LogixNG_SelectEnum<>(this, PowerState.values(), PowerState.On, this);


    public ActionPower(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException, ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionPower copy = new ActionPower(sysName, userName);
        copy.setComment(getComment());
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectEnum<PowerState> getSelectEnum() {
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
        AtomicReference<JmriException> exception = new AtomicReference<>();
        PowerState powerState = _selectEnum.evaluateEnum(getConditionalNG());

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            try {
                InstanceManager.getDefault(PowerManager.class).setPower(powerState.getID());
            } catch (JmriException e) {
                exception.set(e);
            }
        });
        if (exception.get() != null) throw exception.get();
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
        return Bundle.getMessage(locale, "Power_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Power_Long", _selectEnum.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        _selectEnum.registerListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectEnum.unregisterListeners();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    public enum PowerState {
        Off(PowerManager.OFF, Bundle.getMessage("PowerStateOff")),
        On(PowerManager.ON, Bundle.getMessage("PowerStateOn"));

        private final int _id;
        private final String _text;

        private PowerState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public PowerState get(int id) {
            switch (id) {
                case PowerManager.OFF:
                    return Off;

                case PowerManager.ON:
                    return On;

                default:
                    throw new IllegalArgumentException("invalid power state");
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
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionPower.class);

}
