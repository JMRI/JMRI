package jmri.server.json.reporter;

import static jmri.server.json.reporter.JsonReporter.LAST_REPORT;
import static jmri.server.json.reporter.JsonReporter.REPORT;
import static jmri.server.json.reporter.JsonReporter.REPORTER;
import static jmri.server.json.reporter.JsonReporter.REPORTERS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonNamedBeanHttpService;

/**
 *
 * @author Randall Wood Copyright 2016, 2018, 2019
 */
public class JsonReporterHttpService extends JsonNamedBeanHttpService<Reporter> {

    public JsonReporterHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public ObjectNode doPost(Reporter reporter, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        if (data.path(JSON.USERNAME).isTextual()) {
            reporter.setUserName(data.path(JSON.USERNAME).asText());
        }
        if (data.path(JSON.COMMENT).isTextual()) {
            reporter.setComment(data.path(JSON.COMMENT).asText());
        }
        if (!data.path(REPORT).isMissingNode()) {
            if (data.path(REPORT).isNull()) {
                reporter.setReport(null);
            } else {
                reporter.setReport(data.path(REPORT).asText());
            }
        }
        return this.doGet(reporter, name, type, locale, id);
    }

    @Override
    protected ObjectNode doGet(Reporter reporter, String name, String type, Locale locale, int id) throws JsonException {
        ObjectNode root = getNamedBean(reporter, name, type, locale, id); // throws JsonException if reporter == null
        ObjectNode data = root.with(JSON.DATA);
        data.put(JSON.STATE, reporter.getState());
        Object cr = reporter.getCurrentReport();
        if (cr != null) {
            String report;
            if (cr instanceof jmri.Reportable) {
                report = ((jmri.Reportable) cr).toReportString();
            } else {
                report = cr.toString();
            }
            data.put(REPORT, report);
            //value matches text displayed on panel
            data.put(JSON.VALUE, (report.isEmpty() ? Bundle.getMessage(locale, "Blank") : report));            
        } else {
            data.putNull(REPORT);
            data.put(JSON.VALUE, Bundle.getMessage(locale, "NoReport"));
        }
        Object lr = reporter.getLastReport();
        if (lr != null) {
            String report;
            if (lr instanceof jmri.Reportable) {
                report = ((jmri.Reportable) lr).toReportString();
            } else {
                report = lr.toString();
            }
            data.put(LAST_REPORT, report);
        } else {
            data.putNull(LAST_REPORT);
        }
        return root;
    }

    @Override
    protected void doDelete(Reporter reporter, String name, String type, JsonNode data, Locale locale, int id) throws JsonException {
        super.deleteBean(reporter, name, type, data, locale, id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case REPORTER:
            case REPORTERS:
                return doSchema(type,
                        server,
                        "jmri/server/json/reporter/reporter-server.json",
                        "jmri/server/json/reporter/reporter-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }
    
    @Override
    protected ReporterManager getManager() {
        return InstanceManager.getDefault(ReporterManager.class);
    }
    
    @Override
    protected String getType() {
        return REPORTER;
    }
}
