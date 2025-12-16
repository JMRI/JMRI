package jmri.jmrit.logixng.actions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectString;

/**
 * Returns from a Module or a ConditionalNG.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class Error extends AbstractDigitalAction
        implements PropertyChangeListener {

    private final LogixNG_SelectString _selectMessage =
            new LogixNG_SelectString(this, this);

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
        getSelectMessage().copy(copy._selectMessage);
        return manager.registerAction(copy);
    }

    public LogixNG_SelectString getSelectMessage() {
        return _selectMessage;
    }

    /** {@inheritDoc} */
    @Override
    public LogixNG_Category getCategory() {
        return LogixNG_Category.FLOW_CONTROL;
    }

    /** {@inheritDoc} */
    @Override
    public void execute() throws JmriException {
        String message = _selectMessage.evaluateValue(getConditionalNG());
        throw new JmriException(message);
    }

    @Override
    public String getShortDescription(Locale locale) {
        return Bundle.getMessage(locale, "Error_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "Error_Long",
                _selectMessage.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WebBrowser.class);

}
