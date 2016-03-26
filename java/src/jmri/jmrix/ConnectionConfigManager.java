package jmri.jmrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import jmri.InstanceManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrix.internal.InternalConnectionTypeList;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesProvider;
import jmri.util.jdom.JDOMUtil;
import jmri.util.prefs.AbstractPreferencesProvider;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for ConnectionConfig objects.
 *
 * @author Randall Wood (C) 2015
 */
public class ConnectionConfigManager extends AbstractPreferencesProvider implements Iterable<ConnectionConfig> {

    private final ArrayList<ConnectionConfig> connections = new ArrayList<>();
    private final String NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/connections-2-9-6.xsd";
    private final String CONNECTIONS = "connections";
    public final static String CONNECTION = "connection";
    public final static String CLASS = "class";
    public final static String USER_NAME = "userName";
    public final static String SYSTEM_NAME = "systemPrefix";
    public final static String MANUFACTURER = "manufacturer";
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigManager.class);

    @Override
    public void initialize(Profile profile) {
        if (!isInitialized(profile)) {
            log.debug("Initializing...");
            Element sharedConnections = null;
            Element perNodeConnections = null;
            try {
                sharedConnections = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(CONNECTIONS, NAMESPACE, true));
            } catch (NullPointerException ex) {
                // Normal if this is a new profile
                log.info("No connections configured.");
                log.debug("Null pointer thrown reading shared configuration.", ex);
            }
            if (sharedConnections != null) {
                try {
                    perNodeConnections = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(CONNECTIONS, NAMESPACE, false));
                } catch (NullPointerException ex) {
                    // Normal if the profile has not been used on this computer
                    log.info("No local configuration found.");
                    log.debug("Null pointer thrown reading local configuration.", ex);
                    // TODO: notify user
                }
                for (Element shared : sharedConnections.getChildren(CONNECTION)) {
                    Element perNode = shared;
                    String className = shared.getAttributeValue(CLASS);
                    String userName = shared.getAttributeValue(USER_NAME, "");
                    String systemName = shared.getAttributeValue(SYSTEM_NAME, "");
                    String manufacturer = shared.getAttributeValue(MANUFACTURER, "");
                    log.debug("Read shared connection {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                    if (perNodeConnections != null) {
                        for (Element e : perNodeConnections.getChildren(CONNECTION)) {
                            if (systemName.equals(e.getAttributeValue(SYSTEM_NAME))) {
                                perNode = e;
                                className = perNode.getAttributeValue(CLASS);
                                userName = perNode.getAttributeValue(USER_NAME, "");
                                manufacturer = perNode.getAttributeValue(MANUFACTURER, "");
                                log.debug("Read perNode connection {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                            }
                        }
                    }
                    try {
                        log.debug("Creating connection {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                        ((XmlAdapter) Class.forName(className).newInstance()).load(shared, perNode);
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                        log.error("Unable to create {} for {}", className, shared, ex);
                    } catch (Exception ex) {
                        log.error("Unable to load {} into {}", shared, className, ex);
                    }
                }
            }
            setIsInitialized(profile, true);
            log.debug("Initialized...");
        }
    }

    @Override
    public Set<Class<? extends PreferencesProvider>> getRequires() {
        return new HashSet<>();
    }

    @Override
    public void savePreferences(Profile profile) {
        log.debug("Saving connections preferences...");
        // store shared Connection preferences
        savePreferences(profile, true);
        // store private or perNode Connection preferences
        savePreferences(profile, false);
        log.debug("Saved connections preferences...");
    }

    private synchronized void savePreferences(Profile profile, boolean shared) {
        Element element = new Element(CONNECTIONS, NAMESPACE);
        for (ConnectionConfig o : connections) {
            log.debug("Saving connection {} ({})...", o.getConnectionName(), shared);
            Element e = ConfigXmlManager.elementFromObject(o, shared);
            if (e != null) {
                element.addContent(e);
            }
        }
        // save connections, or save an empty connections element if user removed all connections
        try {
            ProfileUtils.getAuxiliaryConfiguration(profile).putConfigurationFragment(JDOMUtil.toW3CElement(element), shared);
        } catch (JDOMException ex) {
            log.error("Unable to create create XML", ex);
        }
    }

    public boolean add(ConnectionConfig c) {
        boolean result = connections.add(c);
        int i = connections.indexOf(c);
        propertyChangeSupport.fireIndexedPropertyChange(CONNECTIONS, i, null, c);
        return result;
    }

    public boolean remove(ConnectionConfig c) {
        int i = connections.indexOf(c);
        boolean result = connections.remove(c);
        propertyChangeSupport.fireIndexedPropertyChange(CONNECTIONS, i, c, null);
        return result;
    }

    public ConnectionConfig[] getConnections() {
        return connections.toArray(new ConnectionConfig[connections.size()]);
    }

    public ConnectionConfig getConnections(int index) {
        return connections.get(index);
    }

    @Override
    public Iterator<ConnectionConfig> iterator() {
        return connections.iterator();
    }

    public String[] getConnectionTypes(String manufacturer) {
        if (InstanceManager.getDefault(ConnectionTypeManager.class) == null) {
            InstanceManager.setDefault(ConnectionTypeManager.class, new ConnectionTypeManager());
        }
        return InstanceManager.getDefault(ConnectionTypeManager.class).getConnectionTypes(manufacturer);
    }

    public String[] getConnectionManufacturers() {
        if (InstanceManager.getDefault(ConnectionTypeManager.class) == null) {
            InstanceManager.setDefault(ConnectionTypeManager.class, new ConnectionTypeManager());
        }
        return InstanceManager.getDefault(ConnectionTypeManager.class).getConnectionManufacturers();
    }

    private static class ConnectionTypeManager {

        private final HashMap<String, ConnectionTypeList> connectionTypeLists = new HashMap<>();

        public ConnectionTypeManager() {
            for (ConnectionTypeList ctl : ServiceLoader.load(ConnectionTypeList.class)) {
                for (String manufacturer : ctl.getManufacturers()) {
                    if (!connectionTypeLists.containsKey(manufacturer)) {
                        connectionTypeLists.put(manufacturer, ctl);
                    } else {
                        log.error("Refusing to add ConnectionListType \"{}\" for manufacturer \"{}\"; existing class is \"{}\"",
                                ctl.getClass().getName(),
                                manufacturer,
                                connectionTypeLists.get(manufacturer).getClass().getName());
                    }
                }
            }
        }

        public String[] getConnectionTypes(String manufacturer) {
            ConnectionTypeList ctl = this.connectionTypeLists.get(manufacturer);
            if (ctl != null) {
                return ctl.getAvailableProtocolClasses();
            }
            return this.connectionTypeLists.get(InternalConnectionTypeList.NONE).getAvailableProtocolClasses();
        }

        public String[] getConnectionManufacturers() {
            ArrayList<String> a = new ArrayList<>(this.connectionTypeLists.keySet());
            a.remove(InternalConnectionTypeList.NONE);
            a.sort(null);
            a.add(0, InternalConnectionTypeList.NONE);
            return a.toArray(new String[a.size()]);
        }

    }
}
