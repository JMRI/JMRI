package jmri.server.json.consist;

import static jmri.server.json.JSON.ADDRESS;
import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.FORWARD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.IS_LONG_ADDRESS;
import static jmri.server.json.JSON.POSITION;
import static jmri.server.json.JSON.SIZE_LIMIT;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.consist.JsonConsist.CONSIST;
import static jmri.server.json.consist.JsonConsist.CONSISTS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.Consist;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.LocoAddress;
import jmri.jmrit.consisttool.ConsistFile;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.util.JsonUtilHttpService;

/**
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonConsistHttpService extends JsonHttpService {

    final JsonConsistManager manager; // default package visibility

    public JsonConsistHttpService(ObjectMapper mapper) {
        super(mapper);
        this.manager = InstanceManager.getOptionalDefault(JsonConsistManager.class)
                .orElseGet(() -> InstanceManager.setDefault(JsonConsistManager.class, new JsonConsistManager()));
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, JsonConsist.ERROR_NO_CONSIST_MANAGER), id); // NOI18N
        }
        return this.getConsist(locale, JsonUtilHttpService.addressForString(name), id);
    }

    /**
     * Change the properties and locomotives of a consist. This method takes as
     * input the JSON representation of a consist as provided by
     * {@link #getConsist(Locale, jmri.LocoAddress, int) }. If present in the
     * JSON, this method sets the following consist properties:
     * <ul>
     * <li>consistID</li>
     * <li>consistType</li>
     * <li>locomotives (<em>engines</em> in the JSON representation)<br>
     * <strong>NOTE</strong> Since this method adds, repositions, and deletes
     * locomotives, the JSON representation must contain <em>every</em>
     * locomotive that should be in the consist, if it contains the engines
     * node.</li>
     * </ul>
     *
     * @param type   the JSON message type
     * @param locale the locale to throw exceptions in
     * @param name   the consist address, ignored if data contains an
     *               {@value jmri.server.json.JSON#ADDRESS} and
     *               {@value jmri.server.json.JSON#IS_LONG_ADDRESS} nodes
     * @param data   the consist as a JsonObject
     * @param id     message id set by client
     * @return the JSON representation of the Consist
     * @throws jmri.server.json.JsonException if there is no consist manager
     *                                        (code 503), the consist does not
     *                                        exist (code 404), or the consist
     *                                        cannot be saved (code 500).
     */
    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, JsonConsist.ERROR_NO_CONSIST_MANAGER), id); // NOI18N
        }
        LocoAddress address;
        if (data.path(ADDRESS).canConvertToInt()) {
            address = new DccLocoAddress(data.path(ADDRESS).asInt(), data.path(IS_LONG_ADDRESS).asBoolean(false));
        } else {
            address = JsonUtilHttpService.addressForString(data.path(ADDRESS).asText());
        }
        if (!this.manager.getConsistList().contains(address)) {
            throw new JsonException(404, Bundle.getMessage(locale, JsonException.ERROR_OBJECT, CONSIST, name), id);
        }
        Consist consist = this.manager.getConsist(address);
        if (data.path(NAME).isTextual()) {
            consist.setConsistID(data.path(NAME).asText());
        }
        if (data.path(TYPE).isInt()) {
            consist.setConsistType(data.path(TYPE).asInt());
        }
        if (data.path(ENGINES).isArray()) {
            ArrayList<LocoAddress> engines = new ArrayList<>();
            // add every engine
            for (JsonNode engine : data.path(ENGINES)) {
                DccLocoAddress engineAddress =
                        new DccLocoAddress(engine.path(ADDRESS).asInt(), engine.path(IS_LONG_ADDRESS).asBoolean());
                if (!consist.contains(engineAddress)) {
                    consist.add(engineAddress, engine.path(FORWARD).asBoolean());
                }
                consist.setPosition(engineAddress, engine.path(POSITION).asInt());
                engines.add(engineAddress);
            }
            // remove engines if needed
            ArrayList<DccLocoAddress> consistEngines = new ArrayList<>(consist.getConsistList());
            consistEngines.stream()
                    .filter(engineAddress -> (!engines.contains(engineAddress)))
                    .forEach(consist::remove);
        }
        try {
            (new ConsistFile()).writeFile(this.manager.getConsistList());
        } catch (IOException ex) {
            throw new JsonException(500, ex.getLocalizedMessage(), id);
        }
        return this.getConsist(locale, address, id);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, JsonConsist.ERROR_NO_CONSIST_MANAGER), id); // NOI18N
        }
        LocoAddress address;
        if (data.path(ADDRESS).canConvertToInt()) {
            address = new DccLocoAddress(data.path(ADDRESS).asInt(), data.path(IS_LONG_ADDRESS).asBoolean(false));
        } else {
            address = JsonUtilHttpService.addressForString(data.path(ADDRESS).asText());
        }
        this.manager.getConsist(address);
        return this.doPost(type, name, data, locale, id);
    }

    @Override
    public void doDelete(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, JsonConsist.ERROR_NO_CONSIST_MANAGER), id); // NOI18N
        }
        if (!this.manager.getConsistList().contains(JsonUtilHttpService.addressForString(name))) {
            throw new JsonException(404, Bundle.getMessage(locale, JsonException.ERROR_OBJECT, CONSIST, name), id); // NOI18N
        }
        this.manager.delConsist(JsonUtilHttpService.addressForString(name));
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, JsonConsist.ERROR_NO_CONSIST_MANAGER), id); // NOI18N
        }
        ArrayNode array = mapper.createArrayNode();
        for (LocoAddress address : this.manager.getConsistList()) {
            array.add(getConsist(locale, address, id));
        }
        return message(array, id);
    }

    /**
     * Get the JSON representation of a consist. The JSON representation is an
     * object with the following data attributes:
     * <ul>
     * <li>address - integer address</li>
     * <li>isLongAddress - boolean true if address is long, false if short</li>
     * <li>type - integer, see {@link jmri.Consist#getConsistType() }</li>
     * <li>id - string with consist Id</li>
     * <li>sizeLimit - the maximum number of locomotives the consist can
     * contain</li>
     * <li>engines - array listing every locomotive in the consist. Each entry
     * in the array contains the following attributes:
     * <ul>
     * <li>address - integer address</li>
     * <li>isLongAddress - boolean true if address is long, false if short</li>
     * <li>forward - boolean true if the locomotive running is forward in the
     * consists</li>
     * <li>position - integer locomotive's position in the consist</li>
     * </ul>
     * </ul>
     *
     * @param locale  The locale to throw exceptions in.
     * @param address The address of the consist to get.
     * @param id      message id set by client
     * @return The JSON representation of the consist.
     * @throws JsonException This exception has code 404 if the consist does not
     *                       exist.
     */
    public JsonNode getConsist(Locale locale, LocoAddress address, int id) throws JsonException {
        if (this.manager.getConsistList().contains(address)) {
            ObjectNode data = mapper.createObjectNode();
            Consist consist = this.manager.getConsist(address);
            data.put(ADDRESS, consist.getConsistAddress().getNumber());
            data.put(IS_LONG_ADDRESS, consist.getConsistAddress().isLongAddress());
            data.put(TYPE, consist.getConsistType());
            ArrayNode engines = data.putArray(ENGINES);
            consist.getConsistList().stream().forEach(locomotive -> {
                ObjectNode engine = mapper.createObjectNode();
                engine.put(ADDRESS, locomotive.getNumber());
                engine.put(IS_LONG_ADDRESS, locomotive.isLongAddress());
                engine.put(FORWARD, consist.getLocoDirection(locomotive));
                engine.put(POSITION, consist.getPosition(locomotive));
                engines.add(engine);
            });
            data.put(NAME, consist.getConsistID());
            data.put(SIZE_LIMIT, consist.sizeLimit());
            return message(CONSIST, data, id);
        } else {
            throw new JsonException(404, Bundle.getMessage(locale, JsonException.ERROR_OBJECT, CONSIST, address.toString()), id); // NOI18N
        }
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case CONSIST:
            case CONSISTS:
                return doSchema(type,
                        server,
                        "jmri/server/json/consist/consist-server.json",
                        "jmri/server/json/consist/consist-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }
}
