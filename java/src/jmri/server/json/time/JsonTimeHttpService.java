package jmri.server.json.time;

import static jmri.server.json.JSON.OFF;
import static jmri.server.json.JSON.ON;
import static jmri.server.json.JSON.RATE;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TIME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Timebase;
import jmri.TimebaseRateException;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 * @author Randall Wood
 */
public class JsonTimeHttpService extends JsonHttpService {

    public JsonTimeHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    // using @CheckForNull to override @Nonnull in super class
    public JsonNode doGet(String type, @CheckForNull String name, JsonNode data, Locale locale, int id)
            throws JsonException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        return doGet(timebase, timebase.getTime(), id);
    }

    public JsonNode doGet(@Nonnull Timebase timebase, @Nonnull Date date, int id) {
        ObjectNode data = this.mapper.createObjectNode();
        data.put(TIME, new StdDateFormat().format(date));
        data.put(RATE, timebase.getRate());
        data.put(STATE, timebase.getRun() ? ON : OFF);
        return message(TIME, data, id);
    }

    @Override
    // using @CheckForNull to override @Nonnull in super class
    public JsonNode doPost(String type, @CheckForNull String name, JsonNode data, Locale locale, int id)
            throws JsonException {
        Timebase timebase = InstanceManager.getDefault(Timebase.class);
        try {
            if (data.path(TIME).isTextual()) {
                timebase.setTime(new StdDateFormat().parse(data.path(TIME).asText()));
            }
            if (data.path(RATE).isDouble() || data.path(RATE).isInt()) {
                timebase.userSetRate(data.path(RATE).asDouble());
            }
            int state = data.findPath(STATE).asInt(0);
            // passing the state UNKNOWN (0) will not trigger change
            if (state == ON || state == OFF) {
                timebase.setRun(state == ON);
            }
        } catch (ParseException ex) {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorTimeFormat"), id);
        } catch (TimebaseRateException e) {
            throw new JsonException(400, Bundle.getMessage(locale, "ErrorRateFactor"), id);
        }
        return this.doGet(type, name, data, locale, id);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, Locale locale, int id) throws JsonException {
        ArrayNode array = this.mapper.createArrayNode();
        array.add(this.doGet(type, null, data, locale, id));
        return message(array, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        if (TIME.equals(type)) {
            return doSchema(type,
                    server,
                    "jmri/server/json/time/time-server.json",
                    "jmri/server/json/time/time-client.json",
                    id);
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }
}
