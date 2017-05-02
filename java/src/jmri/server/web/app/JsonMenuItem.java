package jmri.server.web.app;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Locale;
import java.util.Objects;
import javax.annotation.Nonnull;
import jmri.server.web.spi.WebMenuItem;

/**
 * A POJO for Web menu items generated from JSON.
 *
 * @author Randall Wood (C) 2016
 */
public class JsonMenuItem implements WebMenuItem {

    public String path;
    public String href = null;
    public String iconClass = null;
    public String title = null;
    public int position = 300;
    public boolean separatedBefore = false;
    public boolean separatedAfter = false;
    public boolean dynamic = false;

    /**
     * Create a menu item from a JSON object.
     *
     * @param node the JSON object containing the menu item
     * @throws java.lang.IllegalArgumentException if node does not contain a
     * <em>path</em> node
     */
    public JsonMenuItem(@Nonnull JsonNode node) throws IllegalArgumentException {
        Objects.requireNonNull(node, "cannot parse null object");
        this.path = node.path("path").asText(null); // NOI18N
        if (this.path == null) {
            throw new IllegalArgumentException("path not specified");
        }
        this.href = node.path("href").asText(this.href); // NOI18N
        this.iconClass = node.path("iconClass").asText(this.iconClass); // NOI18N
        this.title = node.path("title").asText(this.title); // NOI18N
        this.position = node.path("position").asInt(this.position); // NOI18N
        this.separatedBefore = node.path("separatedBefore").asBoolean(this.separatedBefore); // NOI18N
        this.separatedAfter = node.path("separatedAfter").asBoolean(this.separatedAfter); // NOI18N
        this.dynamic = node.path("dynamic").asBoolean(this.dynamic); // NOI18N
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String getHref() {
        return this.href;
    }

    @Override
    public String getIconClass() {
        return this.iconClass;
    }

    @Override
    public String getTitle(Locale locale) {
        if (this.title == null) {
            return Bundle.getMessage(locale, this.getPath());
        }
        return this.title;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public boolean isSeparatedBefore() {
        return this.separatedBefore;
    }

    @Override
    public boolean isSeparatedAfter() {
        return this.separatedAfter;
    }

    @Override
    public boolean isDynamic() {
        return this.dynamic;
    }

}
