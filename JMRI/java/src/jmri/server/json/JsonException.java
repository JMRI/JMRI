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
@SuppressWarnings("serial")
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

    public JsonException(int i, String s, Throwable t) {
        super(s, t);
        this.code = i;
    }

    public JsonException(int i, Throwable t) {
        super(t);
        this.code = i;
    }

    public JsonException(int i, String s) {
        super(s);
        this.code = i;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return this.code;
    }

    public JsonNode getJsonMessage() {
        ObjectNode root = MAPPER.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(DATA);
        data.put(CODE, this.getCode());
        data.put(MESSAGE, this.getMessage());
        return root;
    }
}
