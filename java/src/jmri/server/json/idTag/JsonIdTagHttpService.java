package jmri.server.json.idTag;

import static jmri.server.json.idTag.JsonIdTag.IDTAG;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import java.util.Date;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.ProvidingManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.reporter.JsonReporter;

/**
 *
 * @author Randall Wood Copyright 2019
 */
public class JsonIdTagHttpService extends JsonNamedBeanHttpService<IdTag> {

    public JsonIdTagHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doGet(IdTag idTag, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = this.getNamedBean(idTag, name, type, locale, id); // throws JsonException if idTag == null
        ObjectNode data = root.with(JSON.DATA);
        if (idTag != null) {
            switch (idTag.getState()) {
                case IdTag.UNKNOWN:
                    data.put(JSON.STATE, JSON.UNKNOWN);
                    break;
                default:
                    data.put(JSON.STATE, idTag.getState());
            }
            Reporter reporter = idTag.getWhereLastSeen();
            data.put(JsonReporter.REPORTER, reporter != null ? reporter.getSystemName() : null);
            Date date = idTag.getWhenLastSeen();
            data.put(JSON.TIME, date != null ? new StdDateFormat().format(date) : null);
        }
        return root;
    }

    @Override
    public ObjectNode doPost(IdTag idTag, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        JsonNode node = data.path(JsonReporter.REPORTER);
        if (node.isNull()) {
            idTag.setWhereLastSeen(null);
        } else if (node.isTextual()) {
            Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getBeanBySystemName(node.asText());
            if (reporter != null) {
                idTag.setWhereLastSeen(reporter);
            } else {
                throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, "ErrorNotFound", JsonReporter.REPORTER, node.asText()), id);
            }
        }
        return doGet(idTag, name, type, locale, id);
    }

    @Override
    public void doDelete(IdTag bean, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        super.deleteBean(bean, name, type, data, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case IDTAG:
                return doSchema(type,
                        server,
                        "jmri/server/json/idTag/idTag-server.json",
                        "jmri/server/json/idTag/idTag-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type), id);
        }
    }

    @Override
    protected String getType() {
        return IDTAG;
    }

    @Override
    protected ProvidingManager<IdTag> getManager() throws UnsupportedOperationException {
        return InstanceManager.getDefault(IdTagManager.class);
    }
}
