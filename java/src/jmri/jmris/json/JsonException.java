package jmri.jmris.json;

/**
 * Throw an exception, but include an HTTP error code.
 *
 * @author Randall Wood
 * @deprecated Use {@link jmri.server.json.JsonException}
 */
@Deprecated
@SuppressWarnings("serial")
public class JsonException extends jmri.server.json.JsonException {

    public JsonException(int i, String s, Throwable t) {
        super(i, s, t);
    }

    public JsonException(int i, Throwable t) {
        super(i, t);
    }

    public JsonException(int i, String s) {
        super(i, s);
    }
}
