package jmri.jmrit.logixng.expressions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.LogixNG_SelectEnum;
import jmri.jmrit.logixng.util.parser.*;

/**
 * This expression evaluates the state of a Section.
 * The supported characteristics are:
 * <ul>
 *   <li>Is [not] Free</li>
 *   <li>Is [not] Forward</li>
 *   <li>Is [not] Reverse</li>
 * </ul>
 * @author Dave Sand Copyright 2023
 */
public class ExpressionSection extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<Section> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, Section.class, InstanceManager.getDefault(SectionManager.class), this);

    private Is_IsNot_Enum _is_IsNot = Is_IsNot_Enum.Is;

    private final LogixNG_SelectEnum<SectionState> _selectEnum =
            new LogixNG_SelectEnum<>(this, SectionState.values(), SectionState.Free, this);

    public ExpressionSection(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExpressionSection copy = new ExpressionSection(sysName, userName);
        copy.setComment(getComment());

        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);

        copy.set_Is_IsNot(_is_IsNot);

        return manager.registerExpression(copy);
    }

    public LogixNG_SelectNamedBean<Section> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<SectionState> getSelectEnum() {
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

        Section section = _selectNamedBean.evaluateNamedBean(conditionalNG);

        if (section == null) return false;

        SectionState checkSectionState = _selectEnum.evaluateEnum(conditionalNG);

        int currentState = section.getState();

        if (_is_IsNot == Is_IsNot_Enum.Is) {
            return currentState == checkSectionState.getID();
        } else {
            return currentState != checkSectionState.getID();
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
        return Bundle.getMessage(locale, "Section_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state;

        if (_selectEnum.isDirectAddressing()) {
            SectionState sectionState = _selectEnum.getEnum();
            state = Bundle.getMessage(locale, "AddressByDirect", sectionState._text);
        } else {
            state = _selectEnum.getDescription(locale);
        }

        return Bundle.getMessage(locale, "Section_Long", namedBean, _is_IsNot.toString(), state);
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

    public enum SectionState {
        Free(Section.FREE, Bundle.getMessage("Section_StateFree")),
        Forward(Section.FORWARD, Bundle.getMessage("Section_StateForward")),
        Reverse(Section.REVERSE, Bundle.getMessage("Section_StateReverse"));

        private final int _id;
        private final String _text;

        private SectionState(int id, String text) {
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
        log.debug("getUsageReport :: ExpressionSection: bean = {}, report = {}", cdl, report);
        _selectNamedBean.getUsageDetail(level, bean, report, cdl, this, LogixNG_SelectNamedBean.Type.Expression);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExpressionSection.class);

}
