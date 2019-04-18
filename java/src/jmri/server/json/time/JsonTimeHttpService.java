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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood
 */
public class JsonTimeHttpService extends JsonHttpService {

    public JsonTimeHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    // using @Nullable to override @Nonnull in super class
    public JsonNode doGet(String type, @Nullable String name, JsonNode data, Locale locale) throws JsonException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        return doGet(type, timebase, timebase.getTime(), locale);
    }

    public JsonNode doGet(String type, Timebase timebase, Date date, Locale locale) throws JsonException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, TIME);
        ObjectNode data = root.putObject(DATA);
        data.put(TIME, new StdDateFormat().format(date));
        data.put(RATE, timebase.getRate());
        data.put(STATE, timebase.getRun() ? ON : OFF);
        return root;
    }

    @Override
    // using @Nullable to override @Nonnull in super class
    public JsonNode doPost(String type, @Nullable String name, JsonNode data, Locale locale) throws JsonException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        try {
            if (data.path(TIME).isTextual()) {
                timebase.setTime(new StdDateFormat().parse(data.path(TIME).asText()));
            }
            if (data.path(RATE).isDouble() || data.path(RATE).isInt()) {
                timebase.userSetRate(data.path(RATE).asDouble());
            }
            int state = data.findPath(STATE).asInt(0);
            if (state == ON || state == OFF) { // passing the state UNKNOWN (0) will not trigger change
                timebase.setRun(state == ON);
            }
        } catch (ParseException ex) {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorTimeFormat"));
        } catch (TimebaseRateException e) {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorRateFactor"));
        }
        return this.doGet(type, name, data, locale);
    }

    @Override
    public ArrayNode doGetList(String type, JsonNode data, Locale locale) throws JsonException {
        ArrayNode result = this.mapper.createArrayNode();
        result.add(this.doGet(type, null, data, locale));
        return result;
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case TIME:
                return doSchema(type,
                        server,
                        "jmri/server/json/time/time-server.json",
                        "jmri/server/json/time/time-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
