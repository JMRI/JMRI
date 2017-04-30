package jmri.server.web.spi;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 *
 * @author Randall Wood (C) 2017
 */
public class AngularRoute {

    private final String when;
    private final String template;
    private final String redirection;
    private final String controller;

    public AngularRoute(@Nonnull String when, @Nullable String template, @Nullable String controller, @Nullable String redirection) {
        Objects.requireNonNull(when, "Unable to create AngularRoute with null when property.");
        this.when = when;
        this.template = template;
        this.redirection = redirection;
        this.controller = controller;
    }

    @CheckForNull
    public String getRedirection() {
        return this.redirection;
    }

    @Nonnull
    public String getWhen() {
        return this.when;
    }

    @CheckForNull
    public String getTemplate() {
        return this.template;
    }
    
    @CheckForNull
    public String getController() {
        return this.controller;
    }

}
