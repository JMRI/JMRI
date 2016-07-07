package jmri.server.json.time;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.RATE;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.time.JsonTimeServiceFactory.TIME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import java.text.ParseException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood
 */
class JsonTimeHttpService extends JsonHttpService {

    public JsonTimeHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, TIME);
        ObjectNode data = root.putObject(DATA);
        data.put(TIME, new ISO8601DateFormat().format(InstanceManager.getDefault(jmri.Timebase.class).getTime()));
        data.put(RATE, InstanceManager.getDefault(jmri.Timebase.class).getRate());
        data.put(STATE, InstanceManager.getDefault(jmri.Timebase.class).getRun() ? ON : OFF);
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            if (data.path(TIME).isTextual()) {
                InstanceManager.getDefault(jmri.Timebase.class).setTime(new ISO8601DateFormat().parse(data.path(TIME).asText()));
            }
            if (data.path(RATE).isDouble()) {
                InstanceManager.clockControlInstance().setRate(data.path(RATE).asDouble());
            }
            if (data.path(STATE).isInt()) {
                InstanceManager.getDefault(jmri.Timebase.class).setRun(data.path(STATE).asInt() == ON);
            }
        } catch (ParseException ex) {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorTimeFormat"));
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        return this.doGet(type, null, locale);
    }
}
