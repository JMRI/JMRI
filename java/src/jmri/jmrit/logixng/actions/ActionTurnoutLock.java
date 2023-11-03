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
 * This action sets the lock of a turnout.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionTurnoutLock extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Turnout> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Turnout.class, InstanceManager.getDefault(TurnoutManager.class), this);

    private final LogixNG_SelectEnum<TurnoutLock> _selectEnum =
            new LogixNG_SelectEnum<>(this, TurnoutLock.values(), TurnoutLock.Unlock, this);


    public ActionTurnoutLock(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionTurnoutLock copy = new ActionTurnoutLock(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<Turnout> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<TurnoutLock> getSelectEnum() {
        return _selectEnum;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Turnout turnout = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (turnout == null) return;

        TurnoutLock lock = _selectEnum.evaluateEnum(getConditionalNG());

        if (lock == TurnoutLock.Toggle) {
            if (turnout.getLocked(Turnout.CABLOCKOUT)) {
                lock = TurnoutLock.Unlock;
            } else {
                lock = TurnoutLock.Lock;
            }
        }

        // Variables used in lambda must be effectively final
        TurnoutLock theLock = lock;

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            if (theLock == TurnoutLock.Lock) {
                turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, true);
            } else if (theLock == TurnoutLock.Unlock) {
                turnout.setLocked(Turnout.CABLOCKOUT + Turnout.PUSHBUTTONLOCKOUT, false);
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
        return Bundle.getMessage(locale, "TurnoutLock_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        return Bundle.getMessage(locale, "TurnoutLock_Long", namedBean, state);
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


    public enum TurnoutLock {
        Lock(Bundle.getMessage("TurnoutLock_Lock")),
        Unlock(Bundle.getMessage("TurnoutLock_Unlock")),
        Toggle(Bundle.getMessage("TurnoutLock_Toggle"));

        private final String _text;

        private TurnoutLock(String text) {
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
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionTurnoutLock.class);

}
