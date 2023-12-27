package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;

import jmri.*;
import jmri.jmrit.entryexit.DestinationPoints;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.*;
import jmri.jmrit.logixng.util.parser.*;
import jmri.util.ThreadingUtil;

/**
 * This action triggers a entryExit.
 * <p>
 * This action has the Operation enum, similar to EnableLogix and other actions,
 * despite that's not needed since this action only has one option. But it's
 * here in case someone wants to add more options later.
 *
 * @author Daniel Bergqvist Copyright 2021
 */
public class ActionEntryExit extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<DestinationPoints> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, DestinationPoints.class, InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class), this);

    private final LogixNG_SelectEnum<Operation> _selectEnum =
            new LogixNG_SelectEnum<>(this, Operation.values(), Operation.SetNXPairEnabled, this);


    public ActionEntryExit(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ActionEntryExit copy = new ActionEntryExit(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<DestinationPoints> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<Operation> getSelectEnum() {
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
        DestinationPoints entryExit = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (entryExit == null) return;

        Operation oper = _selectEnum.evaluateEnum(getConditionalNG());

        // Variables used in lambda must be effectively final
        Operation theOper = oper;

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            switch (theOper) {
                case SetNXPairEnabled:
                    entryExit.setEnabled(true);
                    break;
                case SetNXPairDisabled:
                    entryExit.setEnabled(false);
                    break;
                case SetNXPairSegment:
                    jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                            setSingleSegmentRoute(entryExit.getSystemName());
                    break;
                case SetNXPairInactive:
                    if (entryExit.isActive()) {
                        jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                                setSingleSegmentRoute(entryExit.getSystemName());
                    }
                    break;
                case SetNXPairActive:
                    if (!entryExit.isActive()) {
                        jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                                setSingleSegmentRoute(entryExit.getSystemName());
                    }
                    break;
                case SetNXPairReversed:
                    if (entryExit.isUniDirection()) {
                        throw new IllegalArgumentException("\"" + entryExit.getDisplayName() +
                                "\" is not enabled for reversed activation (Both Way)");
                    }

                    if (!entryExit.isActive()) {
                        jmri.InstanceManager.getDefault(jmri.jmrit.entryexit.EntryExitPairs.class).
                                setReversedRoute(entryExit.getSystemName());
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
        return Bundle.getMessage(locale, "ActionEntryExit_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        return Bundle.getMessage(locale, "ActionEntryExit_Long", namedBean, state);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        getSelectNamedBean().setup();
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


    public enum Operation {
        SetNXPairEnabled(Bundle.getMessage("ActionEntryExit_SetNXPairEnabled")),
        SetNXPairDisabled(Bundle.getMessage("ActionEntryExit_SetNXPairDisabled")),
        SetNXPairSegment(Bundle.getMessage("ActionEntryExit_SetNXPairSegment")),
        Separator1(Base.SEPARATOR),
        SetNXPairInactive(Bundle.getMessage("ActionEntryExit_SetNXPairInactive")),
        SetNXPairActive(Bundle.getMessage("ActionEntryExit_SetNXPairActive")),
        SetNXPairReversed(Bundle.getMessage("ActionEntryExit_SetNXPairReversed"));

        private final String _text;

        private Operation(String text) {
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ActionEntryExit.class);

}
