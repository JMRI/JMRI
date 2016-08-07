package jmri.server.json.reporter;

import static jmri.server.json.JSON.COMMENT;
import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.USERNAME;
import static jmri.server.json.reporter.JsonReporter.LAST_REPORT;
import static jmri.server.json.reporter.JsonReporter.REPORT;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 *
 * @author Randall Wood (C) 2016
 */
public class JsonReporterHttpService extends JsonHttpService {

    public JsonReporterHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        ObjectNode root = mapper.createObjectNode();
        root.put(TYPE, REPORTER);
        ObjectNode data = root.putObject(DATA);
        Reporter reporter = InstanceManager.getDefault(ReporterManager.class).getReporter(name);
        if (reporter == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", REPORTER, name));
        }
        data.put(NAME, reporter.getSystemName());
        data.put(USERNAME, reporter.getUserName());
        data.put(STATE, reporter.getState());
        data.put(COMMENT, reporter.getComment());
        if (reporter.getCurrentReport() != null) {
            data.put(REPORT, reporter.getCurrentReport().toString());
        } else {
            data.putNull(REPORT);
        }
        if (reporter.getLastReport() != null) {
            data.put(LAST_REPORT, reporter.getLastReport().toString());
        } else {
            data.putNull(LAST_REPORT);
        }
        return root;
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        Reporter reporter = InstanceManager.getDefault(jmri.ReporterManager.class).getBySystemName(name);
        if (reporter == null) {
            throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", REPORTER, name));
        }
        if (data.path(USERNAME).isTextual()) {
            reporter.setUserName(data.path(USERNAME).asText());
        }
        if (data.path(COMMENT).isTextual()) {
            reporter.setComment(data.path(COMMENT).asText());
        }
        if (!data.path(REPORT).isMissingNode()) {
            if (data.path(REPORT).isNull()) {
                reporter.setReport(null);
            } else {
                reporter.setReport(data.path(REPORT).asText());
            }
        }
        return this.doGet(type, name, locale);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        try {
            InstanceManager.getDefault(ReporterManager.class).provideReporter(name);
        } catch (Exception ex) {
            throw new JsonException(500, Bundle.getMessage(locale, "ErrorCreatingObject", REPORTER, name));
        }
        return this.doPost(type, name, data, locale);
    }

    @Override
    public JsonNode doGetList(String type, Locale locale) throws JsonException {
        ArrayNode root = this.mapper.createArrayNode();
        for (String name : InstanceManager.getDefault(ReporterManager.class).getSystemNameList()) {
            root.add(this.doGet(REPORTER, name, locale));
        }
        return root;
    }
}
