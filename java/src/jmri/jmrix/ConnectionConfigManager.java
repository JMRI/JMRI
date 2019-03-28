package jmri.jmrix;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.configurexml.ClassMigrationManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.configurexml.ErrorHandler;
import jmri.configurexml.ErrorMemo;
import jmri.configurexml.XmlAdapter;
import jmri.jmrix.internal.InternalConnectionTypeList;
import jmri.profile.Profile;
import jmri.profile.ProfileUtils;
import jmri.spi.PreferencesManager;
import jmri.util.jdom.JDOMUtil;
import jmri.util.prefs.AbstractPreferencesManager;
import jmri.util.prefs.HasConnectionButUnableToConnectException;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for ConnectionConfig objects.
 *
 * @author Randall Wood (C) 2015
 */
@ServiceProvider(service = PreferencesManager.class)
public class ConnectionConfigManager extends AbstractPreferencesManager implements Iterable<ConnectionConfig> {

    private final ArrayList<ConnectionConfig> connections = new ArrayList<>();
    private final static String NAMESPACE = "http://jmri.org/xml/schema/auxiliary-configuration/connections-2-9-6.xsd"; // NOI18N
    public final static String CONNECTIONS = "connections"; // NOI18N
    public final static String CONNECTION = "connection"; // NOI18N
    public final static String CLASS = "class"; // NOI18N
    public final static String USER_NAME = "userName"; // NOI18N
    public final static String SYSTEM_NAME = "systemPrefix"; // NOI18N
    public final static String MANUFACTURER = "manufacturer"; // NOI18N
    private final static Logger log = LoggerFactory.getLogger(ConnectionConfigManager.class);

    @Override
    public void initialize(Profile profile) throws HasConnectionButUnableToConnectException {
        if (!isInitialized(profile)) {
            log.debug("Initializing...");
            Element sharedConnections = null;
            Element perNodeConnections = null;
            this.setPortNamePattern();
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
                    String userName = shared.getAttributeValue(USER_NAME, ""); // NOI18N
                    String systemName = shared.getAttributeValue(SYSTEM_NAME, ""); // NOI18N
                    String manufacturer = shared.getAttributeValue(MANUFACTURER, ""); // NOI18N
                    log.debug("Read shared connection {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                    if (perNodeConnections != null) {
                        for (Element e : perNodeConnections.getChildren(CONNECTION)) {
                            if (systemName.equals(e.getAttributeValue(SYSTEM_NAME))) {
                                perNode = e;
                                className = perNode.getAttributeValue(CLASS);
                                userName = perNode.getAttributeValue(USER_NAME, ""); // NOI18N
                                manufacturer = perNode.getAttributeValue(MANUFACTURER, ""); // NOI18N
                                log.debug("Read perNode connection {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                            }
                        }
                    }
                    String newClassName = InstanceManager.getDefault(ClassMigrationManager.class).getClassName(className);
                    if (!className.equals(newClassName)) {
                        log.info("Class {} will be used for connection {} instead of {} if preferences are saved", newClassName, userName, className);
                        className = newClassName;
                    }
                    try {
                        log.debug("Creating connection {}:{} ({}) class {}", userName, systemName, manufacturer, className);
                        XmlAdapter adapter = (XmlAdapter) Class.forName(className).getDeclaredConstructor().newInstance();
                        ConnectionConfigManagerErrorHandler handler = new ConnectionConfigManagerErrorHandler();
                        adapter.setExceptionHandler(handler);
                        if (!adapter.load(shared, perNode)) {
                            log.error("Unable to create {} for {}, load returned false", className, shared);
                            String english = Bundle.getMessage(Locale.ENGLISH, "ErrorSingleConnection", userName, systemName); // NOI18N
                            String localized = Bundle.getMessage("ErrorSingleConnection", userName, systemName); // NOI18N
                            this.addInitializationException(profile, new HasConnectionButUnableToConnectException(english, localized));
                        }
                        handler.exceptions.forEach((exception) -> {
                            this.addInitializationException(profile, exception);
                        });
                    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException | java.lang.reflect.InvocationTargetException ex) {
                        log.error("Unable to create {} for {}", className, shared, ex);
                        String english = Bundle.getMessage(Locale.ENGLISH, "ErrorSingleConnection", userName, systemName); // NOI18N
                        String localized = Bundle.getMessage("ErrorSingleConnection", userName, systemName); // NOI18N
                        this.addInitializationException(profile, new HasConnectionButUnableToConnectException(english, localized, ex));
                    } catch (RuntimeException | jmri.configurexml.JmriConfigureXmlException ex) {
                        log.error("Unable to load {} into {}", shared, className, ex);
                        String english = Bundle.getMessage(Locale.ENGLISH, "ErrorSingleConnection", userName, systemName); // NOI18N
                        String localized = Bundle.getMessage("ErrorSingleConnection", userName, systemName); // NOI18N
                        this.addInitializationException(profile, new HasConnectionButUnableToConnectException(english, localized, ex));
                    }
                }
            }
            setInitialized(profile, true);
            List<Exception> exceptions = this.getInitializationExceptions(profile);
            if (exceptions.size() == 1) {
                if (exceptions.get(0) instanceof HasConnectionButUnableToConnectException) {
                    throw (HasConnectionButUnableToConnectException) exceptions.get(0);
                } else {
                    throw new HasConnectionButUnableToConnectException(exceptions.get(0));
                }
            } else if (exceptions.size() > 1) {
                String english = Bundle.getMessage(Locale.ENGLISH, "ErrorMultipleConnections"); // NOI18N
                String localized = Bundle.getMessage("ErrorMultipleConnections"); // NOI18N
                throw new HasConnectionButUnableToConnectException(english, localized);
            }
            log.debug("Initialized...");
        }
    }

