package jmri.jmris.json;

import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.POST;
import static jmri.jmris.json.JSON.TIME;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractTimeServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;

@Deprecated
public class JsonTimeServer extends AbstractTimeServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    JsonTimeServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    @Override
    public void sendTime() throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTime(this.connection.getLocale())));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendRate() throws IOException {
        this.sendTime();
    }

    @Override
    public void sendStatus() throws IOException {
        this.sendTime();
    }

    public void parseRequest(Locale locale, JsonNode data) throws JsonException, IOException {
        if (data.path(METHOD).asText().equals(POST)) {
            JsonUtil.setTime(locale, data);
        }
        this.sendTime();
        listenToTimebase(true);
    }

    @Override
    public void sendErrorStatus() throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorInternal", TIME))));
    }

    @Override
    public void parseTime(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    @Override
    public void parseRate(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }
}
