package jmri.server.json.roster;

import static jmri.server.json.JSON.ADDRESS;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DECODER_FAMILY;
import static jmri.server.json.JSON.DECODER_MODEL;
import static jmri.server.json.JSON.F;
import static jmri.server.json.JSON.FUNCTION_KEYS;
import static jmri.server.json.JSON.GROUP;
import static jmri.server.json.JSON.ICON;
import static jmri.server.json.JSON.IMAGE;
import static jmri.server.json.JSON.IS_LONG_ADDRESS;
import static jmri.server.json.JSON.LABEL;
import static jmri.server.json.JSON.LENGTH;
import static jmri.server.json.JSON.LOCKABLE;
import static jmri.server.json.JSON.MAX_SPD_PCT;
import static jmri.server.json.JSON.MFG;
import static jmri.server.json.JSON.MODEL;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.NUMBER;
import static jmri.server.json.JSON.OWNER;
import static jmri.server.json.JSON.ROAD;
import static jmri.server.json.JSON.SELECTED_ICON;
import static jmri.server.json.JSON.SHUNTING_FUNCTION;
import static jmri.server.json.JSON.VALUE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood Copyright 2016, 2018
 */
public class JsonRosterHttpService extends JsonHttpService {

    public JsonRosterHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
                ObjectNode node = this.mapper.createObjectNode();
                if (!name.isEmpty()) {
                    node.put(GROUP, name);
                }
                return this.getRoster(locale, node, id);
            case JsonRoster.ROSTER_ENTRY:
                return this.getRosterEntry(locale, name, id);
            case JsonRoster.ROSTER_GROUP:
                return this.getRosterGroup(locale, name, id);
            case JsonRoster.ROSTER_GROUPS:
                return this.getRosterGroups(locale, id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
                break;
            case JsonRoster.ROSTER_ENTRY:
                return this.postRosterEntry(locale, name, data, id);
            case JsonRoster.ROSTER_GROUP:
                break;
            case JsonRoster.ROSTER_GROUPS:
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PostNotAllowed", type), id);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
            case JsonRoster.ROSTER_ENTRY:
                return this.getRoster(locale, this.mapper.createObjectNode(), id);
            case JsonRoster.ROSTER_GROUP:
            case JsonRoster.ROSTER_GROUPS:
                return this.getRosterGroups(locale, id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    public JsonNode getRoster(@Nonnull Locale locale, @Nonnull JsonNode data, int id) throws JsonException {
        String group = (!data.path(GROUP).isMissingNode()) ? data.path(GROUP).asText() : null;
        if (Roster.ALLENTRIES.equals(group) || Roster.allEntries(locale).equals(group)) {
            group = null;
        }
        String roadName = (!data.path(ROAD).isMissingNode()) ? data.path(ROAD).asText() : null;
        String roadNumber = (!data.path(NUMBER).isMissingNode()) ? data.path(NUMBER).asText() : null;
        String dccAddress = (!data.path(ADDRESS).isMissingNode()) ? data.path(ADDRESS).asText() : null;
        String mfg = (!data.path(MFG).isMissingNode()) ? data.path(MFG).asText() : null;
        String decoderModel = (!data.path(DECODER_MODEL).isMissingNode()) ? data.path(DECODER_MODEL).asText() : null;
        String decoderFamily = (!data.path(DECODER_FAMILY).isMissingNode()) ? data.path(DECODER_FAMILY).asText() : null;
        String name = (!data.path(NAME).isMissingNode()) ? data.path(NAME).asText() : null;
        ArrayNode array = this.mapper.createArrayNode();
        for (RosterEntry entry : Roster.getDefault().getEntriesMatchingCriteria(roadName, roadNumber, dccAddress, mfg, decoderModel, decoderFamily, name, group)) {
            array.add(getRosterEntry(locale, entry, id));
        }
        return message(array, id);
    }

    /**
     * Returns the JSON representation of a roster entry.
     * <p>
     * Note that this returns, for images and icons, a URL relative to the root
     * folder of the JMRI server. It is expected that clients will fill in the
     * server IP address and port as they know it to be.
     *
     * @param locale the client's locale
     * @param name   the id of an entry in the roster
     * @param id     the message id set by the client
     * @return a roster entry in JSON notation
     * @throws jmri.server.json.JsonException If no roster entry exists for the
     *                                        given id
     */
    public JsonNode getRosterEntry(Locale locale, String name, int id) throws JsonException {
        try {
            return this.getRosterEntry(locale, Roster.getDefault().getEntryForId(name), id);
        } catch (NullPointerException ex) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonRoster.ROSTER_ENTRY, name), id);
        }
    }

    /**
     * Returns the JSON representation of a roster entry.
     * <p>
     * Note that this returns, for images and icons, a URL relative to the root
     * folder of the JMRI server. It is expected that clients will fill in the
     * server IP address and port as they know it to be.
     *
     * @param locale the client's Locale
     * @param entry  A RosterEntry that may or may not be in the roster.
     * @param id     message id set by client
     * @return a roster entry in JSON notation
     * @throws jmri.server.json.JsonException if an error needs to be reported
     *                                        to the user
     */
    public JsonNode getRosterEntry(Locale locale, @Nonnull RosterEntry entry, int id) throws JsonException {
        String entryPath;
        try {
            entryPath = String.format("/%s/%s/", JsonRoster.ROSTER, URLEncoder.encode(entry.getId(), StandardCharsets.UTF_8.toString()));
        } catch (UnsupportedEncodingException ex) {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnencodeable", JsonRoster.ROSTER_ENTRY, entry.getId(), NAME), id);
        }
        ObjectNode data = this.mapper.createObjectNode();
        data.put(NAME, entry.getId());
        data.put(ADDRESS, entry.getDccAddress());
        data.put(IS_LONG_ADDRESS, entry.isLongAddress());
        data.put(ROAD, entry.getRoadName());
        data.put(NUMBER, entry.getRoadNumber());
        data.put(MFG, entry.getMfg());
        data.put(DECODER_MODEL, entry.getDecoderModel());
        data.put(DECODER_FAMILY, entry.getDecoderFamily());
        data.put(MODEL, entry.getModel());
        data.put(COMMENT, entry.getComment());
        data.put(MAX_SPD_PCT, entry.getMaxSpeedPCT());
        data.put(IMAGE, (entry.getImagePath() != null)
                ? entryPath + IMAGE
                : null);
        data.put(ICON, (entry.getIconPath() != null)
                ? entryPath + ICON
                : null);
        data.put(SHUNTING_FUNCTION, entry.getShuntingFunction());
        data.put(OWNER, entry.getOwner());
        data.put(JsonRoster.DATE_MODIFIED, (entry.getDateModified() != null)
                ? new StdDateFormat().format(entry.getDateModified())
                : null);
        ArrayNode labels = data.putArray(FUNCTION_KEYS);
        for (int i = 0; i <= entry.getMAXFNNUM(); i++) {
            ObjectNode label = mapper.createObjectNode();
            label.put(NAME, F + i);
            label.put(LABEL, entry.getFunctionLabel(i));
            label.put(LOCKABLE, entry.getFunctionLockable(i));
            label.put(ICON, (entry.getFunctionImage(i) != null)
                    ? entryPath + F + i + "/" + ICON
                    : null);
            label.put(SELECTED_ICON, (entry.getFunctionSelectedImage(i) != null)
                    ? entryPath + F + i + "/" + SELECTED_ICON
                    : null);
            labels.add(label);
        }
        ArrayNode attributes = data.putArray(JsonRoster.ATTRIBUTES);
        entry.getAttributes().stream().forEach(name -> {
            ObjectNode attribute = mapper.createObjectNode();
            attribute.put(NAME, name);
            attribute.put(VALUE, entry.getAttribute(name));
            attributes.add(attribute);
        });
        ArrayNode rga = data.putArray(JsonRoster.ROSTER_GROUPS);
        entry.getGroups().stream().forEach(group -> rga.add(group.getName()));
        return message(JsonRoster.ROSTER_ENTRY, data, id);
    }

    public JsonNode getRosterGroups(Locale locale, int id) throws JsonException {
        ArrayNode array = mapper.createArrayNode();
        array.add(getRosterGroup(locale, Roster.ALLENTRIES, id));
        for (String name : Roster.getDefault().getRosterGroupList()) {
            array.add(getRosterGroup(locale, name, id));
        }
        return message(array, id);
    }

    public JsonNode getRosterGroup(Locale locale, String name, int id) throws JsonException {
        if (name.equals(Roster.ALLENTRIES) || Roster.getDefault().getRosterGroupList().contains(name)) {
            int size = Roster.getDefault().getEntriesInGroup(name).size();
            ObjectNode data = mapper.createObjectNode();
            data.put(NAME, name.isEmpty() ? Roster.allEntries(locale) : name);
            data.put(LENGTH, size);
            return message(JsonRoster.ROSTER_GROUP, data, id);
        } else {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonRoster.ROSTER_GROUP, name), id);
        }
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
            case JsonRoster.ROSTER_ENTRY:
                return doSchema(type,
                        server,
                        "jmri/server/json/roster/" + type + "-server.json",
                        "jmri/server/json/roster/" + type + "-client.json",
                        id);
            case JsonRoster.ROSTER_GROUP:
            case JsonRoster.ROSTER_GROUPS:
                return doSchema(type,
                        server,
                        "jmri/server/json/roster/rosterGroup-server.json",
                        "jmri/server/json/roster/rosterGroup-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    /**
     * Edit an existing roster entry.
     *
     * @param locale the locale of the client
     * @param name   the roster entry id
     * @param data   the roster entry attributes to be edited
     * @param id     message id set by client
     * @return the roster entry as edited
     * @throws jmri.server.json.JsonException if an error needs to be reported
     *                                        to the user
     */
    public JsonNode postRosterEntry(Locale locale, String name, JsonNode data, int id) throws JsonException {
        RosterEntry entry;
        try {
            entry = Roster.getDefault().getEntryForId(name);
        } catch (NullPointerException ex) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, JsonRoster.ROSTER_ENTRY, name), id);
        }
        if (data.path(JsonRoster.ATTRIBUTES).isArray()) {
            List<String> toKeep = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();
            data.path(JsonRoster.ATTRIBUTES).forEach(attribute -> {
                String key = attribute.path(NAME).asText();
                String value = attribute.path(VALUE).isNull() ? null : attribute.path(VALUE).asText();
                toKeep.add(key);
                entry.putAttribute(key, value);
            });
            entry.getAttributes()
                    .stream()
                    .filter(key -> (!toKeep.contains(key) && !key.startsWith(Roster.ROSTER_GROUP_PREFIX)))
                    .forEachOrdered(toRemove::add);
            toRemove.forEach(entry::deleteAttribute);
        }
        if (data.path(JsonRoster.ROSTER_GROUPS).isArray()) {
            List<String> toKeep = new ArrayList<>();
            List<String> toRemove = new ArrayList<>();
            data.path(JsonRoster.ROSTER_GROUPS).forEach(attribute -> {
                String key = attribute.asText();
                String value = attribute.path(VALUE).isNull() ? null : attribute.path(VALUE).asText();
                toKeep.add(key);
                entry.putAttribute(key, value);
            });
            entry.getGroups()
                    .stream()
                    .filter(key -> (!toKeep.contains(Roster.ROSTER_GROUP_PREFIX + key)))
                    .forEachOrdered(key -> toRemove.add(Roster.ROSTER_GROUP_PREFIX + key));
            toRemove.forEach(entry::deleteAttribute);
        }
        if (data.path(FUNCTION_KEYS).isArray()) {
            data.path(FUNCTION_KEYS).forEach(functionKey -> {
                int function = Integer.parseInt(functionKey.path(NAME).asText().substring(F.length() - 1));
                entry.setFunctionLabel(function, functionKey.path(LABEL).isNull() ? null : functionKey.path(LABEL).asText());
                entry.setFunctionLockable(function, functionKey.path(LOCKABLE).asBoolean());
            });
        }
        if (data.path(ADDRESS).isTextual()) {
            entry.setDccAddress(data.path(ADDRESS).asText());
        }
        if (data.path(ROAD).isTextual()) {
            entry.setRoadName(data.path(ROAD).asText());
        }
        if (data.path(NUMBER).isTextual()) {
            entry.setRoadNumber(data.path(NUMBER).asText());
        }
        if (data.path(MFG).isTextual()) {
            entry.setMfg(data.path(MFG).asText());
        }
        if (data.path(MODEL).isTextual()) {
            entry.setModel(data.path(MODEL).asText());
        }
        if (!data.path(COMMENT).isMissingNode()) {
            entry.setComment(data.path(COMMENT).isTextual() ? data.path(COMMENT).asText() : null);
        }
        if (data.path(MAX_SPD_PCT).isInt()) {
            entry.setMaxSpeedPCT(data.path(MAX_SPD_PCT).asInt());
        }
        if (!data.path(SHUNTING_FUNCTION).isMissingNode()) {
            entry.setShuntingFunction(data.path(SHUNTING_FUNCTION).isTextual() ? data.path(SHUNTING_FUNCTION).asText() : null);
        }
        if (!data.path(OWNER).isMissingNode()) {
            entry.setOwner(data.path(OWNER).isTextual() ? data.path(OWNER).asText() : null);
        }
        entry.updateFile();
        return this.getRosterEntry(locale, entry, id);
    }

}
