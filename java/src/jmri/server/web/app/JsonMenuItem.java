package jmri.server.web.app;

import java.util.Locale;
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
