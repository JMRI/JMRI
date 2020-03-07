package jmri.server.json;

import java.util.Locale;
import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Container for data in JSON request.
 */
public class JsonRequest {

    public final int id;
    public final Locale locale;
    public final String method;
    public final String version;

    /**
     * Create a JSON request container.
     *
     * @param locale  the request locale
     * @param version the JSON version to use
     * @param method  the JSON method to use
     * @param id      the ID of the request
     */
    public JsonRequest(@Nonnull Locale locale, @Nonnull String version, @Nonnull String method, int id) {
        Objects.requireNonNull(locale, "Locale must be non-null");
        Objects.requireNonNull(version, "Version must be specified");
        this.locale = locale;
        this.version = version;
        this.method = method;
        this.id = id;
    }
}
