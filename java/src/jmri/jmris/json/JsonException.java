package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Throw an exception, but include an HTTP error code.
 *
 * @author rhwood
 */
public class JsonException extends Exception {

    private int code = 500;

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
        return JsonLister.handleError(this.code, this.getLocalizedMessage());
    }
}
