package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.*;

/**
 * This expression evaluates the state of a Transit.
 * The supported characteristics are:
 * <ul>
 *   <li>Is [not] Idle</li>
 *   <li>Is [not] Assigned</li>
 * </ul>
 * @author Dave Sand Copyright 2023
 */
public class ExpressionTransit extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Transit> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Transit.class, InstanceManager.getDefault(TransitManager.class), this);

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;

    private final LogixNG_SelectEnum<TransitState> _selectEnum =
            new LogixNG_SelectEnum<>(this, TransitState.values(), TransitState.Idle, this);

    public ExpressionTransit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionTransit copy = new ExpressionTransit(sysName, userName);
        copy.setComment(getComment());

        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);

        copy.set_Is_IsNot(_is_IsNot);

        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<Transit> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<TransitState> getSelectEnum() {
        return _selectEnum;
    }

    public void set_Is_IsNot(Is_IsNot_Enum is_IsNot) {
        _is_IsNot = is_IsNot;
    }

    public Is_IsNot_Enum get_Is_IsNot() {
        return _is_IsNot;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.ITEM;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        ConditionalNG conditionalNG = getConditionalNG();

        Transit transit = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (transit == null) return false;

        TransitState checkTransitState = _selectEnum.evaluateEnum(conditionalNG);

        int currentState = transit.getState();

        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentState == checkTransitState.getID();
        } else {
            return currentState != checkTransitState.getID();
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
        return Bundle.getMessage(locale, "Transit_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

        if (_selectEnum.isDirectAddressing()) {
            TransitState transitState = _selectEnum.getEnum();
            state = Bundle.getMessage(locale, "AddressByDirect", transitState._text);
        } else {
            state = _selectEnum.getDescription(locale);
        }

        return Bundle.getMessage(locale, "Transit_Long", namedBean, _is_IsNot.toString(), state);
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
            _selectNamedBean.addPropertyChangeListener(this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener(this);
            _selectNamedBean.unregisterListeners();
            _listenersAreRegistered = false;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

    public enum TransitState {
        Idle(Transit.IDLE, Bundle.getMessage("Transit_StateIdle")),
        Assigned(Transit.ASSIGNED, Bundle.getMessage("Transit_StateAssigned"));

        private final int _id;
        private final String _text;

        private TransitState(int id, String text) {
            this._id = id;
            this._text = text;
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
        log.debug("getUsageReport :: ExpressionTransit: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionTransit.class);

}
