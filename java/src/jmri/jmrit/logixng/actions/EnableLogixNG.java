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
 * This action enables/disables a LogixNG.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class EnableLogixNG extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectNamedBean<LogixNG> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, LogixNG.class, InstanceManager.getDefault(LogixNG_Manager.class), this);

    private final LogixNG_SelectEnum<Operation> _selectEnum =
            new LogixNG_SelectEnum<>(this, Operation.values(), Operation.Disable, this);


    public EnableLogixNG(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        EnableLogixNG copy = new EnableLogixNG(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        _selectEnum.copy(copy._selectEnum);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<LogixNG> getSelectNamedBean() {
        return _selectNamedBean;
    }

    public LogixNG_SelectEnum<Operation> getSelectEnum() {
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
        LogixNG logixNG = _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (logixNG == null) return;

        Operation operation = _selectEnum.evaluateEnum(getConditionalNG());

        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            operation.runTask(logixNG);
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
        return Bundle.getMessage(locale, "EnableLogixNG_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String namedBean = _selectNamedBean.getDescription(locale);
        String state = _selectEnum.getDescription(locale);

        return Bundle.getMessage(locale, "EnableLogixNG_Long", namedBean, state);
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


    private interface LogixNG_Task {
        void run(LogixNG logixNG);
    }


    public enum Operation {
        Enable(Bundle.getMessage("EnableLogixNG_Enable"), (lng) -> {lng.setEnabled(true);}),
        Disable(Bundle.getMessage("EnableLogixNG_Disable"), (lng) -> {lng.setEnabled(false);}),
        Activate(Bundle.getMessage("EnableLogixNG_Activate"), (lng) -> {lng.setActive(true);}),
        Deactivate(Bundle.getMessage("EnableLogixNG_Deactivate"), (lng) -> {lng.setActive(false);});

        private final String _text;
        private final LogixNG_Task _task;

        private Operation(String text, LogixNG_Task task) {
            this._text = text;
            this._task = task;
        }

        public void runTask(LogixNG logixNG) {
            _task.run(logixNG);
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnableLogix.class);

}
