package jmri.server.json.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Metadata;
import jmri.jmris.json.JsonServerPreferences;
import jmri.jmrix.ConnectionConfig;
import jmri.jmrix.ConnectionConfigManager;
import jmri.jmrix.SystemConnectionMemo;
import jmri.profile.ProfileManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.util.ConnectionNameFromSystemName;
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
        if (name.isEmpty()) {
            return this.doGetList(type, locale);
        }
        switch (type) {
            case JSON.HELLO:
                return this.getHello(locale, JsonServerPreferences.getDefault().getHeartbeatInterval());
            case JSON.METADATA:
                return this.getMetadata(locale, name);
            case JSON.NODE:
                return this.getNode(locale);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        switch (type) {
            case JSON.METADATA:
                return this.getMetadata(locale);
            case JSON.NETWORK_SERVICES:
                return this.getNetworkServices(locale);
            case JSON.SYSTEM_CONNECTIONS:
                return this.getSystemConnections(locale);
            default:
                return this.doGet(type, null, locale);
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
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
        data.put(JSON.RAILROAD, WebServerPreferences.getDefault().getRailRoadName());
        data.put(JSON.NODE, NodeIdentity.identity());
        data.put(JSON.ACTIVE_PROFILE, ProfileManager.getDefault().getActiveProfile().getName());
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
    public JsonNode getMetadata(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        for (String name : Metadata.getSystemNameList()) {
            root.add(getMetadata(locale, name));
        }
        return root;
    }

    /**
     *
     * @param locale the client's Locale.
     * @return the JSON networkServices message.
     */
    public JsonNode getNetworkServices(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        ZeroConfService.allServices().stream().forEach((service) -> {
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
            root.add(ns);
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

    /**
     *
     * @param locale the client's Locale.
     * @return the JSON networkServices message.
     */
    public JsonNode getSystemConnections(Locale locale) {
        ArrayNode root = mapper.createArrayNode();
        ArrayList<String> prefixes = new ArrayList<>();
        for (ConnectionConfig config : InstanceManager.getDefault(ConnectionConfigManager.class)) {
            if (!config.getDisabled()) {
                ObjectNode connection = mapper.createObjectNode().put(JSON.TYPE, JSON.SYSTEM_CONNECTION);
                ObjectNode data = connection.putObject(JSON.DATA);
                data.put(JSON.NAME, config.getConnectionName());
                data.put(JSON.MFG, config.getManufacturer());
                data.put(JSON.PREFIX, config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                prefixes.add(config.getAdapter().getSystemConnectionMemo().getSystemPrefix());
                root.add(connection);
            }
        }
        InstanceManager.getList(SystemConnectionMemo.class).stream().map((instance)
                -> (SystemConnectionMemo) instance).filter((memo)
                -> (!memo.getDisabled() && !prefixes.contains(memo.getSystemPrefix()))).forEach((memo) -> {
            ObjectNode connection = mapper.createObjectNode().put(JSON.TYPE, JSON.SYSTEM_CONNECTION);
            ObjectNode data = connection.putObject(JSON.DATA);
            data.put(JSON.NAME, memo.getUserName());
            data.put(JSON.PREFIX, memo.getSystemPrefix());
            data.putNull(JSON.MFG);
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
            root.add(connection);
        }
        return root;
    }

}
