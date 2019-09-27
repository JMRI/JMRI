package jmri.server.json.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import jmri.InstanceManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonHttpService;

/**
 * A message to be sent by the JMRI JSON message service.
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonMessage {

    /**
     * {@value #CLIENT}
     */
    public static final String CLIENT = "client"; // NOI18N
    /**
     * {@value #MESSAGE}
     */
    public static final String MESSAGE = "message"; // NOI18N
    /**
     * {@value #CONTEXT}
     */
    public static final String CONTEXT = "context"; // NOI18N
    /**
     * {@value #INFO}
     */
    public static final String INFO = "info"; // NOI18N
    /**
     * {@value #SUCCESS}
     */
    public static final String SUCCESS = "success"; // NOI18N
    /**
     * {@value #WARNING}
     */
    public static final String WARNING = "warning"; // NOI18N
    /**
     * {@value #ERROR}
     */
    public static final String ERROR = "error"; // NOI18N

    private final String message;
    private final Locale locale;
    private final String client;
    private final JsonNode context;
    private final TYPE type;

    public static enum TYPE {
        INFO, SUCCESS, WARNING, ERROR
    }

    /**
     * Create a message with an {@value #INFO} type to be sent to all JSON
     * clients.
     *
     * @param message the message to send
     * @param locale  the locale of the message
     */
    public JsonMessage(@Nonnull String message, @Nonnull Locale locale) {
        this(TYPE.INFO, message, null, null, locale);
    }

    /**
     * Create a message to be sent to all JSON clients.
     *
     * @param type    the message type
     * @param message the message to send
     * @param locale  the locale of the message
     */
    public JsonMessage(TYPE type, @Nonnull String message, @Nonnull Locale locale) {
        this(type, message, null, null, locale);
    }

    /**
     * Create a message to be sent to the specified JSON client.
     *
     * @param type    the message type
     * @param message the message to send
     * @param client  the client to send the message to; if null, the message is
     *                sent to all clients
     * @param locale  the locale of the message
     */
    public JsonMessage(TYPE type, @Nonnull String message, @CheckForNull String client, @Nonnull Locale locale) {
        this(type, message, client, null, locale);
    }

    /**
     * Create a message to be sent to the specified JSON client with the
     * specified context.
     *
     * @param type    the message type
     * @param message the message to send
     * @param client  the client to send the message to; if null, the message is
     *                sent to all clients
     * @param context the context for the message; if null, no context is sent
     * @param locale  the locale of the message
     */
    public JsonMessage(TYPE type, @Nonnull String message, @CheckForNull String client, @CheckForNull JsonNode context, @Nonnull Locale locale) {
        this.type = type;
        this.message = message;
        this.locale = locale;
        this.client = client;
        this.context = (context != null && context.isObject()) ? context : null;
    }

    /**
     * Send the message using the default
     * {@link jmri.server.json.message.JsonMessageClientManager}.
     * <p>
     * To send a message to an alternate JsonMessageClientManager, call that
     * manager's
     * {@link jmri.server.json.message.JsonMessageClientManager#send(jmri.server.json.message.JsonMessage)}
     * method.
     */
    public void send() {
        InstanceManager.getDefault(JsonMessageClientManager.class).send(this);
    }

    public String getClient() {
        return this.client;
    }

    public JsonNode getContext() {
        return this.context;
    }

    public String getMessage() {
        return this.message;
    }

    public Locale getLocale() {
        return this.locale;
    }

    /**
     * Get the JSON token for each of the message types used in the
     * constructors.
     *
     * @return the token value
     */
    public String getType() {
        switch (this.type) {
            case SUCCESS:
                return SUCCESS;
            case WARNING:
                return WARNING;
            case ERROR:
                return ERROR;
            case INFO:
            default:
                return INFO;
        }
    }

    /**
     * Create a JSON node from this message.
     *
     * @param mapper the instance used to create raw JSON nodes
     * @return the JSON node
     */
    public JsonNode toJSON(ObjectMapper mapper) {
        ObjectNode data = mapper.createObjectNode();
        data.put(MESSAGE, this.getMessage());
        if (this.getContext() != null) {
            data.set(CONTEXT, this.getContext());
        } else {
            data.putNull(CONTEXT);
        }
        data.put(JSON.TYPE, this.getType());
        data.put(JSON.LOCALE, this.getLocale().toLanguageTag());
        return JsonHttpService.message(mapper, MESSAGE, data, null, 0);
    }
}
