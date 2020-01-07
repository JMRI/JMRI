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
    public final String version;

    public JsonRequest(@Nonnull Locale locale, @Nonnull String version, int id) {
        Objects.requireNonNull(locale, "Locale must be non-null");
        Objects.requireNonNull(version, "Version must be specified");
        this.locale = locale;
        this.version = version;
        this.id = id;
    }
}
