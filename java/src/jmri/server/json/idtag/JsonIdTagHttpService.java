package jmri.server.json.idtag;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.util.StdDateFormat;

import static jmri.server.json.idtag.JsonIdTag.IDTAG;

import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import jmri.IdTag;
import jmri.IdTagManager;
import jmri.InstanceManager;
import jmri.NamedBean;
import jmri.ProvidingManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;
import jmri.server.json.JsonRequest;
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
    public ObjectNode doGet(IdTag idTag, String name, String type, JsonRequest request) throws JsonException {
        ObjectNode root = this.getNamedBean(idTag, name, getType(), request); // throws JsonException if idTag == null
        ObjectNode data = root.with(JSON.DATA);
        if (idTag != null) {
            int state = idTag.getState();
            if (state == NamedBean.UNKNOWN) {
                data.put(JSON.STATE, JSON.UNKNOWN);
            } else {
                data.put(JSON.STATE, state);
            }
            Reporter reporter = idTag.getWhereLastSeen();
            data.put(JsonReporter.REPORTER, reporter != null ? reporter.getSystemName() : null);
            Date date = idTag.getWhenLastSeen();
            data.put(JSON.TIME, date != null ? new StdDateFormat().format(date) : null);
        }
        return root;
    }

    @Override
    public ObjectNode doPost(IdTag idTag, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
        JsonNode node = data.path(JsonReporter.REPORTER);
        if (node.isNull()) {
            idTag.setWhereLastSeen(null);
        } else if (node.isTextual()) {
            Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getBySystemName(node.asText());
            if (reporter != null) {
                idTag.setWhereLastSeen(reporter);
            } else {
                throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, JsonReporter.REPORTER, node.asText()), request.id);
            }
        }
        return doGet(idTag, name, type, request);
    }

    @Override
    public void doDelete(IdTag bean, String name, String type, JsonNode data, JsonRequest request) throws JsonException {
        super.deleteBean(bean, name, type, data, request);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        if (IDTAG.equals(type)) {
            return doSchema(type,
                    server,
                    "jmri/server/json/idtag/idTag-server.json",
                    "jmri/server/json/idtag/idTag-client.json",
                    request.id);
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    protected String getType() {
        return IDTAG;
    }

    @Override
    protected ProvidingManager<IdTag> getManager() {
        return InstanceManager.getDefault(IdTagManager.class);
    }
}
