package jmri.server.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.TYPE;

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

    public JsonException(int i, String s, Throwable t) {
        this(i, s, t, null);
    }

    public JsonException(int i, String s, Throwable t, ObjectNode additionalData) {
        super(s, t);
        this.code = i;
        this.additionalData = additionalData;
    }

    public JsonException(int i, Throwable t) {
        this(i, t, null);
    }

    public JsonException(int i, Throwable t, ObjectNode additionalData) {
        super(t);
        this.code = i;
        this.additionalData = additionalData;
    }

    public JsonException(int i, String s) {
        this(i, s, (ObjectNode) null);
    }

    public JsonException(int i, String s, ObjectNode additionalData) {
        super(s);
        this.code = i;
        this.additionalData = additionalData;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return this.code;
    }

    public ObjectNode getAdditionalData() {
        return this.additionalData.deepCopy();
    }

    public JsonNode getJsonMessage() {
        ObjectNode root = MAPPER.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(DATA);
        data.put(CODE, this.getCode());
        data.put(MESSAGE, this.getMessage());
        if (additionalData != null) {
            data.setAll(additionalData);
        }
        return root;
    }
}
