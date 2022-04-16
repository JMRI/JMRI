package jmri.jmrit.logixng.actions;

import java.beans.VetoableChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.util.ThreadingUtil;

/**
 * This action sets the state of a turnout.
 *
 * @author Daniel Bergqvist Copyright 2018
 */
public class ActionTurnout extends AbstractDigitalAction implements VetoableChangeListener {

    private final LogixNG_SelectNamedBean<Turnout> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Turnout.class, InstanceManager.getDefault(TurnoutManager.class));
    private final LogixNG_SelectEnum<TurnoutState> _selectEnum =
            new LogixNG_SelectEnum<>(this, TurnoutState.values(), TurnoutState.Thrown);


    public ActionTurnout(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user, Category.ITEM);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionTurnout copy = new ActionTurnout(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Turnout> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<TurnoutState> getSelectEnum() {
        return _selectEnum;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Turnout turnout = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (turnout == null) {
            return;
        }

        TurnoutState state = _selectEnum.evaluateEnum(getConditionalNG());

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (state == TurnoutState.Toggle) {
                if (turnout.getKnownState() == Turnout.CLOSED) {
                    turnout.setCommandedState(Turnout.THROWN);
                } else {
                    turnout.setCommandedState(Turnout.CLOSED);
                }
            } else {
                turnout.setCommandedState(state.getID());
            }
        });
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Turnout_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        return Bundle.getMessage(locale, "Turnout_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }


    // This constant is only used internally in TurnoutState but must be outside
    // the enum.
    private static final int TOGGLE_ID = -1;


    public enum TurnoutState {
        Closed(Turnout.CLOSED, InstanceManager.getDefault(TurnoutManager.class).getClosedText()),
        Thrown(Turnout.THROWN, InstanceManager.getDefault(TurnoutManager.class).getThrownText()),
        Toggle(TOGGLE_ID, Bundle.getMessage("TurnoutToggleStatus")),
        Unknown(Turnout.UNKNOWN, Bundle.getMessage("BeanStateUnknown")),
        Inconsistent(Turnout.INCONSISTENT, Bundle.getMessage("BeanStateInconsistent"));

        private final int _id;
        private final String _text;

        private TurnoutState(int id, String text) {
            this._id = id;
            this._text = text;
        }

        static public TurnoutState get(int id) {
            switch (id) {
                case Turnout.UNKNOWN:
                    return Unknown;

                case Turnout.INCONSISTENT:
                    return Inconsistent;

                case Turnout.CLOSED:
                    return Closed;

                case Turnout.THROWN:
                    return Thrown;

                case TOGGLE_ID:
                    return Toggle;

                default:
                    throw new IllegalArgumentException("invalid turnout state");
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
        log.debug("getUsageReport :: ActionTurnout: bean = {}, report = {}", cdl, report);
        NamedBeanHandle<Turnout> handle = _selectNamedBean.getNamedBean();
        if (handle != null && bean.equals(handle.getBean())) {
            report.add(new NamedBeanUsageReport("LogixNGAction", cdl, getLongDescription()));
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnout.class);

}
