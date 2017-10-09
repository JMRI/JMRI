package jmri.server.json.logs;

import static jmri.server.json.logs.JsonLogs.LOGS;
import static jmri.server.json.logs.JsonLogs.LOG_FILE;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Locale;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle requests for log streaming within the JSON socket services.
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonLogsSocketService extends JsonSocketService {

    protected Tailer tailer = null;

    public JsonLogsSocketService(JsonConnection connection) {
        super(connection);
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        int state = data.path(JSON.STATE).asInt(0);
        switch (state) {
            case JSON.ON:
                log.debug("{} wants to listen to logs", this.connection);
                this.sendMessage(type, data); // echo data back
                if (this.tailer == null) {
                    this.tailer = new Tailer(LOG_FILE, new TailerListener(), 5000);
                    this.tailer.run();
                }
                break;
            case JSON.OFF:
                log.debug("{} no longer wants to listen to logs", this.connection);
                this.sendMessage(type, data); // echo data back
                if (this.tailer != null) {
                    this.tailer.stop();
                    this.tailer = null;
                }
                break;
            default:
                throw new JsonException(400, Bundle.getMessage(locale, "ErrorUnknownState", LOGS, state));
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        try (BufferedReader br = Files.newBufferedReader(LOG_FILE.toPath(), StandardCharsets.UTF_8)) {
            String line = br.readLine();
            while (line != null) {
                this.sendLogEntry(line);
                line = br.readLine();
            }
        }
    }

    @Override
    public void onClose() {
        if (this.tailer != null) {
            this.tailer.stop();
            this.tailer = null;
        }
    }

    private void sendLogEntry(String entry) throws IOException {
        // uncomment to see messages that sent (not a log.debug or log.trace statement
        // since it is a bad idea to log reading a log entry)
        System.out.println(String.format("{\"type\": \"log\", \"data\": %1$s}", entry));
        this.connection.sendMessage(String.format("{\"type\": \"log\", \"data\": %1$s}", entry));
    }

    private final static Logger log = LoggerFactory.getLogger(JsonLogsSocketService.class);

    private class TailerListener extends TailerListenerAdapter {

        @Override
        public void handle(String line) {
            try {
                JsonLogsSocketService.this.sendLogEntry(line);
            } catch (IOException ex) {
                JsonLogsSocketService.this.tailer.stop();
                JsonLogsSocketService.this.tailer = null;
                log.error("IOException sending logs to {}", JsonLogsSocketService.this.connection);
            }
        }

        @Override
        public void handle(Exception ex) {
            try {
                JsonLogsSocketService.this.connection.sendMessage((new JsonException(ex)).getJsonMessage());
            } catch (IOException smex) {
                JsonLogsSocketService.this.tailer.stop();
                JsonLogsSocketService.this.tailer = null;
                log.error("IOException sending logs to {}", JsonLogsSocketService.this.connection);
            }
        }
    }

}
