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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Metadata;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrit.display.Editor;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.panelEditor.PanelEditor;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.util.JmriJFrame;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceManager;
import jmri.web.server.WebServerPreferences;

/**
 *
 * @author Randall Wood Copyright 2016, 2017, 2018
 */
public class JsonUtilHttpService extends JsonHttpService {

    public JsonUtilHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    // use @Nullable to override @Nonnull specified in superclass
    public JsonNode doGet(String type, @Nullable String name, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case JSON.HELLO:
                return this.getHello(locale, InstanceManager.getDefault(JsonServerPreferences.class).getHeartbeatInterval(), id);
            case JSON.METADATA:
                if (name == null) {
                    return this.getMetadata(locale, id);
                }
                return this.getMetadata(locale, name, id);
            case JSON.NETWORK_SERVICE:
            case JSON.NETWORK_SERVICES:
                if (name == null) {
                    return this.getNetworkServices(locale, id);
                }
                return this.getNetworkService(locale, name, id);
            case JSON.NODE:
                return this.getNode(locale, id);
            case JSON.PANELS:
                return this.getPanels(locale, id);
            case JSON.RAILROAD:
                return this.getRailroad(locale, id);
            case JSON.SYSTEM_CONNECTIONS:
                return this.getSystemConnections(locale, id);
            case JSON.CONFIG_PROFILES:
                return this.getConfigProfiles(locale, id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type), id);
        }
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case JSON.METADATA:
                return this.getMetadata(locale, id);
            case JSON.NETWORK_SERVICES:
                return this.getNetworkServices(locale, id);
            case JSON.SYSTEM_CONNECTIONS:
                return this.getSystemConnections(locale, id);
            case JSON.CONFIG_PROFILES:
                return this.getConfigProfiles(locale, id);
            default:
                ArrayNode array = this.mapper.createArrayNode();
                JsonNode node = this.doGet(type, null, data, locale, id);
                if (node.isArray()) {
                    array.addAll((ArrayNode) node);
                } else {
                    array.add(node);
                }
                return array;
        }
    }

    @Override
    // Use @Nullable to override non-null requirement of superclass
    public JsonNode doPost(String type, @Nullable String name,
            JsonNode data, Locale locale, int id) throws JsonException {
        return this.doGet(type, name, data, locale, id);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#HELLO} message.
     *
     * @param locale    the client's Locale
     * @param id        message id set by client
     * @param heartbeat seconds in which a client must send a message before its
     *                  connection is broken
     * @return the JSON hello message
     */
    public JsonNode getHello(Locale locale, int heartbeat, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.JMRI, jmri.Version.name());
        data.put(JSON.JSON, JSON.JSON_PROTOCOL_VERSION);
        data.put(JSON.HEARTBEAT, Math.round(heartbeat * 0.9f));
        data.put(JSON.RAILROAD, InstanceManager.getDefault(WebServerPreferences.class).getRailroadName());
        data.put(JSON.NODE, NodeIdentity.networkIdentity());
        Profile activeProfile = ProfileManager.getDefault().getActiveProfile();
        data.put(JSON.ACTIVE_PROFILE, activeProfile != null ? activeProfile.getName() : null);
        return message(JSON.HELLO, data, id);
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
        String metadata = Metadata.getBySystemName(name);
        ObjectNode data = mapper.createObjectNode();
        if (metadata != null) {
            data.put(JSON.NAME, name);
            data.put(JSON.VALUE, Metadata.getBySystemName(name));
        } else {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", JSON.METADATA, name), id);
        }
        return message(JSON.METADATA, data, id);
    }

    /**
     * Get a JSON array of metadata elements as listed by
     * {@link jmri.Metadata#getSystemNameList()}.
     *
     * @param locale The client's Locale.
     * @param id     message id set by client
     * @return Array of JSON metadata elements.
     * @throws JsonException if thrown by
     *                       {@link #getMetadata(java.util.Locale, java.lang.String, int)}.
     */
    public ArrayNode getMetadata(Locale locale, int id) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : Metadata.getSystemNameList()) {
            root.add(getMetadata(locale, name, id));
        }
        return root;
    }

    /**
     * Get a running {@link jmri.util.zeroconf.ZeroConfService} using the
     * protocol as the name of the service.
     *
     * @param locale the client's Locale.
     * @param name   the service protocol.
     * @param id     message id set by client
     * @return the JSON networkService message.
     * @throws jmri.server.json.JsonException if type is not a running zeroconf
     *                                        networking protocol.
     */
    public JsonNode getNetworkService(Locale locale, String name, int id) throws JsonException {
        for (ZeroConfService service : InstanceManager.getDefault(ZeroConfServiceManager.class).allServices()) {
            if (service.getType().equals(name)) {
                return this.getNetworkService(service, id);
            }
        }
        throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", JSON.NETWORK_SERVICE, name), id);
    }

    private JsonNode getNetworkService(ZeroConfService service, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NAME, service.getName());
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
     *
     * @param locale the client's Locale.
     * @param id     message id set by client
     * @return the JSON networkServices message.
     */
    public ArrayNode getNetworkServices(Locale locale, int id) {
        ArrayNode root = mapper.createArrayNode();
        InstanceManager.getDefault(ZeroConfServiceManager.class).allServices().stream().forEach((service) -> {
            root.add(this.getNetworkService(service, id));
        });
        return root;
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * JMRI node identity and former identities.
     *
     * @param locale the client's Locale
     * @param id     message id set by client
     * @return the JSON node message
     * @see jmri.util.node.NodeIdentity
     */
    public JsonNode getNode(Locale locale, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NODE, NodeIdentity.networkIdentity());
        ArrayNode nodes = mapper.createArrayNode();
        NodeIdentity.formerIdentities().stream().forEach((node) -> {
            nodes.add(node);
        });
        data.set(JSON.FORMER_NODES, nodes);
        return message(JSON.NODE, data, id);
    }

    public ObjectNode getPanel(Locale locale, Editor editor, String format, int id) {
        if (editor.getAllowInFrameServlet()) {
            Container container = editor.getTargetPanel().getTopLevelAncestor();
            if (container instanceof JmriJFrame) {
                String title = ((Frame) container).getTitle();
                if (!title.isEmpty() && !Arrays.asList(InstanceManager.getDefault(WebServerPreferences.class).getDisallowedFrames()).contains(title)) {
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
                    data.put(NAME, name + "/" + title.replaceAll(" ", "%20").replaceAll("#", "%23")); // NOI18N
                    data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                    data.put(USERNAME, title);
                    data.put(TYPE, type);
                    return message(PANEL, data, id);
                }
            }
        }
        return null;
    }

    public JsonNode getPanels(Locale locale, String format, int id) {
        ArrayNode root = mapper.createArrayNode();
        // list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor, SwitchboardEditor)
        // list ControlPanelEditors
        Editor.getEditors(ControlPanelEditor.class).stream()
                .map((editor) -> this.getPanel(locale, editor, format, id))
                .filter((panel) -> (panel != null)).forEach((panel) -> {
            root.add(panel);
        });
        // list LayoutEditors and PanelEditors
        Editor.getEditors(PanelEditor.class).stream()
                .map((editor) -> this.getPanel(locale, editor, format, id))
                .filter((panel) -> (panel != null)).forEach((panel) -> {
            root.add(panel);
        });
        // list SwitchboardEditors
        Editor.getEditors(SwitchboardEditor.class).stream()
                .map((editor) -> this.getPanel(locale, editor, format, id))
                .filter((panel) -> (panel != null)).forEach((panel) -> {
            root.add(panel);
        });
        return root;
    }

    public JsonNode getPanels(Locale locale, int id) {
        return this.getPanels(locale, JSON.XML, id);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * Railroad from the Railroad Name preferences.
     *
     * @param locale the client's Locale
     * @param id     message id set by client
     * @return the JSON railroad name message
     */
    public JsonNode getRailroad(Locale locale, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NAME, InstanceManager.getDefault(WebServerPreferences.class).getRailroadName());
        return message(JSON.RAILROAD, data, id);
    }

    /**
     *
     * @param locale the client's Locale.
     * @param id     message id set by client
     * @return the JSON systemConnections message.
     */
    public ArrayNode getSystemConnections(Locale locale, int id) {
        ArrayNode root = mapper.createArrayNode();
        ArrayList<String> prefixes = new ArrayList<>();
        for (ConnectionConfig config : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!config.getDisabled()) {
                ObjectNode data = mapper.createObjectNode();
                data.put(JSON.NAME, config.getConnectionName());
                data.put(JSON.MFG, config.getManufacturer());
                data.put(JSON.PREFIX, config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                data.put(JSON.DESCRIPTION, Bundle.getMessage(locale, "ConnectionSucceeded", config.getConnectionName(), config.name(), config.getInfo()));
                prefixes.add(config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                root.add(message(JSON.SYSTEM_CONNECTION, data, id));
            }
        }
        InstanceManager.getList(SystemConnectionMemo.class).stream().map((instance) -> instance)
                .filter((memo) -> (!memo.getDisabled() && !prefixes.contains(memo.getSystemPrefix()))).forEach((memo) -> {
            ObjectNode data = mapper.createObjectNode();
            data.put(JSON.NAME, memo.getUserName());
            data.put(JSON.PREFIX, memo.getSystemPrefix());
            data.putNull(JSON.MFG);
            data.putNull(JSON.DESCRIPTION);
            prefixes.add(memo.getSystemPrefix());
            root.add(message(JSON.SYSTEM_CONNECTION, data, id));
        });
        // Following is required because despite there being a SystemConnectionMemo
        // for the default internal connection, it is not used for the default internal
        // connection. This allows a client to map the server's internal objects.
        SystemConnectionMemo internal = InstanceManager.getDefault(InternalSystemConnectionMemo.class);
        if (!prefixes.contains(internal.getSystemPrefix())) {
            ObjectNode data = mapper.createObjectNode();
            data.put(JSON.NAME, internal.getUserName());
            data.put(JSON.PREFIX, internal.getSystemPrefix());
            data.putNull(JSON.MFG);
            data.putNull(JSON.DESCRIPTION);
            root.add(message(JSON.SYSTEM_CONNECTION, data, id));
        }
        return root;
    }

    /**
     *
     * @param locale the client's Locale.
     * @param id     message id set by client
     * @return the JSON configProfiles message.
     */
    public ArrayNode getConfigProfiles(Locale locale, int id) {
        ArrayNode root = mapper.createArrayNode();

        for (Profile p : ProfileManager.getDefault().getProfiles()) {
            boolean isActiveProfile = (p == ProfileManager.getDefault().getActiveProfile());
            boolean isAutoStart = (isActiveProfile && ProfileManager.getDefault().isAutoStartActiveProfile()); // only true for activeprofile
            ObjectNode data = mapper.createObjectNode();
            data.put(JSON.USERNAME, p.getName());
            data.put(JSON.UNIQUE_ID, p.getUniqueId());
            data.put(JSON.NAME, p.getId());
            data.put(JSON.IS_ACTIVE_PROFILE, isActiveProfile);
            data.put(JSON.IS_AUTO_START, isAutoStart);
            root.add(message(JSON.CONFIG_PROFILE, data, id));
        }
        return root;
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
    static public DccLocoAddress addressForString(String address) {
        String[] components = address.split("[()]");
        int number = Integer.parseInt(components[0]);
        boolean isLong = false;
        if (components.length > 1 && "L".equals(components[1].toUpperCase())) {
            isLong = true;
        }
        return new DccLocoAddress(number, isLong);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
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
                                this.mapper.readTree(this.getClass().getClassLoader().getResource("jmri/server/json/util/" + type + "-server.json")), id);
                    } else {
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "NotAClientType", type), id);
                    }
                case JSON.LOCALE:
                case JSON.PING:
                    if (!server) {
                        return doSchema(type, server,
                                this.mapper.readTree(this.getClass().getClassLoader().getResource("jmri/server/json/util/" + type + "-client.json")), id);
                    } else {
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "NotAServerType", type), id);
                    }
                case JSON.GOODBYE:
                case JSON.HELLO:
                case JSON.METADATA:
                case JSON.NODE:
                case JSON.RAILROAD:
                    return doSchema(type,
                            server,
                            "jmri/server/json/util/" + type + "-server.json",
                            "jmri/server/json/util/" + type + "-client.json",
                            id);
                default:
                    throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type), id);
            }
        } catch (IOException ex) {
            throw new JsonException(500, ex, id);
        }
    }
}
