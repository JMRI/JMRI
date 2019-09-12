package jmri.server.web.spi;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 *
 * @author Randall Wood (C) 2017
 */
public final class AngularRoute {

    private final String when;
    private final String template;
    private final String redirection;
    private final String controller;

    /**
     * Create an AngularJS route.
     *
     * @param when        the trigger for the route
     * @param template    the template loaded for the route; must be non-null if
     *                    controller is non-null
     * @param controller  the controller loaded for the route; must be non-null
     *                    if template is non-null
     * @param redirection the path to redirect the route to; must be non-null if
     *                    template and controller is null; must be null if
     *                    template and controller is non-null
     * @throws NullPointerException     if when is null
     * @throws IllegalArgumentException if any of the rules concerning when
     *                                  template, controller, and redirection
     *                                  must be non-null are violated
     */
    public AngularRoute(@Nonnull String when, @CheckForNull String template, @CheckForNull String controller, @CheckForNull String redirection) {
        Objects.requireNonNull(when, "Unable to create AngularRoute with null when property.");
        if ((template == null && controller != null) || (template != null && controller == null)) {
            throw new IllegalArgumentException("template and controller must both be non-null or null");
        }
        if ((redirection != null && template != null)
                || (redirection == null && template == null)) {
            throw new IllegalArgumentException("redirection must be null if template or controller is non-null");
        }
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

    /**
     * Get the HTML template for the route.
     *
     * @return the template or null
     */
    @CheckForNull
    public String getTemplate() {
        return this.template;
    }

    /**
     * Get the AngularJS controller for the route.
     *
     * @return the controller or null
     */
    @CheckForNull
    public String getController() {
        return this.controller;
    }

}
