package jmri.server.json.consist;

import static jmri.server.json.JSON.ADDRESS;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.ENGINES;
import static jmri.server.json.JSON.FORWARD;
import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.IS_LONG_ADDRESS;
import static jmri.server.json.JSON.POSITION;
import static jmri.server.json.JSON.SIZE_LIMIT;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.consist.JsonConsist.CONSIST;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;
import jmri.Consist;
import jmri.LocoAddress;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.jmrit.consisttool.ConsistFile;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.util.JsonUtilHttpService;

/**
 *
 * @author Randall Wood Copyright (C) 2016
 */
public class JsonConsistHttpService extends JsonHttpService {

    final JsonConsistManager manager; // default package visibility

    public JsonConsistHttpService(ObjectMapper mapper) {
        super(mapper);
        this.manager = InstanceManager.getOptionalDefault(JsonConsistManager.class).orElseGet(() -> {
            return InstanceManager.setDefault(JsonConsistManager.class, new JsonConsistManager());
        });
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
        return this.getConsist(locale, JsonUtilHttpService.addressForString(name));
    }

    /**
     * Change the properties and locomotives of a consist.
     *
     * This method takes as input the JSON representation of a consist as
     * provided by {@link #getConsist(Locale, jmri.DccLocoAddress) }.
     *
     * If present in the JSON, this method sets the following consist
     * properties:
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
     * @return the JSON representation of the Consist
     * @throws jmri.server.json.JsonException if there is no consist manager
     *                                        (code 503), the consist does not
     *                                        exist (code 404), or the consist
     *                                        cannot be saved (code 500).
     */
    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
        DccLocoAddress address;
        if (data.path(ADDRESS).canConvertToInt()) {
            address = new DccLocoAddress(data.path(ADDRESS).asInt(), data.path(IS_LONG_ADDRESS).asBoolean(false));
        } else {
            address = JsonUtilHttpService.addressForString(data.path(ADDRESS).asText());
        }
        if (!this.manager.getConsistList().contains(address)) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", CONSIST, name));
        }
        Consist consist = this.manager.getConsist(address);
        if (data.path(ID).isTextual()) {
            consist.setConsistID(data.path(ID).asText());
        }
        if (data.path(TYPE).isInt()) {
            consist.setConsistType(data.path(TYPE).asInt());
        }
        if (data.path(ENGINES).isArray()) {
            ArrayList<DccLocoAddress> engines = new ArrayList<>();
            // add every engine
            for (JsonNode engine : data.path(ENGINES)) {
                DccLocoAddress engineAddress = new DccLocoAddress(engine.path(ADDRESS).asInt(), engine.path(IS_LONG_ADDRESS).asBoolean());
                if (!consist.contains(engineAddress)) {
                    consist.add(engineAddress, engine.path(FORWARD).asBoolean());
                }
                consist.setPosition(engineAddress, engine.path(POSITION).asInt());
                engines.add(engineAddress);
            }
            // remove engines if needed
            ArrayList<DccLocoAddress> consistEngines = new ArrayList<>(consist.getConsistList());
            consistEngines.stream().filter((engineAddress) -> (!engines.contains(engineAddress))).forEach((engineAddress) -> {
                consist.remove(engineAddress);
            });
        }
        try {
            (new ConsistFile()).writeFile(this.manager.getConsistList());
        } catch (IOException ex) {
            throw new JsonException(500, ex.getLocalizedMessage());
        }
        return this.getConsist(locale, address);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
        DccLocoAddress address;
        if (data.path(ADDRESS).canConvertToInt()) {
            address = new DccLocoAddress(data.path(ADDRESS).asInt(), data.path(IS_LONG_ADDRESS).asBoolean(false));
        } else {
            address = JsonUtilHttpService.addressForString(data.path(ADDRESS).asText());
        }
        this.manager.getConsist(address);
        return this.doPost(type, name, data, locale);
    }

    @Override
    public void doDelete(String type, String name, Locale locale) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
        if (!this.manager.getConsistList().contains(JsonUtilHttpService.addressForString(name))) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", CONSIST, name)); // NOI18N
        }
        this.manager.delConsist(JsonUtilHttpService.addressForString(name));
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        if (!this.manager.isConsistManager()) {
            throw new JsonException(503, Bundle.getMessage(locale, "ErrorNoConsistManager")); // NOI18N
        }
        ArrayNode root = mapper.createArrayNode();
        for (LocoAddress address : this.manager.getConsistList()) {
            root.add(getConsist(locale, (DccLocoAddress) address));
        }
        return root;
    }

    /**
     * Get the JSON representation of a consist.
     *
     * The JSON representation is an object with the following data attributes:
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
     * @return The JSON representation of the consist.
     * @throws JsonException This exception has code 404 if the consist does not
     *                       exist.
     */
    public JsonNode getConsist(Locale locale, DccLocoAddress address) throws JsonException {
        if (this.manager.getConsistList().contains(address)) {
            ObjectNode root = mapper.createObjectNode();
            root.put(TYPE, CONSIST);
            ObjectNode data = root.putObject(DATA);
            Consist consist = this.manager.getConsist(address);
            data.put(ADDRESS, consist.getConsistAddress().getNumber());
            data.put(IS_LONG_ADDRESS, consist.getConsistAddress().isLongAddress());
            data.put(TYPE, consist.getConsistType());
            ArrayNode engines = data.putArray(ENGINES);
            consist.getConsistList().stream().forEach((locomotive) -> {
                ObjectNode engine = mapper.createObjectNode();
                engine.put(ADDRESS, locomotive.getNumber());
                engine.put(IS_LONG_ADDRESS, locomotive.isLongAddress());
                engine.put(FORWARD, consist.getLocoDirection(locomotive));
                engine.put(POSITION, consist.getPosition(locomotive));
                engines.add(engine);
            });
            data.put(ID, consist.getConsistID());
            data.put(SIZE_LIMIT, consist.sizeLimit());
            return root;
        } else {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", CONSIST, address.toString())); // NOI18N
        }
    }
}
