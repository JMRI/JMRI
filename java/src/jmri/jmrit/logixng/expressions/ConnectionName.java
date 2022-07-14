package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.LogixNG_SelectString;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrix.*;

/**
 * Returns true if there is a connection of specified type.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ConnectionName extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private final LogixNG_SelectString _connectionName =
            new LogixNG_SelectString(this, this);


    public ConnectionName(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);

        _connectionName.setValue("LocoNet Simulator");
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ConnectionName copy = new ConnectionName(sysName, userName);
        copy.setComment(getComment());
        _connectionName.copy(copy._connectionName);
        return manager.registerExpression(copy);
    }

    public LogixNG_SelectString getSelectConnectionName() {
        return _connectionName;
    }

    /** {@inheritDoc} */
    @Override
    public Category getCategory() {
        return Category.OTHER;
    }

    /** {@inheritDoc} */
    @Override
    public boolean evaluate() throws JmriException {
        String connectionName = _connectionName.evaluateValue(getConditionalNG());
        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);
        for (ConnectionConfig cc : manager.getConnections()) {
            if (cc.name().equals(connectionName)) {
                return true;
            }
        }
        return false;
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
        return Bundle.getMessage(locale, "ConnectionName_Short");
    }

    @Override
    public String getLongDescription(Locale locale) {
        return Bundle.getMessage(locale, "ConnectionName_Long", _connectionName.getDescription(locale));
    }

    /** {@inheritDoc} */
    @Override
    public void setup() {
        // Do nothing
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
    public void propertyChange(PropertyChangeEvent evt) {
        getConditionalNG().execute();
    }

    /** {@inheritDoc} */
    @Override
    public void disposeMe() {
    }


//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionName.class);
}
