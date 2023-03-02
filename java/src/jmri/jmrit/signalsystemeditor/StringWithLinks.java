package jmri.jmrit.signalsystemeditor;

import java.util.ArrayList;
import java.util.List;

/**
 * A string that might contain URL links
 *
 * @author Daniel Bergqvist (C) 2022
 */
public class StringWithLinks {

    private final List<String> _strings = new ArrayList<>();
    private final List<Link> _links = new ArrayList<>();

    public List<String> getStrings() {
        return this._strings;
    }

    public List<Link> getLinks() {
        return this._links;
    }


    public static class Link {

        private String _name;
        private String _href;

        public Link(String name, String href) {
            this._name = name;
            this._href = href;
        }

        public void setName(String name) {
            this._name = name;
        }

        public String getName() {
            return this._name;
        }

        public void setHref(String href) {
            this._href = href;
        }

        public String getHref() {
            return this._href;
        }

    }
}
