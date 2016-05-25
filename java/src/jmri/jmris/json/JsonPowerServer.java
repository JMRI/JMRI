package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractPowerServer;
import jmri.jmris.JmriConnection;

/**
 * Send power status over WebSockets as a JSON String
 *
 * This server creates a JSON string for a Power object with an integer value
 * for the Power state. These states are identical to the
 * {@link jmri.PowerManager} power states, with the addition of -1 as an error
 * indicator.
 *
 * The JSON string is in the form:
 * <code>{type:'power', data:{state:&lt;integer&gt;}}</code>
 *
 * @author rhwood
 * @deprecated 4.3.4
 */
@Deprecated
public class JsonPowerServer extends AbstractPowerServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;

    public JsonPowerServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
        mgrOK();
    }

    @Override
    public void sendStatus(int status) throws IOException {
        this.sendStatus();
    }

    private void sendStatus() throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getPower(this.connection.getLocale())));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus() throws IOException {
        this.sendErrorStatus(500);
    }

    public void sendErrorStatus(int status) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(status, Bundle.getMessage(this.connection.getLocale(), "ErrorPower"))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        if (this.mgrOK()) {
            JsonUtil.setPower(locale, data);
        }
        this.sendStatus();
    }
}
