package jmri.server.json.roster;

import static jmri.server.json.JSON.ADDRESS;
import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
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
import static jmri.server.json.JSON.ROAD;
import static jmri.server.json.JSON.SELECTED_ICON;
import static jmri.server.json.JSON.SHUNTING_FUNCTION;
import static jmri.server.json.JSON.TYPE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.jmrit.roster.Roster;
import jmri.jmrit.roster.RosterEntry;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood
 */
class JsonRosterHttpService extends JsonHttpService {

    public JsonRosterHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
                ObjectNode node = this.mapper.createObjectNode();
                if (name != null) {
                    node.put(GROUP, name);
                }
                return this.getRoster(locale, node);
            case JsonRoster.ROSTER_ENTRY:
                return this.getRosterEntry(locale, name);
            case JsonRoster.ROSTER_GROUP:
                return this.getRosterGroup(locale, name);
            case JsonRoster.ROSTER_GROUPS:
                return this.getRosterGroups(locale);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
                break;
            case JsonRoster.ROSTER_ENTRY:
                break;
            case JsonRoster.ROSTER_GROUP:
                break;
            case JsonRoster.ROSTER_GROUPS:
                break;
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PostNotAllowed", type));
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        switch (type) {
            case JsonRoster.ROSTER:
            case JsonRoster.ROSTER_ENTRY:
                return this.getRoster(locale, this.mapper.createObjectNode());
            case JsonRoster.ROSTER_GROUP:
            case JsonRoster.ROSTER_GROUPS:
                return this.getRosterGroups(locale);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }

    public JsonNode getRoster(@Nonnull Locale locale, @Nonnull JsonNode data) {
        String group = (!data.path(GROUP).isMissingNode()) ? data.path(GROUP).asText() : null;
        if (Roster.ALLENTRIES.equals(group) || Roster.AllEntries(locale).equals(group)) {
            group = null;
        }
        String roadName = (!data.path(ROAD).isMissingNode()) ? data.path(ROAD).asText() : null;
        String roadNumber = (!data.path(NUMBER).isMissingNode()) ? data.path(NUMBER).asText() : null;
        String dccAddress = (!data.path(ADDRESS).isMissingNode()) ? data.path(ADDRESS).asText() : null;
        String mfg = (!data.path(MFG).isMissingNode()) ? data.path(MFG).asText() : null;
        String decoderModel = (!data.path(DECODER_MODEL).isMissingNode()) ? data.path(DECODER_MODEL).asText() : null;
        String decoderFamily = (!data.path(DECODER_FAMILY).isMissingNode()) ? data.path(DECODER_FAMILY).asText() : null;
        String id = (!data.path(NAME).isMissingNode()) ? data.path(NAME).asText() : null;
        ArrayNode root = this.mapper.createArrayNode();
        for (RosterEntry entry : Roster.getDefault().getEntriesMatchingCriteria(roadName, roadNumber, dccAddress, mfg, decoderModel, decoderFamily, id, group)) {
            root.add(getRosterEntry(locale, entry));
        }
        return root;
    }

    /**
     * Returns the JSON representation of a roster entry.
     *
     * Note that this returns, for images and icons, a URL relative to the root
     * folder of the JMRI server. It is expected that clients will fill in the
     * server IP address and port as they know it to be.
     *
     * @param locale
     * @param id     The id of an entry in the roster.
     * @return a roster entry in JSON notation
     */
    public JsonNode getRosterEntry(Locale locale, String id) throws JsonException {
        try {
            return this.getRosterEntry(locale, Roster.getDefault().getEntryForId(id));
        } catch (NullPointerException ex) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, "ErrorNotFound", JsonRoster.ROSTER_ENTRY, id));
        }
    }

    /**
     * Returns the JSON representation of a roster entry.
     *
     * Note that this returns, for images and icons, a URL relative to the root
     * folder of the JMRI server. It is expected that clients will fill in the
     * server IP address and port as they know it to be.
     *
     * @param locale
     * @param entry  A RosterEntry that may or may not be in the roster.
     * @return a roster entry in JSON notation
     */
    public JsonNode getRosterEntry(Locale locale, @Nonnull RosterEntry entry) {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, JsonRoster.ROSTER_ENTRY);
        ObjectNode data = root.putObject(DATA);
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
        data.put(MAX_SPD_PCT, Integer.toString(entry.getMaxSpeedPCT()));
        data.put(IMAGE, (entry.getImagePath() != null) ? "/" + JsonRoster.ROSTER + "/" + entry.getId() + "/" + IMAGE : null);
        data.put(ICON, (entry.getIconPath() != null) ? "/" + JsonRoster.ROSTER + "/" + entry.getId() + "/" + ICON : null);
        data.put(SHUNTING_FUNCTION, entry.getShuntingFunction());
        ArrayNode labels = data.putArray(FUNCTION_KEYS);
        for (int i = 0; i <= entry.getMAXFNNUM(); i++) {
            ObjectNode label = mapper.createObjectNode();
            label.put(NAME, F + i);
            label.put(LABEL, entry.getFunctionLabel(i));
            label.put(LOCKABLE, entry.getFunctionLockable(i));
            label.put(ICON, (entry.getFunctionImage(i) != null) ? "/" + JsonRoster.ROSTER + "/" + entry.getId() + "/" + F + i + "/" + ICON : null);
            label.put(SELECTED_ICON, (entry.getFunctionSelectedImage(i) != null) ? "/" + JsonRoster.ROSTER + "/" + entry.getId() + "/" + F + i + "/" + SELECTED_ICON : null);
            labels.add(label);
        }
        ArrayNode rga = data.putArray(JsonRoster.ROSTER_GROUPS);
        entry.getGroups().stream().forEach((group) -> {
            rga.add(group.getName());
        });
        return root;
    }

    public JsonNode getRosterGroups(Locale locale) throws JsonException {
        ArrayNode root = mapper.createArrayNode();
        root.add(getRosterGroup(locale, Roster.ALLENTRIES));
        for (String name : Roster.getDefault().getRosterGroupList()) {
            root.add(getRosterGroup(locale, name));
        }
        return root;
    }

    public JsonNode getRosterGroup(Locale locale, String name) throws JsonException {
        int size = Roster.getDefault().getEntriesInGroup(name).size();
        if (size != 0) {
            ObjectNode root = mapper.createObjectNode();
            root.put(TYPE, JsonRoster.ROSTER_GROUP);
            ObjectNode data = root.putObject(DATA);
            data.put(NAME, (name.isEmpty()) ? Roster.AllEntries(locale) : name);
            data.put(LENGTH, size);
            return root;
        } else {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, "ErrorNotFound", JsonRoster.ROSTER_GROUP, name));
        }
    }

}