    @Override
    public Set<Class<? extends PreferencesManager>> getRequires() {
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
        connections.stream().forEach((o) -> {
            log.debug("Saving connection {} ({})...", o.getConnectionName(), shared);
            Element e = ConfigXmlManager.elementFromObject(o, shared);
            if (e != null) {
                element.addContent(e);
            }
        });
        // save connections, or save an empty connections element if user removed all connections
        try {
            ProfileUtils.getAuxiliaryConfiguration(profile).putConfigurationFragment(JDOMUtil.toW3CElement(element), shared);
        } catch (JDOMException ex) {
            log.error("Unable to create create XML", ex);
        }
    }

    /**
     * Add a {@link jmri.jmrix.ConnectionConfig} following the rules specified
     * in {@link java.util.Collection#add(java.lang.Object)}.
     *
     * @param c an existing ConnectionConfig
     * @return true if c was added, false otherwise
     * @throws NullPointerException if c is null
     */
    public boolean add(@Nonnull ConnectionConfig c) throws NullPointerException {
        if (c == null) {
            throw new NullPointerException();
        }
        if (!connections.contains(c)) {
            boolean result = connections.add(c);
            int i = connections.indexOf(c);
            fireIndexedPropertyChange(CONNECTIONS, i, null, c);
            return result;
        }
        return false;
    }

    /**
     * Remove a {@link jmri.jmrix.ConnectionConfig} following the rules
     * specified in {@link java.util.Collection#add(java.lang.Object)}.
     *
     * @param c an existing ConnectionConfig
     * @return true if c was removed, false otherwise
     */
    public boolean remove(@Nonnull ConnectionConfig c) {
        int i = connections.indexOf(c);
        boolean result = connections.remove(c);
        if (result) {
            fireIndexedPropertyChange(CONNECTIONS, i, c, null);
        }
        return result;
    }

    /**
     * Get an Array of {@link jmri.jmrix.ConnectionConfig} objects.
     *
     * @return an Array, possibly empty if there are no ConnectionConfig
     *         objects.
     */
    @Nonnull
    public ConnectionConfig[] getConnections() {
        return connections.toArray(new ConnectionConfig[connections.size()]);
    }

    /**
     * Get the {@link jmri.jmrix.ConnectionConfig} at index following the rules
     * specified in {@link java.util.Collection#add(java.lang.Object)}.
     *
     * @param index index of the ConnectionConfig to return
     * @return the ConnectionConfig at the specified location
     */
    public ConnectionConfig getConnections(int index) {
        return connections.get(index);
    }

    @Override
    public Iterator<ConnectionConfig> iterator() {
        return connections.iterator();
    }

    /**
     * Get the class names for classes supporting layout connections for the
     * given manufacturer.
     *
     * @param manufacturer the name of the manufacturer
     * @return An array of supporting class names; will return the list of
     *         internal connection classes if manufacturer is not a known
     *         manufacturer; the array may be empty if there are no supporting
     *         classes for the given manufacturer.
     */
    @Nonnull
    public String[] getConnectionTypes(@Nonnull String manufacturer) {
        return this.getDefaultConnectionTypeManager().getConnectionTypes(manufacturer);
    }

    /**
     * Get the list of known manufacturers.
     *
     * @return An array of known manufacturers.
     */
    @Nonnull
    public String[] getConnectionManufacturers() {
        return this.getDefaultConnectionTypeManager().getConnectionManufacturers();
    }

    /**
     * Get the manufacturer that is supported by a connection type. If there are
     * multiple manufacturers supported by connectionType, returns only the
     * first manufacturer.
     *
     * @param connectionType the class name of a connection type.
     * @return the supported manufacturer. Returns null if no manufacturer is
     *         associated with the connectionType.
     */
    @CheckForNull
    public String getConnectionManufacturer(@Nonnull String connectionType) {
        for (String manufacturer : this.getConnectionManufacturers()) {
            for (String manufacturerType : this.getConnectionTypes(manufacturer)) {
                if (connectionType.equals(manufacturerType)) {
                    return manufacturer;
                }
            }
        }
        return null;
    }

