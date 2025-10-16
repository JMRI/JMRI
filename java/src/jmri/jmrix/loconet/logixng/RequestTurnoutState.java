package jmri.jmrix.loconet.logixng;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.jmrit.logixng.actions.*;

import java.util.*;

import jmri.*;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrix.loconet.*;

/**
 * Sets the speed to zero if the loco hasn't been used in a while.
 *
 * @author Daniel Bergqvist Copyright 2025
 */
public class RequestTurnoutState extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Turnout> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Turnout.class, InstanceManager.getDefault(TurnoutManager.class), this);

    private LocoNetSystemConnectionMemo _memo;

    public RequestTurnoutState(String sys, String user, LocoNetSystemConnectionMemo memo) {
        super(sys, user);
        _memo = memo;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        RequestTurnoutState copy = new RequestTurnoutState(sysName, userName, _memo);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerAction(copy).deepCopyChildren(this, systemNames, userNames);
    }

    public LogixNG_SelectNamedBean<Turnout> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return CategoryLocoNet.LOCONET;
    }

    public void setMemo(LocoNetSystemConnectionMemo memo) {
        assertListenersAreNotRegistered(log, "setMemo");
        _memo = memo;
    }

    public LocoNetSystemConnectionMemo getMemo() {
        return _memo;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        Turnout theTurnout = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (theTurnout == null) return;

        if (!(theTurnout instanceof LnTurnout)) {
            throw new IllegalArgumentException(String.format("Turnout %s is not a LocoNet turnout", theTurnout));
        }

        int turnout = ((LnTurnout)theTurnout).getNumber();

        LocoNetMessage msg = new LocoNetMessage(4);
        msg.setOpCode(LnConstants.OPC_SW_STATE);
        msg.setElement(1, (turnout-1) & 0x7f);
        msg.setElement(2, (turnout-1) >> 7);
        _memo.getLnTrafficController().sendLocoNetMessage(msg);
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
        return Bundle.getMessage(locale, "RequestTurnoutState_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        return Bundle.getMessage(locale, "RequestTurnoutState_Long",
                namedBean,
                _memo != null ? _memo.getUserName() : Bundle.getMessage("MemoNotSet"));
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
            _listenersAreRegistered = true;
            _selectNamedBean.registerListeners();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        _selectNamedBean.unregisterListeners();
        _listenersAreRegistered = false;
    }

    /** {@inheritDoc} */
    @Override
    public void getUsageDetail(int level, NamedBean bean, List<NamedBeanUsageReport> report, NamedBean cdl) {
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Action);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RequestTurnoutState.class);

}
