package jmri.jmrix.jmriclient.json;

import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.GOODBYE;
import static jmri.jmris.json.JSON.HELLO;
import static jmri.jmris.json.JSON.LOCALE;
import static jmri.jmris.json.JSON.PING;
import static jmri.jmris.json.JSON.PONG;
import static jmri.jmris.json.JSON.TYPE;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.SwingUtilities;
import jmri.jmris.json.JSON;
import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.jmrix.AbstractMRTrafficController;
import jmri.jmrix.AbstractPortController;
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
        log.debug("Cleanup Starts");
        if (ostream == null) {
            return;    // no connection to terminate
        }
        this.forwardToPort(new JsonClientMessage(mapper.createObjectNode().put(JSON.TYPE, JSON.GOODBYE)), null);
        this.disconnectPort(this.controller);
    }

    @Override
    public void disconnectPort(AbstractPortController controller) {
        if (this.heartbeat != null) {
            this.heartbeat.cancel();
            this.heartbeat = null;
        }
        super.disconnectPort(controller);
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
    protected void notifyMessage(AbstractMRMessage m, AbstractMRListener notMe) {
        // Don't notify listeners of a heartbeat message
        if (((JsonClientMessage) m).getMessage().path(TYPE).asText().equals(PING)) {
            return;
        }
        super.notifyMessage(m, notMe);
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
                    log.info("Connection closing from server.");
                    break;
                } else if (type.equals(HELLO)) {
                    this.receiveHello(data);
                } else if (type.equals(LOCALE)) {
                    this.receiveHello(data);
                } else if (type.equals(PONG)) {
                    // silently ignore
                } else if (!data.isMissingNode()
                        || (root.isArray() && ((ArrayNode) root).size() > 0)) {
                    // process replies with a data node or non-empty arrays
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

    protected void receiveHello(JsonNode helloData) {
        if (!helloData.path(JSON.HEARTBEAT).isMissingNode()) {
            this.heartbeat = new Timer();
            this.heartbeat.schedule(new Heartbeat(), 0, helloData.path(JSON.HEARTBEAT).asInt());
        }
        if (!helloData.path(JSON.NODE).isMissingNode()) {
            ((JsonClientSystemConnectionMemo) this.controller.getSystemConnectionMemo()).setNodeIdentity(helloData.path(JSON.NODE).asText());
        }
        ((JsonClientSystemConnectionMemo) this.controller.getSystemConnectionMemo()).configureManagers();
        // Send LOCALE message
        /* Comment out following until Java 7 can be used.
         ObjectNode root = this.mapper.createObjectNode();
         ObjectNode data = root.putObject(JSON.DATA);
         root.put(JSON.TYPE, JSON.LOCALE);
         data.put(JSON.LOCALE, Locale.getDefault().toLanguageTag());
         this.sendMessage(new JsonClientMessage(root), null);
         */
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
            sendMessage(new JsonClientMessage(mapper.createObjectNode().put(JSON.TYPE, JSON.PING)), null);
        }
    }
}
