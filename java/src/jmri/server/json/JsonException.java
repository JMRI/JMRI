package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Throw an exception, but include an HTTP error code.
 *
 * @author Randall Wood Copyright (C) 2015, 2016
 */
public class JsonException extends Exception {

    /**
     * {@value #ERROR}
     */
    public static final String ERROR = "error"; // NOI18N
    /**
     * {@value #CODE}
     */
    public static final String CODE = "code"; // NOI18N
    /**
     * {@value #MESSAGE}
     */
    public static final String MESSAGE = "message"; // NOI18N

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private final int code;
    private final ObjectNode additionalData;
    /**
     * Only access through {@link #getId()}, even when used within this class
     */
    private final int id;

    /**
     * Create an exception that can be passed to a JSON client.
     * 
     * @param code      the error code
     * @param message   message, displayable to the user, in the client's
     *                      preferred locale
     * @param throwable the cause of the exception
     * @param id        the message id passed by the client, or the additive
     *                      inverse of that id
     */
    public JsonException(int code, String message, Throwable throwable, int id) {
        this(code, message, throwable, null, id);
    }

    /**
     * Create an exception that can be passed to a JSON client.
     * 
     * @param code           the error code
     * @param message        message, displayable to the user, in the client's
     *                           preferred locale
     * @param throwable      the cause of the exception
     * @param additionalData additional data to be passed to the client
     * @param id             the message id passed by the client, or the
     *                           additive inverse of that id
     */
    public JsonException(int code, String message, Throwable throwable, ObjectNode additionalData, int id) {
        super(message, throwable);
        this.code = code;
        this.additionalData = additionalData;
        this.id = id;
    }

    /**
     * Create an exception that can be passed to a JSON client.
     * 
     * @param code      the error code
     * @param throwable the cause of the exception
     * @param id        the message id passed by the client, or the additive
     *                      inverse of that id
     */
    public JsonException(int code, Throwable throwable, int id) {
        this(code, throwable, null, id);
    }

    /**
     * Create an exception that can be passed to a JSON client.
     * 
     * @param code           the error code
     * @param throwable      the cause of the exception
     * @param additionalData additional data to be passed to the client
     * @param id             the message id passed by the client, or the
     *                           additive inverse of that id
     */
    public JsonException(int code, Throwable throwable, ObjectNode additionalData, int id) {
        super(throwable);
        this.code = code;
        this.additionalData = additionalData;
        this.id = id;
    }

    /**
     * Create an exception that can be passed to a JSON client.
     * 
     * @param code    the error code
     * @param message message, displayable to the user, in the client's
     *                    preferred locale
     * @param id      the message id passed by the client, or the additive
     *                    inverse of that id
     */
    public JsonException(int code, String message, int id) {
        this(code, message, (ObjectNode) null, id);
    }

    /**
     * Create an exception that can be passed to a JSON client.
     * 
     * @param code           the error code
     * @param message        message, displayable to the user, in the client's
     *                           preferred locale
     * @param additionalData additional data to be passed to the client
     * @param id             the message id passed by the client, or the
     *                           additive inverse of that id
     */
    public JsonException(int code, String message, ObjectNode additionalData, int id) {
        super(message);
        this.code = code;
        this.additionalData = additionalData;
        this.id = id;
    }

    /**
     * Get the error code (usually an HTTP error code)
     * 
     * @return the code
     */
    public int getCode() {
        return this.code;
    }

    /**
     * Get any additional data passed to the client. This is specific to, and
     * will vary based on, the original exception.
     * 
     * @return the additional data or null if none
     */
    public ObjectNode getAdditionalData() {
        return this.additionalData.deepCopy();
    }

    /**
     * Get the id passed to the client.
     * 
     * @return the absolute value of the id
     * @see JsonHttpService
     */
    public int getId() {
        return Math.abs(id);
    }

    /**
     * Get the JSON formatted error message.
     * 
     * @return the error message in a JSON format
     */
    public JsonNode getJsonMessage() {
        ObjectNode data = MAPPER.createObjectNode();
        data.put(CODE, this.getCode());
        data.put(MESSAGE, this.getMessage());
        if (additionalData != null) {
            data.setAll(additionalData);
        }
        return JsonHttpService.message(MAPPER, ERROR, data, null, getId());
    }
}
