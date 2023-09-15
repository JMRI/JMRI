package jmri.server.web.spi;

import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Provide integration for the JMRI web services so servlets can visually
 * integrate into the JMRI web site.
 *
 * @author Randall Wood (C) 2016
 */
public interface WebManifest {

    /**
     * Get the navigation menu items that provide access to the servlet
     * associated with the manifest.
     *
     * @return a set of menu items; provide an empty set if the item should not
     *         be in the navigation menu
     */
    @Nonnull
    Set<WebMenuItem> getNavigationMenuItems();

    /**
     * Get any scripts the servlet associated with the manifest requires in the
     * order required.
     *
     * @return a set of script URLs; provide an empty set if the item needs no
     *         scripts
     */
    @Nonnull
    List<String> getScripts();

    /**
     * Get any CSS style sheets the servlet associated with the manifest
     * requires in the order required.
     *
     * @return a set of style sheet URLs; provide an empty set if the item needs
     *         no style sheets
     */
    @Nonnull
    List<String> getStyles();

    /**
     * Get the Angular dependencies required by the servlet associated with the
     * manifest.
     *
     * @return an ordered list of angular dependencies
     */
    @Nonnull
    List<String> getAngularDependencies();

    /**
     * Get the Angular routes supported by the servlet associated with the
     * manifest.
     *
     * @return a map of angular path to angular routing instructions
     */
    @Nonnull
    Set<AngularRoute> getAngularRoutes();

    /**
     * Get the sources for the Angular module components required by the servlet
     * associated with the manifest.
     *
     * @return a list of sources to include in the web app
     */
    @Nonnull
    List<URL> getAngularSources();

    /**
     * Get the paths for JSON translation dictionaries to pre-load. If
     * translation dictionaries exist, but not for the requested Locale,
     * fallback onto the requested language, and, if that is also not available,
     * to the English language with no country specified.
     *
     * @param locale the requested locale for the translations
     * @return a list of translation sources
     */
    Set<URI> getPreloadedTranslations(Locale locale);
}
