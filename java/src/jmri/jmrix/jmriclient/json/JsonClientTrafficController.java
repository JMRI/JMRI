package jmri.jmrix.jmriclient.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import jmri.jmris.json.JSON;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.GOODBYE;
import static jmri.jmris.json.JSON.HELLO;
import static jmri.jmris.json.JSON.LOCALE;
import static jmri.jmris.json.JSON.TYPE;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.ConnectionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood 2014
 */
public class JsonClientTrafficController extends AbstractMRTrafficController implements JsonClientInterface {

    protected final ObjectMapper mapper;
    protected Timer heartbeat = null;
    final private static Logger log = LoggerFactory.getLogger(JsonClientTrafficController.class);

    public JsonClientTrafficController() {
        super();
        this.mapper = new ObjectMapper().configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, false);
        this.setAllowUnexpectedReply(true);
    }

    public JsonClientTrafficController instance() {
        return this;
    }

    @Override
    protected void terminate() {
        if (this.heartbeat != null) {
            this.heartbeat.cancel();
            this.heartbeat = null;
        }
    }

    @Override
    protected void setInstance() {
        // nothing to do
    }

    @Override
    protected JsonClientMessage pollMessage() {
        return null;
    }

    @Override
    protected JsonClientListener pollReplyHandler() {
        return null;
    }

    @Override
    protected JsonClientMessage enterProgMode() {
        return null;
    }

    @Override
    protected JsonClientMessage enterNormalMode() {
        return null;
    }

    @Override
    protected AbstractMRReply newReply() {
        return new JsonClientReply();
    }

    @Override
    protected boolean endOfMessage(AbstractMRReply r) {
        return (((JsonClientReply) r).getMessage() != null);
    }

    @Override
    public void addJsonClientListener(JsonClientListener l) {
        this.addListener(l);
    }

    @Override
    public void removeJsonClientListener(JsonClientListener l) {
        this.removeListener(l);
    }

    @Override
    public void sendJsonClientMessage(JsonClientMessage message, JsonClientListener listener) {
        this.sendMessage(message, listener);
    }

    @Override
    protected void forwardMessage(AbstractMRListener client, AbstractMRMessage m) {
        log.debug("Forwarding message {}", m.toString());
        ((JsonClientListener) client).message((JsonClientMessage) m);
    }

    @Override
    protected void forwardReply(AbstractMRListener client, AbstractMRReply m) {
        log.debug("Forwarding reply {}", m.toString());
        ((JsonClientListener) client).reply((JsonClientReply) m);
    }

    @Override
    public void receiveLoop() {
        log.debug("receiveLoop starts");
        ObjectReader reader = this.mapper.reader();

        while (true) {
            try {
                JsonNode root = reader.readTree(this.istream);
                String type = root.path(TYPE).asText();
                JsonNode data = root.path(DATA);
                log.debug("Processing {} with {}", type, data);
                if (type.equals(GOODBYE)) {
                    // TODO: close port connection
                } else if (type.equals(HELLO)) {
                    this.receiveHello(data);
                } else if (type.equals(LOCALE)) {
                    this.receiveHello(data);
                } else if (!data.isMissingNode()) {
                    JsonClientReply reply = new JsonClientReply(root);
                    Runnable r = new RcvNotifier(reply, mLastSender, this);
                    try {
                        SwingUtilities.invokeAndWait(r);
                    } catch (InterruptedException e) {
                        log.error("Exception notifying listeners of reply: {}", e.getMessage(), e);
                    } catch (InvocationTargetException e) {
                        log.error("Exception notifying listeners of reply: {}", e.getMessage(), e);
                    }
                }
            } catch (IOException e) {
                this.rcvException = true;
                reportReceiveLoopException(e);
                break;
            } catch (NoSuchElementException nse) {
                // we get an NSE when we are finished with this client
                // so break out of the loop
                break;
            }
        }
        ConnectionStatus.instance().setConnectionState(this.controller.getCurrentPortName(), ConnectionStatus.CONNECTION_DOWN);
        log.error("Exit from rcv loop");
        this.recovery();
    }

    protected void receiveHello(JsonNode data) {
        this.heartbeat = new Timer();
        this.heartbeat.schedule(new Heartbeat(), 0, data.path(JSON.HEARTBEAT).asInt());
        // TODO: request power status
    }

    @Override
    synchronized protected void forwardToPort(AbstractMRMessage message, AbstractMRListener reply) {
        log.debug("forwardToPort message: [{}]", message.toString());
        // remember who sent this
        mLastSender = reply;

        // forward the message to the registered recipients,
        // which includes the communications monitor, except the sender.
        // Schedule notification via the Swing event queue to ensure order
        Runnable r = new XmtNotifier(message, mLastSender, this);
        SwingUtilities.invokeLater(r);

        try {
            if (this.ostream != null) {
                ((DataOutputStream) this.ostream).writeBytes(this.mapper.writeValueAsString(((JsonClientMessage) message).getMessage()));
            } else {
                // no stream connected
                connectionWarn();
            }
        } catch (IOException e) {
            // TODO Currently there's no port recovery if an exception occurs
            // must restart JMRI to clear xmtException.
            xmtException = true;
            portWarn(e);
        }
    }

    public class Heartbeat extends TimerTask {

        @Override
        public void run() {
            ObjectNode root = mapper.createObjectNode();
            root.put(JSON.TYPE, JSON.PING);
            JsonClientMessage message = new JsonClientMessage(root);
            sendMessage(message, null);
        }
    }
}
