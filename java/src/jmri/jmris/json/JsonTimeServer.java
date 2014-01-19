package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.AbstractTimeServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.POST;
import static jmri.jmris.json.JSON.TIME;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonTimeServer extends AbstractTimeServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonTimeServer.class);

    JsonTimeServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
        listenToTimebase(true);
    }

    @Override
    public void sendTime() throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTime()));
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

    void parseRequest(JsonNode data) throws JsonException, IOException {
        if (data.path(METHOD).asText().equals(POST)) {
            JsonUtil.setTime(data);
        }
        this.sendTime();
        this.timebase.addMinuteChangeListener(timeListener);
    }

    @Override
    public void sendErrorStatus() throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage("ErrorInternal", TIME))));
    }

    @Override
    public void parseTime(String statusString) throws JmriException, IOException    {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    @Override
    public void parseRate(String statusString) throws JmriException, IOException    {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }
}
