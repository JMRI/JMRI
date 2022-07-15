package jmri.jmrit.logixng.expressions;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.util.parser.ParserException;
import jmri.jmrix.*;
import static jmri.jmrix.JmrixConfigPane.NONE_SELECTED;

/**
 * Returns true if there is a connection of specified type.
 *
 * @author Daniel Bergqvist Copyright 2022
 */
public class ConnectionName extends AbstractDigitalExpression
        implements PropertyChangeListener {

    private String _manufacturer = NONE_SELECTED;
    private String _connectionName = NONE_SELECTED;


    public ConnectionName(String sys, String user)
            throws BadUserNameException, BadSystemNameException {
        super(sys, user);
    }

    @Override
    public Base getDeepCopy(Map<String, String> systemNames, Map<String, String> userNames) throws ParserException {
        DigitalExpressionManager manager = InstanceManager.getDefault(DigitalExpressionManager.class);
        String sysName = systemNames.get(getSystemName());
        String userName = userNames.get(getSystemName());
        if (sysName == null) sysName = manager.getAutoSystemName();
        ConnectionName copy = new ConnectionName(sysName, userName);
        copy.setComment(getComment());
        copy._manufacturer = _manufacturer;
        copy._connectionName = _connectionName;
        return manager.registerExpression(copy);
    }

    public void setManufacturer(String manufacturer) {
        _manufacturer = manufacturer;
    }

    public String getManufacturer() {
        return _manufacturer;
    }

    public void setConnectionName(String name) {
        _connectionName = name;
    }

    public String getConnectionName() {
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
        ConnectionConfigManager manager = InstanceManager.getDefault(ConnectionConfigManager.class);
        for (ConnectionConfig cc : manager.getConnections()) {
            if (cc.getManufacturer().equals(_manufacturer) && cc.name().equals(_connectionName)) {
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
        return Bundle.getMessage(locale, "ConnectionName_Long", _manufacturer, _connectionName);
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
