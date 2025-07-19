package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;
import jmri.util.JUnitUtil.ReleaseUntil;

/**
 * This action waits for a condition to be true. It is used for tests.
 *
 * @author Daniel Bergqvist Copyright 2024
 */
public class WaitForScaffold extends AbstractDigitalAction {

    private ReleaseUntil _condition;

    public WaitForScaffold(ReleaseUntil condition)
            throws BadUserNameException {
        super(InstanceManager.getDefault(DigitalActionManager.class).getAutoSystemName(), null);
        _condition = condition;
    }

    public WaitForScaffold(String sys, String user, ReleaseUntil condition)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
        _condition = condition;
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        WaitForScaffold copy = new WaitForScaffold(sysName, userName, _condition);
        copy.setComment(getComment());
        return manager.registerAction(copy);
    }

    public void setReleaseCondition(ReleaseUntil condition) {
        _condition = condition;
    }

    public ReleaseUntil getReleaseCondition() {
        return _condition;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        if (!JUnitUtil.waitFor(_condition)) {
            throw new JmriException("WaitFor condition didn't returned true in time");
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
        return "Wait for";
    }

    @Override
    public String getLongDescription(Locale locale) {
        return "Wait for";
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
    }

    /** {@inheritDoc} */
    @Override
    public void registerListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void unregisterListenersForThisClass() {
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }

}
