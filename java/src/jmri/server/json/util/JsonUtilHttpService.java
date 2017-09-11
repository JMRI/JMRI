package jmri.server.json.util;

import static jmri.server.json.JSON.CONTROL_PANEL;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.LAYOUT_PANEL;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PANEL;
import static jmri.server.json.JSON.SWITCHBOARD_PANEL;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.URL;
import static jmri.server.json.JSON.USERNAME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.Container;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Locale;
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
import jmri.profile.Profile;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.util.ConnectionNameFromSystemName;
import jmri.util.JmriJFrame;
import jmri.util.node.NodeIdentity;
import jmri.util.zeroconf.ZeroConfService;
import jmri.web.server.WebServerPreferences;

/**
 *
 * @author Randall Wood
 */
public class JsonUtilHttpService extends JsonHttpService {

    public JsonUtilHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        switch (type) {
            case JSON.HELLO:
                return this.getHello(locale, JsonServerPreferences.getDefault().getHeartbeatInterval());
            case JSON.METADATA:
                if (name == null) {
                    return this.getMetadata(locale);
                }
                return this.getMetadata(locale, name);
            case JSON.NETWORK_SERVICE:
            case JSON.NETWORK_SERVICES:
                if (name == null) {
                    return this.getNetworkServices(locale);
                }
                return this.getNetworkService(locale, name);
            case JSON.NODE:
                return this.getNode(locale);
            case JSON.PANELS:
                return this.getPanels(locale);
            case JSON.RAILROAD:
                return this.getRailroad(locale);
            case JSON.SYSTEM_CONNECTIONS:
                return this.getSystemConnections(locale);
            case JSON.CONFIG_PROFILES:
                return this.getConfigProfiles(locale);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        switch (type) {
            case JSON.METADATA:
                return this.getMetadata(locale);
            case JSON.NETWORK_SERVICES:
                return this.getNetworkServices(locale);
            case JSON.SYSTEM_CONNECTIONS:
                return this.getSystemConnections(locale);
            case JSON.CONFIG_PROFILES:
                return this.getConfigProfiles(locale);
            default:
                ArrayNode array = this.mapper.createArrayNode();
                JsonNode node = this.doGet(type, null, locale);
                if (node.isArray()) {
                    array.addAll((ArrayNode) node);
                } else {
                    array.add(node);
                }
                return array;
        }
    }

