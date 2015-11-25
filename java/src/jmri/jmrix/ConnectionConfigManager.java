package jmri.jmrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.XmlAdapter;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.AbstractPreferencesProvider;
import jmri.spi.PreferencesProvider;
import jmri.util.jdom.JDOMUtil;
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
        if (!this.isInitialized(profile)) {
            log.debug("Initializing...");
            try {
            
                Element sharedConnections = null;
                
                if (ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(CONNECTIONS, NAMESPACE, true) != null) {
                    sharedConnections = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(CONNECTIONS, NAMESPACE, true));
                } else {
                    log.error("ConnectionConfigManager currently doesn't cleanly handle the case of no connections; can this really be normal?");
                }
                
                Element perNodeConnections = null;

                try {
                    perNodeConnections = JDOMUtil.toJDOMElement(ProfileUtils.getAuxiliaryConfiguration(profile).getConfigurationFragment(CONNECTIONS, NAMESPACE, false));
                } catch (NullPointerException ex) {
                    log.error("Null pointer while trying to find local configuration; can this really be normal?");
                }
                if (sharedConnections != null) {
                    for (Element shared : sharedConnections.getChildren(CONNECTION)) {
                        Element perNode = null;
                        String className = shared.getAttributeValue(CLASS);
                        String userName = shared.getAttributeValue(USER_NAME, "");
                        String systemName = shared.getAttributeValue(SYSTEM_NAME, "");
                        String manufacturer = shared.getAttributeValue(MANUFACTURER, "");
                        log.debug("Read {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                        if (perNodeConnections != null) {
                            for (Element e : perNodeConnections.getChildren(CONNECTION)) {
                                if (systemName.equals(e.getAttributeValue(SYSTEM_NAME))) {
                                    perNode = e;
                                }
                            }
                        }
                        try {
                            log.debug("Creating {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                            ((XmlAdapter) Class.forName(className).newInstance()).load(shared, perNode);
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
                            log.error("Unable to create {} for {}", className, shared, ex);
                        } catch (Exception ex) {
                            log.error("Unable to load {} into {}", shared, className, ex);
                        }
                    }
                }
            } catch (NullPointerException ex) {
                log.error("Null pointer while trying to read configuration.", ex);
            }
            this.setIsInitialized(profile, true);
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
        this.savePreferences(profile, true);
        // store private or perNode Connection preferences
        this.savePreferences(profile, false);
        log.debug("Saved connections preferences...");
    }

    private synchronized void savePreferences(Profile profile, boolean shared) {
        Element element = new Element(CONNECTIONS, NAMESPACE);
        log.debug("connections is {}null", (connections != null) ? "not " : "");
        for (ConnectionConfig o : connections) {
            log.debug("Saving connection {} ({})...", o.getConnectionName(), shared);
            Element e = ConfigXmlManager.elementFromObject(o, shared);
            if (e != null) {
                element.addContent(e);
            }
        }
        // if there are no <connection> Elements in the <connections> Element, skip storing
        if (element.getChildren("connection").size() > 0) {
            try {
                ProfileUtils.getAuxiliaryConfiguration(profile).putConfigurationFragment(JDOMUtil.toW3CElement(element), shared);
            } catch (JDOMException ex) {
                log.error("Unable to create create XML", ex);
            }
        } else {
            log.debug("No connections, skipping store of <connections>");
        }
    }

    public boolean add(ConnectionConfig c) {
        boolean result = this.connections.add(c);
        int i = this.connections.indexOf(c);
        this.propertyChangeSupport.fireIndexedPropertyChange(CONNECTIONS, i, null, c);
        return result;
    }

    public boolean remove(ConnectionConfig c) {
        int i = this.connections.indexOf(c);
        boolean result = this.connections.remove(c);
        this.propertyChangeSupport.fireIndexedPropertyChange(CONNECTIONS, i, c, null);
        return result;
    }

    public ConnectionConfig[] getConnections() {
        return this.connections.toArray(new ConnectionConfig[this.connections.size()]);
    }

    public ConnectionConfig getConnections(int index) {
        return this.connections.get(index);
    }

    @Override
    public Iterator<ConnectionConfig> iterator() {
        return this.connections.iterator();
    }
}
