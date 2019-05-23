package jmri.util.jdom;

import java.util.Locale;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.Namespace;

/**
 * Select XML content based on current Locale.
 *
 * Tries locale and country separated by an underscore, language, and then uses
 * the default Locale.
 *
 * _tlh is treated as a special case, for the ant locale target
 *
 * @author Bob Jacobsen Copyright 2010
 * @since 2.9.3
 */
public class LocaleSelector {

    static String[] suffixes
            = new String[]{
                Locale.getDefault().getLanguage() + "_" + Locale.getDefault().getCountry(),
                Locale.getDefault().getLanguage(),
                ""
            };

    static boolean testLocale = Locale.getDefault().getLanguage().equals("tlh");

    /**
     * Return the value of an attribute for the current locale.
     * <p>
     * {@code <foo temp="a">}
     * {@code <temp xml:lang="hh">b</temp>}
     * {@code </foo>}
     * <p>
     * Say it's looking for the attribute ATT. For each possible suffix, it
     * first looks for a contained element named ATT with an XML 'lang'
     * attribute for the suffix. If so, it takes that value. If none are found,
     * the attribute value is taken from the original element.
     *
     * @param el   the element with the attribute or child element
     * @param name the name of the attribute or child element
     * @return the value of the attribute or null
     */
    static public String getAttribute(Element el, String name) {
        String retval;
        // look for each suffix first
        for (String suffix : suffixes) {
            retval = checkElement(el, name, suffix);
            if (retval != null) {
                if (testLocale) {
                    return retval.toUpperCase(); // the I18N test case, return string in upper case
                }
                return retval;
            }
        }

        // try default element
        for (Object obj : el.getChildren(name)) {
            Element e = (Element) obj;
            Attribute a = e.getAttribute("lang", Namespace.XML_NAMESPACE);
            if (a == null) {
                // default element
                if (testLocale) {
                    return e.getText().toUpperCase(); // the I18N test case, return string in upper case
                }
                return e.getText();
            }
        }

        // failed, go back to original attribute
        Attribute a = el.getAttribute(name);
        if (a == null) {
            return null;
        }
        if (testLocale) {
            return a.getValue().toUpperCase(); // the I18N test case, return string in upper case
        }
        return a.getValue();
    }

    /**
     * Checks one element to see if it's the one for the current language else
     * returns null.
     *
     * @param el     the element
     * @param name   the attribute
     * @param suffix the locale
     * @return the value of the attribute or null
     */
    static String checkElement(Element el, String name, String suffix) {
        for (Object obj : el.getChildren(name)) {
            Element e = (Element) obj;
            Attribute a = e.getAttribute("lang", Namespace.XML_NAMESPACE);
            if (a != null) {
                if (a.getValue().equals(suffix)) {
                    return e.getText();
                }
            }
        }
        return null;
    }

}