    @Override
    public JsonNode doPost(String type, String name,
            JsonNode data, Locale locale) throws JsonException {
        return this.doGet(type, name, locale);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#HELLO} message.
     *
     * @param locale    the client's Locale
     * @param heartbeat seconds in which a client must send a message before its
     *                  connection is broken
     * @return the JSON hello message
     */
    public JsonNode getHello(Locale locale, int heartbeat) {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, JSON.HELLO);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.JMRI, jmri.Version.name());
        data.put(JSON.JSON, JSON.JSON_PROTOCOL_VERSION);
        data.put(JSON.HEARTBEAT, Math.round(heartbeat * 0.9f));
        data.put(JSON.RAILROAD, WebServerPreferences.getDefault().getRailroadName());
        data.put(JSON.NODE, NodeIdentity.identity());
        data.put(JSON.ACTIVE_PROFILE, ProfileManager.getDefault().getActiveProfileName());
        return root;
    }

    /**
     * Get a JSON message with a metadata element from {@link jmri.Metadata}.
     *
     * @param locale The client's Locale.
     * @param name   The metadata element to get.
     * @return JSON metadata element.
     * @throws JsonException if name is not a recognized metadata element.
     */
    public JsonNode getMetadata(Locale locale, String name) throws JsonException {
        String metadata = Metadata.getBySystemName(name);
        ObjectNode root;
        if (metadata != null) {
            root = mapper.createObjectNode();
            root.put(JSON.TYPE, JSON.METADATA);
            ObjectNode data = root.putObject(JSON.DATA);
            data.put(JSON.NAME, name);
            data.put(JSON.VALUE, Metadata.getBySystemName(name));
        } else {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", JSON.METADATA, name));
        }
        return root;
    }

    /**
     * Get a JSON array of metadata elements as listed by
     * {@link jmri.Metadata#getSystemNameList()}.
     *
     * @param locale The client's Locale.
     * @return Array of JSON metadata elements.
     * @throws JsonException if thrown by
     *                       {@link #getMetadata(java.util.Locale, java.lang.String)}.
     */
    public ArrayNode getMetadata(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : Metadata.getSystemNameList()) {
            root.add(getMetadata(locale, name));
        }
        return root;
    }

    /**
     * Get a running {@link jmri.util.zeroconf.ZeroConfService} using the
     * protocol as the name of the service.
     *
     * @param locale the client's Locale.
     * @param name   the service protocol.
     * @return the JSON networkService message.
     * @throws jmri.server.json.JsonException if type is not a running zeroconf
     *                                        networking protocol.
     */
    public JsonNode getNetworkService(Locale locale, String name) throws JsonException {
        for (ZeroConfService service : ZeroConfService.allServices()) {
            if (service.type().equals(name)) {
                return this.getNetworkService(service);
            }
        }
        throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", JSON.NETWORK_SERVICE, name));
    }

    private JsonNode getNetworkService(ZeroConfService service) {
        ObjectNode ns = mapper.createObjectNode().put(JSON.TYPE, JSON.NETWORK_SERVICE);
        ObjectNode data = ns.putObject(JSON.DATA);
        data.put(JSON.NAME, service.name());
        data.put(JSON.PORT, service.serviceInfo().getPort());
        data.put(JSON.TYPE, service.type());
        Enumeration<String> pe = service.serviceInfo().getPropertyNames();
        while (pe.hasMoreElements()) {
            String pn = pe.nextElement();
            data.put(pn, service.serviceInfo().getPropertyString(pn));
        }
        return ns;
    }

    /**
     *
     * @param locale the client's Locale.
     * @return the JSON networkServices message.
     */
    public ArrayNode getNetworkServices(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        ZeroConfService.allServices().stream().forEach((service) -> {
            root.add(this.getNetworkService(service));
        });
        return root;
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * JMRI node identity and former identities.
     *
     * @param locale the client's Locale
     * @return the JSON node message
     * @see jmri.util.node.NodeIdentity
     */
    public JsonNode getNode(Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, JSON.NODE);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.NODE, NodeIdentity.identity());
        ArrayNode nodes = mapper.createArrayNode();
        NodeIdentity.formerIdentities().stream().forEach((node) -> {
            nodes.add(node);
        });
        data.put(JSON.FORMER_NODES, nodes);
        return root;
    }

    public ObjectNode getPanel(Locale locale, Editor editor, String format) {
        if (editor.getAllowInFrameServlet()) {
            Container container = editor.getTargetPanel().getTopLevelAncestor();
            if (container instanceof JmriJFrame) {
                String title = ((JmriJFrame) container).getTitle();
                if (!title.isEmpty() && !Arrays.asList(WebServerPreferences.getDefault().getDisallowedFrames()).contains(title)) {
                    String type = PANEL;
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
                    ObjectNode root = this.mapper.createObjectNode();
                    root.put(TYPE, PANEL);
                    ObjectNode data = root.putObject(DATA);
                    data.put(NAME, name + "/" + title.replaceAll(" ", "%20").replaceAll("#", "%23")); // NOI18N
                    data.put(URL, "/panel/" + data.path(NAME).asText() + "?format=" + format); // NOI18N
                    data.put(USERNAME, title);
                    data.put(TYPE, type);
                    return root;
                }
            }
        }
        return null;
    }

    public JsonNode getPanels(Locale locale, String format) {
        ArrayNode root = mapper.createArrayNode();
        // list loaded Panels (ControlPanelEditor, PanelEditor, LayoutEditor, SwitchboardEditor)
        // list ControlPanelEditors
        Editor.getEditors(ControlPanelEditor.class).stream()
                .map((editor) -> this.getPanel(locale, editor, format))
                .filter((panel) -> (panel != null)).forEach((panel) -> {
            root.add(panel);
        });
        // list LayoutEditors and PanelEditors
        Editor.getEditors(PanelEditor.class).stream()
                .map((editor) -> this.getPanel(locale, editor, format))
                .filter((panel) -> (panel != null)).forEach((panel) -> {
            root.add(panel);
        });
        // list SwitchboardEditors
        Editor.getEditors(SwitchboardEditor.class).stream()
                .map((editor) -> this.getPanel(locale, editor, format))
                .filter((panel) -> (panel != null)).forEach((panel) -> {
            root.add(panel);
        });
        return root;
    }

    public JsonNode getPanels(Locale locale) {
        return this.getPanels(locale, JSON.XML);
    }

    /**
     * Send a JSON {@link jmri.server.json.JSON#NODE} message containing the
     * Railroad from the Railroad Name preferences.
     *
     * @param locale the client's Locale
     * @return the JSON railroad name message
     */
    public JsonNode getRailroad(Locale locale) {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, JSON.RAILROAD);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.NAME, WebServerPreferences.getDefault().getRailroadName());
        return root;
    }

    /**
     *
     * @param locale the client's Locale.
     * @return the JSON systemConnections message.
     */
    public ArrayNode getSystemConnections(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        ArrayList<String> prefixes = new ArrayList<>();
        for (ConnectionConfig config : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!config.getDisabled()) {
                ObjectNode connection = mapper.createObjectNode().put(JSON.TYPE, JSON.SYSTEM_CONNECTION);
                ObjectNode data = connection.putObject(JSON.DATA);
                data.put(JSON.NAME, config.getConnectionName());
                data.put(JSON.MFG, config.getManufacturer());
                data.put(JSON.PREFIX, config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                data.put(JSON.DESCRIPTION, Bundle.getMessage(locale, "ConnectionSucceeded", config.getConnectionName(), config.name(), config.getInfo()));
                prefixes.add(config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                root.add(connection);
            }
        }
        InstanceManager.getList(SystemConnectionMemo.class).stream().map((instance) -> instance)
                .filter((memo) -> (!memo.getDisabled() && !prefixes.contains(memo.getSystemPrefix()))).forEach((memo) -> {
            ObjectNode connection = mapper.createObjectNode().put(JSON.TYPE, JSON.SYSTEM_CONNECTION);
            ObjectNode data = connection.putObject(JSON.DATA);
            data.put(JSON.NAME, memo.getUserName());
            data.put(JSON.PREFIX, memo.getSystemPrefix());
            data.putNull(JSON.MFG);
            data.putNull(JSON.DESCRIPTION);
            prefixes.add(memo.getSystemPrefix());
            root.add(connection);
        });
        // Following is required because despite there being a SystemConnectionMemo
        // for the default internal connection, it is not used for the default internal
        // connection. This allows a client to map the server's internal objects.
        String prefix = "I";
        if (!prefixes.contains(prefix)) {
            ObjectNode connection = mapper.createObjectNode().put(JSON.TYPE, JSON.SYSTEM_CONNECTION);
            ObjectNode data = connection.putObject(JSON.DATA);
            data.put(JSON.NAME, ConnectionNameFromSystemName.getConnectionName(prefix));
            data.put(JSON.PREFIX, prefix);
            data.putNull(JSON.MFG);
            data.putNull(JSON.DESCRIPTION);
            root.add(connection);
        }
        return root;
    }

    /**
     *
     * @param locale the client's Locale.
     * @return the JSON configProfiles message.
     */
    public ArrayNode getConfigProfiles(Locale locale) {
        ArrayNode root = mapper.createArrayNode();

        for (Profile p : ProfileManager.getDefault().getProfiles()) {
            boolean isActiveProfile = (p == ProfileManager.getDefault().getActiveProfile());
            boolean isAutoStart = (isActiveProfile && ProfileManager.getDefault().isAutoStartActiveProfile()); // only true for activeprofile
            ObjectNode connection = mapper.createObjectNode().put(JSON.TYPE, JSON.CONFIG_PROFILE);
            ObjectNode data = connection.putObject(JSON.DATA);
            data.put(JSON.NAME, p.getName());
            data.put(JSON.UNIQUE_ID, p.getUniqueId());
            data.put(JSON.ID, p.getId());
            data.put(JSON.IS_ACTIVE_PROFILE, isActiveProfile);
            data.put(JSON.IS_AUTO_START, isAutoStart);
            root.add(connection);
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

}
