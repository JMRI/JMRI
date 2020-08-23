package jmri.server.json.util;

import static jmri.server.json.JSON.CONTROL_PANEL;
import static jmri.server.json.JSON.LAYOUT_PANEL;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PANEL;
import static jmri.server.json.JSON.PANEL_PANEL;
import static jmri.server.json.JSON.SWITCHBOARD_PANEL;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.URL;
import static jmri.server.json.JSON.USERNAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Container;
import java.awt.Frame;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Metadata;
import jmri.server.json.JsonServerPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.SystemConnectionMemo;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;
import jmri.util.JmriJFrame;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceManager;
import jmri.web.server.WebServerPreferences;

/**
 * @author Randall Wood Copyright 2016, 2017, 2018
 */
public class JsonUtilHttpService extends JsonHttpService {

    private static final String RESOURCE_PATH = "jmri/server/json/util/";

    public JsonUtilHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    // use @CheckForNull to override @Nonnull specified in superclass
    public JsonNode doGet(String type, @CheckForNull String name, JsonNode data, JsonRequest request)
            throws JsonException {
        switch (type) {
            case JSON.HELLO:
                return this.getHello(
                        InstanceManager.getDefault(JsonServerPreferences.class).getHeartbeatInterval(), request);
            case JSON.METADATA:
                if (name == null) {
                    return this.getMetadata(request);
                }
                return this.getMetadata(request.locale, name, request.id);
            case JSON.NETWORK_SERVICE:
            case JSON.NETWORK_SERVICES:
                if (name == null) {
                    return this.getNetworkServices(request.locale, request.id);
                }
                return this.getNetworkService(name, request);
            case JSON.NODE:
                return this.getNode(request);
            case JSON.PANEL:
            case JSON.PANELS:
                if (name == null) {
                    return this.getPanels(request.id);
                }
                return this.getPanel(request.locale, name, request.id);
            case JSON.RAILROAD:
                return this.getRailroad(request);
            case JSON.SYSTEM_CONNECTION:
            case JSON.SYSTEM_CONNECTIONS:
                if (name == null) {
                    return this.getSystemConnections(request);
                }
                return this.getSystemConnection(name, request);
            case JSON.CONFIG_PROFILE:
            case JSON.CONFIG_PROFILES:
                if (name == null) {
                    return this.getConfigProfiles(request);
                }
                return this.getConfigProfile(name, request);
            case JSON.VERSION:
                return this.getVersion();
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        switch (type) {
            case JSON.METADATA:
                return this.getMetadata(request);
            case JSON.NETWORK_SERVICE:
            case JSON.NETWORK_SERVICES:
                return this.getNetworkServices(request);
            case JSON.PANEL:
            case JSON.PANELS:
                return this.getPanels(request.id);
            case JSON.SYSTEM_CONNECTION:
            case JSON.SYSTEM_CONNECTIONS:
                return this.getSystemConnections(request);
            case JSON.CONFIG_PROFILE:
            case JSON.CONFIG_PROFILES:
                return this.getConfigProfiles(request);
            default:
                ArrayNode array = this.mapper.createArrayNode();
                JsonNode node = this.doGet(type, null, data, request);
                if (node.isArray()) {
                    array.addAll((ArrayNode) node);
                } else {
                    array.add(node);
                }
                return array;
        }
    }

    @Override
    // Use @CheckForNull to override non-null requirement of superclass
    public JsonNode doPost(String type, @CheckForNull String name,
            JsonNode data, JsonRequest request) throws JsonException {
        return this.doGet(type, name, data, request);
    }

    /**
     * @return JSON map of complete versions and URL part for protocols
     * @throws JsonException if a protocol version is not available
     */
    public JsonNode getVersion() throws JsonException {
        ObjectNode data = mapper.createObjectNode();
        for (String version : JSON.VERSIONS) {
            try {
                Field field;
                field = JSON.class.getDeclaredField(version.toUpperCase() + "_PROTOCOL_VERSION");
                data.put(field.get(null).toString(), version);
            } catch (
                    IllegalAccessException |
                    IllegalArgumentException |
                    NoSuchFieldException |
                    SecurityException ex) {
                throw new JsonException(500, ex, 0);
            }
        }
        return message(JSON.VERSION, data, 0);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#HELLO} message.
     *
     * @param heartbeat seconds in which a client must send a message before its
     *                  connection is broken
     * @param request   the JSON request
     * @return the JSON hello message
     */
    public JsonNode getHello(int heartbeat, @Nonnull JsonRequest request) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.JMRI, jmri.Version.name());
        data.put(JSON.JSON, JSON.V5_PROTOCOL_VERSION);
        data.put(JSON.VERSION, JSON.V5);
        data.put(JSON.HEARTBEAT, Math.round(heartbeat * 0.9f));
        data.put(JSON.RAILROAD, InstanceManager.getDefault(WebServerPreferences.class).getRailroadName());
        data.put(JSON.NODE, NodeIdentity.networkIdentity());
        Profile activeProfile = ProfileManager.getDefault().getActiveProfile();
        data.put(JSON.ACTIVE_PROFILE, activeProfile != null ? activeProfile.getName() : null);
        return message(JSON.HELLO, data, request.id);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#HELLO} message.
     *
     * @param locale    the client's Locale
     * @param id        message id set by client
     * @param heartbeat seconds in which a client must send a message before its
     *                  connection is broken
     * @return the JSON hello message
     * @deprecated since 4.19.2; use {@link #getHello(int, JsonRequest)} instead
     */
    @Deprecated
    public JsonNode getHello(Locale locale, int heartbeat, int id) {
        return getHello(heartbeat, new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Get a JSON message with a metadata element from {@link jmri.Metadata}.
     *
     * @param name    The metadata element to get
     * @param request the JSON request
     * @return JSON metadata element
     * @throws JsonException if name is not a recognized metadata element
     */
    public JsonNode getMetadata(@Nonnull String name, @Nonnull JsonRequest request) throws JsonException {
        String metadata = Metadata.getBySystemName(name);
        ObjectNode data = mapper.createObjectNode();
        if (metadata != null) {
            data.put(JSON.NAME, name);
            data.put(JSON.VALUE, Metadata.getBySystemName(name));
        } else {
            throw new JsonException(404,
                    Bundle.getMessage(request.locale, JsonException.ERROR_OBJECT, JSON.METADATA, name),
                    request.id);
        }
        return message(JSON.METADATA, data, request.id);
    }

    /**
     * Get a JSON message with a metadata element from {@link jmri.Metadata}.
     *
     * @param locale The client's Locale.
     * @param name   The metadata element to get.
     * @param id     message id set by client
     * @return JSON metadata element.
     * @throws JsonException if name is not a recognized metadata element.
     */
    public JsonNode getMetadata(Locale locale, String name, int id) throws JsonException {
        return getMetadata(name, new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Get a JSON array of metadata elements as listed by
     * {@link jmri.Metadata#getSystemNameList()}.
     *
     * @param request the JSON request
     * @return Array of JSON metadata elements
     * @throws JsonException if thrown by
     *                       {@link #getMetadata(java.util.Locale, java.lang.String, int)}
     */
    public ArrayNode getMetadata(@Nonnull JsonRequest request) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : Metadata.getSystemNameList()) {
            root.add(getMetadata(name, request));
        }
        return root;
    }

    /**
     * Get a JSON array of metadata elements as listed by
     * {@link jmri.Metadata#getSystemNameList()}.
     *
     * @param locale The client's Locale
     * @param id     message id set by client
     * @return Array of JSON metadata elements
     * @throws JsonException if thrown by
     *                       {@link #getMetadata(java.util.Locale, java.lang.String, int)}
     * @deprecated since 4.19.2; use {@link #getMetadata(JsonRequest)} instead
     */
    @Deprecated
    public ArrayNode getMetadata(Locale locale, int id) throws JsonException {
        return getMetadata(new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Get a running {@link jmri.util.zeroconf.ZeroConfService} using the
     * protocol as the name of the service.
     *
     * @param name    the service protocol
     * @param request the JSON request
     * @return the JSON networkService message
     * @throws JsonException if type is not a running zeroconf networking
     *                       protocol
     */
    public JsonNode getNetworkService(@Nonnull String name, @Nonnull JsonRequest request) throws JsonException {
        for (ZeroConfService service : InstanceManager.getDefault(ZeroConfServiceManager.class).allServices()) {
            if (service.getType().equals(name)) {
                return this.getNetworkService(service, request.id);
            }
        }
        throw new JsonException(404,
                Bundle.getMessage(request.locale, JsonException.ERROR_OBJECT, JSON.NETWORK_SERVICE, name),
                request.id);
    }

    /**
     * Get a running {@link jmri.util.zeroconf.ZeroConfService} using the
     * protocol as the name of the service.
     *
     * @param locale the client's Locale
     * @param name   the service protocol
     * @param id     message id set by client
     * @return the JSON networkService message
     * @throws JsonException if type is not a running zeroconf networking
     *                       protocol
     * @deprecated since 4.19.2; use
     *             {@link #getNetworkService(String, JsonRequest)} instead
     */
    @Deprecated
    public JsonNode getNetworkService(Locale locale, String name, int id) throws JsonException {
        return getNetworkService(name, new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    private JsonNode getNetworkService(ZeroConfService service, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NAME, service.getType());
        data.put(JSON.USERNAME, service.getName());
        data.put(JSON.PORT, service.getServiceInfo().getPort());
        data.put(JSON.TYPE, service.getType());
        Enumeration<String> pe = service.getServiceInfo().getPropertyNames();
        while (pe.hasMoreElements()) {
            String pn = pe.nextElement();
            data.put(pn, service.getServiceInfo().getPropertyString(pn));
        }
        return message(JSON.NETWORK_SERVICE, data, id);
    }

    /**
     * @param request the JSON request
     * @return the JSON networkServices message.
     */
    public ArrayNode getNetworkServices(@Nonnull JsonRequest request) {
        ArrayNode root = mapper.createArrayNode();
        InstanceManager.getDefault(ZeroConfServiceManager.class).allServices().stream()
                .forEach(service -> root.add(this.getNetworkService(service, request.id)));
        return root;
    }

    /**
     * @param locale the client's Locale.
     * @param id     message id set by client
     * @return the JSON networkServices message.
     */
    public ArrayNode getNetworkServices(Locale locale, int id) {
        return getNetworkServices(new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * JMRI node identity and former identities.
     *
     * @param request the JSON request
     * @return the JSON node message
     * @see jmri.util.node.NodeIdentity
     */
    public JsonNode getNode(JsonRequest request) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NODE, NodeIdentity.networkIdentity());
        ArrayNode nodes = mapper.createArrayNode();
        NodeIdentity.formerIdentities().stream().forEach(nodes::add);
        data.set(JSON.FORMER_NODES, nodes);
        return message(JSON.NODE, data, request.id);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * JMRI node identity and former identities.
     *
     * @param locale the client's Locale
     * @param id     message id set by client
     * @return the JSON node message
     * @see jmri.util.node.NodeIdentity
     * @deprecated since 4.19.2; use {@link #getNode(JsonRequest)} instead
     */
    @Deprecated
    public JsonNode getNode(Locale locale, int id) {
        return getNode(new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * return a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * requested panel details
     * 
     * @param locale the client's Locale
     * @param name   panel name to return
     * @param id     message id set by client
     * @return the JSON panel message.
     * @throws JsonException if panel not found
     */
    public JsonNode getPanel(Locale locale, String name, int id) throws JsonException {
        ArrayNode panels = getPanels(JSON.XML, id);
        for (JsonNode panel : panels) {
            if (panel.path(JSON.DATA).path(JSON.NAME).asText().equals(name)) {
                return message(JSON.PANEL, panel.path(JSON.DATA), id);
            }
        }
        throw new JsonException(404, Bundle.getMessage(locale, JsonException.ERROR_OBJECT, JSON.PANEL, name), id);
    }

    public ObjectNode getPanel(Editor editor, String format, int id) {
        if (editor.getAllowInFrameServlet()) {
            Container container = editor.getTargetPanel().getTopLevelAncestor();
            if (container instanceof JmriJFrame) {
                String title = ((Frame) container).getTitle();
                if (!title.isEmpty() &&
                        !Arrays.asList(InstanceManager.getDefault(WebServerPreferences.class).getDisallowedFrames())
                                .contains(title)) {
                    String type = PANEL_PANEL;
                    String name = "Panel";
                    if (editor instanceof ControlPanelEditor) {
                        type = CONTROL_PANEL;
                        name = "ControlPanel";
                    } else if (editor instanceof LayoutEditor) {
                        type = LAYOUT_PANEL;
                        name = "Layout";
                    } else if (editor instanceof SwitchboardEditor) {
                        type = SWITCHBOARD_PANEL;
                        name = "Switchboard";
                    }
                    ObjectNode data = this.mapper.createObjectNode();
                    data.put(NAME, name + "/" + title.replace(" ", "%20").replace("#", "%23")); // NOI18N
                    data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                    data.put(USERNAME, title);
                    data.put(TYPE, type);
                    return message(PANEL, data, id);
                }
            }
        }
        return null;
    }

    public ArrayNode getPanels(String format, int id) {
        ArrayNode root = mapper.createArrayNode();
        // list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor,
        // SwitchboardEditor)
        InstanceManager.getDefault(EditorManager.class).getAll().stream()
                .map(editor -> this.getPanel(editor, format, id))
                .filter(Objects::nonNull).forEach(root::add);
        return root;
    }

    public ArrayNode getPanels(int id) {
        return this.getPanels(JSON.XML, id);
    }

    /**
     * return a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * Railroad from the Railroad Name preferences.
     *
     * @param request the JSON request
     * @return the JSON railroad name message
     */
    public JsonNode getRailroad(@Nonnull JsonRequest request) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NAME, InstanceManager.getDefault(WebServerPreferences.class).getRailroadName());
        return message(JSON.RAILROAD, data, request.id);
    }

    /**
     * return a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * Railroad from the Railroad Name preferences.
     *
     * @param locale the client's Locale
     * @param id     message id set by client
     * @return the JSON railroad name message
     * @deprecated since 4.19.2; use {@link #getRailroad(JsonRequest)} instead
     */
    @Deprecated
    public JsonNode getRailroad(Locale locale, int id) {
        return getRailroad(new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * return a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * requested systemConnection details
     * 
     * @param name    system connection name to return
     * @param request the JSON request
     * @return the JSON systemConnections message
     * @throws JsonException if systemConnection not found
     */
    public JsonNode getSystemConnection(String name, JsonRequest request) throws JsonException {
        for (JsonNode connection : getSystemConnections(request)) {
            JsonNode data = connection.path(JSON.DATA);
            if (data.path(JSON.NAME).asText().equals(name)) {
                return message(JSON.SYSTEM_CONNECTION, data, request.id);
            }
        }
        throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, JSON.SYSTEM_CONNECTION, name),
                request.id);
    }

    /**
     * return a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * requested systemConnection details
     * 
     * @param locale the client's Locale.
     * @param name   system connection name to return
     * @param id     message id set by client
     * @return the JSON systemConnections message.
     * @throws JsonException if systemConnection not found
     * @deprecated since 4.19.2; use
     *             {@link #getSystemConnection(String, JsonRequest)} instead
     */
    @Deprecated
    public JsonNode getSystemConnection(Locale locale, String name, int id) throws JsonException {
        return getSystemConnection(name, new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * return a JSON array containing the defined system connections
     * 
     * @param request the JSON request
     * @return the JSON systemConnections message.
     */
    public ArrayNode getSystemConnections(@Nonnull JsonRequest request) {
        ArrayNode root = mapper.createArrayNode();
        ArrayList<String> prefixes = new ArrayList<>();
        for (ConnectionConfig config : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!config.getDisabled()) {
                ObjectNode data = mapper.createObjectNode();
                data.put(JSON.NAME, config.getConnectionName());
                data.put(JSON.PREFIX, config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                data.put(JSON.MFG, config.getManufacturer());
                data.put(JSON.DESCRIPTION,
                        Bundle.getMessage(request.locale, "ConnectionSucceeded", config.getConnectionName(),
                                config.name(), config.getInfo()));
                prefixes.add(config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                root.add(message(JSON.SYSTEM_CONNECTION, data, request.id));
            }
        }
        InstanceManager.getList(SystemConnectionMemo.class).stream().map(instance -> instance)
                .filter(memo -> (!memo.getDisabled() && !prefixes.contains(memo.getSystemPrefix())))
                .forEach(memo -> {
                    ObjectNode data = mapper.createObjectNode();
                    data.put(JSON.NAME, memo.getUserName());
                    data.put(JSON.PREFIX, memo.getSystemPrefix());
                    data.putNull(JSON.MFG);
                    data.putNull(JSON.DESCRIPTION);
                    prefixes.add(memo.getSystemPrefix());
                    root.add(message(JSON.SYSTEM_CONNECTION, data, request.id));
                });
        // Following is required because despite there being a
        // SystemConnectionMemo for the default internal connection, it is not
        // used for the default internal connection. This allows a client to map
        // the server's internal objects.
        SystemConnectionMemo internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        if (!prefixes.contains(internal.getSystemPrefix())) {
            ObjectNode data = mapper.createObjectNode();
            data.put(JSON.NAME, internal.getUserName());
            data.put(JSON.PREFIX, internal.getSystemPrefix());
            data.putNull(JSON.MFG);
            data.putNull(JSON.DESCRIPTION);
            root.add(message(JSON.SYSTEM_CONNECTION, data, request.id));
        }
        return root;
    }

    /**
     * return a JSON array containing the defined system connections
     * 
     * @param locale the client's Locale.
     * @param id     message id set by client
     * @return the JSON systemConnections message.
     * @deprecated since 4.19.2; use {@link #getSystemConnections(JsonRequest)}
     *             instead
     */
    @Deprecated
    public ArrayNode getSystemConnections(Locale locale, int id) {
        return getSystemConnections(new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Get a JSON message containing the requested configuration profile.
     * 
     * @param profile the requested profile
     * @param manager the in use profile manager
     * @param request the JSON request
     * @return the data for this profile as a JSON Node
     */
    private JsonNode getConfigProfile(@Nonnull Profile profile, @Nonnull ProfileManager manager,
            @Nonnull JsonRequest request) {
        boolean active = profile == manager.getActiveProfile();
        boolean next = profile == manager.getNextActiveProfile();
        boolean isAutoStart = (active && manager.isAutoStartActiveProfile());
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.USERNAME, profile.getName());
        data.put(JSON.UNIQUE_ID, profile.getUniqueId());
        data.put(JSON.NAME, profile.getId());
        data.put(JSON.IS_ACTIVE_PROFILE, active);
        if (request.version.equals(JSON.V5)) {
            // this is not a property of a profile
            data.put(JSON.IS_AUTO_START, isAutoStart);
        }
        data.put(JSON.IS_NEXT_PROFILE, next);
        return message(JSON.CONFIG_PROFILE, data, request.id);
    }

    /**
     * Get the named configuration profile.
     * 
     * @param name    the Profile name
     * @param request the JSON request
     * @return the JSON configProfiles message
     * @throws JsonException if the requested configProfile is not found
     */
    public JsonNode getConfigProfile(@Nonnull String name, @Nonnull JsonRequest request) throws JsonException {
        ProfileManager manager = ProfileManager.getDefault();
        for (Profile profile : manager.getProfiles()) {
            if (profile.getId().equals(name)) {
                return getConfigProfile(profile, manager, request);
            }
        }
        throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                Bundle.getMessage(request.locale, JsonException.ERROR_OBJECT, JSON.CONFIG_PROFILE, name),
                request.id);
    }

    /**
     * find and return the data for a single config profile
     * 
     * @param locale the client's Locale.
     * @param name   requested configProfile name
     * @param id     message id set by client
     * @return the JSON configProfiles message.
     * @throws JsonException if the requested configProfile is not found
     * @deprecated since 4.19.2; use {@link #getConfigProfile(String, JsonRequest)} instead
     */
    @Deprecated
    public JsonNode getConfigProfile(Locale locale, String name, int id) throws JsonException {
        return getConfigProfile(name, new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Get a JSON array of all configuration profiles.
     * 
     * @param request the JSON request
     * @return the JSON configProfiles message
     */
    public ArrayNode getConfigProfiles(@Nonnull JsonRequest request) {
        ArrayNode root = mapper.createArrayNode();
        ProfileManager manager = ProfileManager.getDefault();
        for (Profile profile : manager.getProfiles()) {
            if (profile != null) {
                root.add(getConfigProfile(profile, manager, request));
            }
        }
        return root;
    }

    /**
     * Get a JSON array of all configuration profiles.
     * 
     * @param locale the client's Locale.
     * @param id     message id set by client
     * @return the JSON configProfiles message.
     * @deprecated since 4.19.2; use
     *             {@link #getConfigProfiles(JsonRequest)} instead
     */
    @Deprecated
    public ArrayNode getConfigProfiles(Locale locale, int id) {
        return getConfigProfiles(new JsonRequest(locale, JSON.V5, JSON.GET, id));
    }

    /**
     * Gets the {@link jmri.DccLocoAddress} for a String in the form
     * {@code number(type)} or {@code number}.
     * <p>
     * Type may be {@code L} for long or {@code S} for short. If the type is not
     * specified, type is assumed to be short.
     *
     * @param address the address
     * @return The DccLocoAddress for address
     */
    public static DccLocoAddress addressForString(String address) {
        String[] components = address.split("[()]");
        int number = Integer.parseInt(components[0]);
        boolean isLong = false;
        if (components.length > 1 && "L".equalsIgnoreCase(components[1])) {
            isLong = true;
        }
        return new DccLocoAddress(number, isLong);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        int id = request.id;
        try {
            switch (type) {
                case JSON.CONFIG_PROFILE:
                case JSON.CONFIG_PROFILES:
                    return doSchema(type,
                            server,
                            "jmri/server/json/util/configProfile-server.json",
                            "jmri/server/json/util/configProfile-client.json",
                            id);
                case JSON.NETWORK_SERVICE:
                case JSON.NETWORK_SERVICES:
                    return doSchema(type,
                            server,
                            "jmri/server/json/util/networkService-server.json",
                            "jmri/server/json/util/networkService-client.json",
                            id);
                case JSON.PANEL:
                case JSON.PANELS:
                    return doSchema(type,
                            server,
                            "jmri/server/json/util/panel-server.json",
                            "jmri/server/json/util/panel-client.json",
                            id);
                case JSON.SYSTEM_CONNECTION:
                case JSON.SYSTEM_CONNECTIONS:
                    return doSchema(type,
                            server,
                            "jmri/server/json/util/systemConnection-server.json",
                            "jmri/server/json/util/systemConnection-client.json",
                            id);
                case JsonException.ERROR:
                case JSON.LIST:
                case JSON.PONG:
                    if (server) {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader()
                                        .getResource(RESOURCE_PATH + type + "-server.json")),
                                id);
                    } else {
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                Bundle.getMessage(request.locale, "NotAClientType", type), id);
                    }
                case JSON.LOCALE:
                case JSON.PING:
                    if (!server) {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader()
                                        .getResource(RESOURCE_PATH + type + "-client.json")),
                                id);
                    } else {
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                Bundle.getMessage(request.locale, "NotAServerType", type), id);
                    }
                case JSON.GOODBYE:
                case JSON.HELLO:
                case JSON.METADATA:
                case JSON.NODE:
                case JSON.RAILROAD:
                case JSON.VERSION:
                    return doSchema(type,
                            server,
                            RESOURCE_PATH + type + "-server.json",
                            RESOURCE_PATH + type + "-client.json",
                            id);
                default:
                    throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                            Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
            }
        } catch (IOException ex) {
            throw new JsonException(500, ex, id);
        }
    }
}
