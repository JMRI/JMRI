package jmri.server.web.spi;

import java.util.Locale;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Provide a menu item used in the navigation bar on the JMRI web server.
 * <p>
 * <strong>Note:</strong> the results of two or more WebMenuItems having the
 * same path is undefined.
 *
 * @author Randall Wood (C) 2016
 */
public interface WebMenuItem {

    /**
     * Get the path to the menu item. This should be the same regardless of
     * locale. Use forward slashes ({@literal / }) to separate menu items to
     * create sub menus. Menu items will have the id {@code navbar-&lt;path&gt;}
     * <p>
     * Primary menu items will not have a separator.
     *
     * @return the path to the menu item
     */
    @Nonnull
    public String getPath();

    /**
     * Get the URL for the menu item. This may be an absolute URL path in the
     * JMRI web service, a URL that resolves to some other public location, or a
     * JavaScript trigger. If null, the menu item will not have a link. If the
     * HREF starts with {@literal ng-click:}, it will be treated as a JavaScript
     * trigger instead of a URL.
     *
     * @return the hyper-reference or null if the item is not a link
     */
    @CheckForNull
    public String getHref();

    /**
     * Get the icon for the menu item. This icon needs to be the class
     * attributes for the HTML span element that contains the icon. It is
     * recommended that this icon be one of the
     * <a href="http://fontawesome.io/icons/">Font Awesome</a> or
     * <a href="http://www.patternfly.org/styles/icons/#_">Patternfly</a> icons
     * as these icon sets will be available.
     * <p>
     * Icons will only be displayed for items in the primary menu.
     *
     * @return the class(es) for the icon or null if no icon is to be used
     */
    @CheckForNull
    public String getIconClass();

    /**
     * Get the title for the menu item. This is displayed in the menu.
     *
     * @param locale the client locale
     * @return the localized title
     */
    @Nonnull
    public String getTitle(@Nonnull Locale locale);

    /**
     * The relative position of the menu item. If two menu items have the same
     * relative position, they will be sorted in order by the path. For items
     * within a sub menu, this position only applies to the sub menu.
     *
     * @return the relative position
     */
    public int getPosition();

    /**
     * Indicate if the menu item should have a separator before it. Note that if
     * there are multiple items with the same position, they will be grouped on
     * the same side of the separator if any of the items requires a separator.
     *
     * @return true if there should be a separator before the item; false
     *         otherwise
     */
    public boolean isSeparatedBefore();

    /**
     * Indicate if the menu item should have a separator after it. Note that if
     * there are multiple items with the same position, they will be grouped on
     * the same side of the separator if any of the items requires a separator.
     *
     * @return true if there should be a separator after the item; false
     *         otherwise
     */
    public boolean isSeparatedAfter();

    /**
     * Indicate if the menu item is the anchor for a dynamic menu. A dynamic
     * menu is one that is built on as needed, often by JavaScript executed on
     * the client.
     *
     * @return true if the menu item is an anchor for a dynamic menu; false
     *         otherwise
     */
    public boolean isDynamic();

}