    /**
     * Get the list of all known manufacturers that a single connection type
     * supports.
     *
     * @param connectionType the class name of a connection type.
     * @return an Array of supported manufacturers. Returns an empty Array if no
     *         manufacturer is associated with the connectionType.
     */
    @Nonnull
    public String[] getConnectionManufacturers(@Nonnull String connectionType) {
        ArrayList<String> manufacturers = new ArrayList<>();
        for (String manufacturer : this.getConnectionManufacturers()) {
            for (String manufacturerType : this.getConnectionTypes(manufacturer)) {
                if (connectionType.equals(manufacturerType)) {
                    manufacturers.add(manufacturer);
                }
            }
        }
        return manufacturers.toArray(new String[manufacturers.size()]);
    }

    /**
     * Get the default {@link ConnectionTypeManager}, creating it if needed.
     *
     * @return the default ConnectionTypeManager
     */
    private ConnectionTypeManager getDefaultConnectionTypeManager() {
        if (InstanceManager.getNullableDefault(ConnectionTypeManager.class) == null) {
            InstanceManager.setDefault(ConnectionTypeManager.class, new ConnectionTypeManager());
        }
        return InstanceManager.getDefault(ConnectionTypeManager.class);
    }

    private static class ConnectionTypeManager {

        private final HashMap<String, ConnectionTypeList> connectionTypeLists = new HashMap<>();

        public ConnectionTypeManager() {
            for (ConnectionTypeList ctl : ServiceLoader.load(ConnectionTypeList.class)) {
                for (String manufacturer : ctl.getManufacturers()) {
                    if (!connectionTypeLists.containsKey(manufacturer)) {
                        connectionTypeLists.put(manufacturer, ctl);
                    } else {
                        log.debug("Need a proxy for {} from {} in {}", manufacturer, ctl.getClass().getName(), this);
                        ProxyConnectionTypeList proxy;
                        ConnectionTypeList existing = connectionTypeLists.get(manufacturer);
                        if (existing instanceof ProxyConnectionTypeList) {
                            proxy = (ProxyConnectionTypeList) existing;
                        } else {
                            proxy = new ProxyConnectionTypeList(existing);
                        }
                        proxy.add(ctl);
                        connectionTypeLists.put(manufacturer, proxy);
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

    private static class ProxyConnectionTypeList implements ConnectionTypeList {

        private final ArrayList<ConnectionTypeList> connectionTypeLists = new ArrayList<>();

        public ProxyConnectionTypeList(@Nonnull ConnectionTypeList connectionTypeList) {
            log.debug("Creating proxy for {}", connectionTypeList.getManufacturers()[0]);
            this.add(connectionTypeList);
        }

        public final void add(@Nonnull ConnectionTypeList connectionTypeList) {
            log.debug("Adding {} to proxy", connectionTypeList.getClass().getName());
            this.connectionTypeLists.add(connectionTypeList);
        }

        @Override
        public String[] getAvailableProtocolClasses() {
            TreeSet<String> classes = new TreeSet<>();
            this.connectionTypeLists.stream().forEach((connectionTypeList) -> {
                classes.addAll(Arrays.asList(connectionTypeList.getAvailableProtocolClasses()));
            });
            return classes.toArray(new String[classes.size()]);
        }

        @Override
        public String[] getManufacturers() {
            TreeSet<String> manufacturers = new TreeSet<>();
            this.connectionTypeLists.stream().forEach((connectionTypeList) -> {
                manufacturers.addAll(Arrays.asList(connectionTypeList.getManufacturers()));
            });
            return manufacturers.toArray(new String[manufacturers.size()]);
        }

    }

    /**
     * Override the default port name patterns unless the
     * purejavacomm.portnamepattern property was set on the command line.
     */
    private void setPortNamePattern() {
        final String pattern = "purejavacomm.portnamepattern";
        Properties properties = System.getProperties();
        if (properties.getProperty(pattern) == null) {
            try (InputStream in = ConnectionConfigManager.class.getResourceAsStream("PortNamePatterns.properties")) { // NOI18N
                properties.load(in);
            } catch (IOException ex) {
                log.error("Unable to read PortNamePatterns.properties", ex);
            }
        }
    }

    private static class ConnectionConfigManagerErrorHandler extends ErrorHandler {

        ArrayList<HasConnectionButUnableToConnectException> exceptions = new ArrayList<>();

        public ConnectionConfigManagerErrorHandler() {
            super();
        }

        /**
         * Capture ErrorMemos as initialization exceptions. {@inheritDoc}
         */
        @Override
        // The memo has a generic message (since the real cause never makes it this far anyway)
        // If the memo reliably had an exception, we could make a decision about
        // how to handle that, but since it doesn't all we can do is log it
        public void handle(ErrorMemo memo) {
            if (memo.exception != null) {
                this.exceptions.add(new HasConnectionButUnableToConnectException(memo.description, Bundle.getMessage("ErrorSubException", memo.description), memo.exception));
            } else {
                this.exceptions.add(new HasConnectionButUnableToConnectException(memo.description, Bundle.getMessage("ErrorSubException", memo.description) + memo.description));
            }
        }
    }
}
