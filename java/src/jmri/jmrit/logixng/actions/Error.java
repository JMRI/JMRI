package jmri.jmrit.logixng.actions;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;

/**
 * Returns from a Module or a ConditionalNG.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class Error extends AbstractDigitalAction {

    private String _message = "";

    public Error(String sys, String user) {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws JmriException {
        DigitalActionManager manager = InstanceManager.getDefault(DigitalActionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        Error copy = new Error(sysName, userName);
        copy.setComment(getComment());
        copy.setMessage(_message);
        return manager.registerAction(copy);
    }

    public void setMessage(String message) {
        _message = message;
    }

    public String getMessage() {
        return _message;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.FLOW_CONTROL;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        throw new JmriException(_message);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Error_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Error_Long", _message);
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebBrowser.class);

}
