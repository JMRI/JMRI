package jmri.jmrit.logixng.actions;

import java.beans.*;
import java.util.*;

import jmri.*;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectNamedBean;
import jmri.jmrit.logixng.util.parser.ParserException;

/**
 * Executes a digital action.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class ExecuteAction extends AbstractDigitalAction
        implements PropertyChangeListener, VetoableChangeListener {

    private final LogixNG_SelectNamedBean<MaleDigitalActionSocket> _selectNamedBean =
            new LogixNG_SelectNamedBean<>(
                    this, MaleDigitalActionSocket.class, InstanceManager.getDefault(DigitalActionManager.class), this);

    public ExecuteAction(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _selectNamedBean.setOnlyDirectAddressingAllowed();
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ExecuteAction copy = new ExecuteAction(sysName, userName);
        copy.setComment(getComment());
        _selectNamedBean.copy(copy._selectNamedBean);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectNamedBean<MaleDigitalActionSocket> getSelectNamedBean() {
        return _selectNamedBean;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        MaleDigitalActionSocket action =
                _selectNamedBean.evaluateNamedBean(getConditionalNG());

        if (action == null) {
            log.error("The action is null");
            return;
        }

        if (getConditionalNG() != action.getConditionalNG()) {
            // If the action is not in the same ConditionalNG, the action
            // might be executed concurrent by the other ConditionalNG if
            // the two ConditionalNGs executes in two different threads.
            // And LogixNG actions/expressions are not designed to handle
            // that.
            log.error("The action is not in the same ConditionalNG as this action.");
            return;
        }

        action.execute();
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
        return Bundle.getMessage(locale, "ExecuteAction_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        String beanName = _selectNamedBean.getDescription(locale);

        return Bundle.getMessage(locale, "ExecuteAction_Long",
                beanName);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        _selectNamedBean.setup();
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
        if (!_listenersAreRegistered) {
            _selectNamedBean.addPropertyChangeListener(PROPERTY_LAST_RESULT_CHANGED, this);
            _selectNamedBean.registerListeners();
            _listenersAreRegistered = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
        if (_listenersAreRegistered) {
            _selectNamedBean.removePropertyChangeListener(PROPERTY_LAST_RESULT_CHANGED, this);
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ExecuteAction.class);

}
