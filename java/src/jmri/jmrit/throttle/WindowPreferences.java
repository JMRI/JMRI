package jmri.jmrit.throttle;

import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyVetoException;
import javax.swing.JInternalFrame;
import org.jdom2.Element;

/**
 * A helper class for getting and setting XML attributes of a JInternalFrame.
 */
public class WindowPreferences {

    /**
     * Collect JInternalFrame preferences.
     *
     * @param c The JInternalFrame being XMLed.
     * @return An Element containing the following prefs:
     * <ul>
     * <li> x location
     * <li> y location
     * <li> width
     * <li> height
     * <li> isIcon
     * </ul>
     */
    public static Element getPreferences(JInternalFrame c) {
        Element window = getPreferences((Container) c);
        window.setAttribute("isIconified", String.valueOf(c.isIcon()));
        return window;
    }

    /**
     * Set JInternalFrame preferences from an XML Element.
     *
     * @param c The JInternalFrame being set.
     * @param e An Element containing the following prefs:
     * <ul>
     * <li> x location
     * <li> y location
     * <li> width
     * <li> height
     * <li> isIcon
     * </ul>
     */
    public static void setPreferences(JInternalFrame c, Element e) {
        setPreferences((Container) c, e);
        try {
            if (e.getAttribute("isIconified") != null) {
                c.setIcon(e.getAttribute("isIconified").getBooleanValue());
            }
        } catch (org.jdom2.DataConversionException | PropertyVetoException ex) {
            log.warn("Exception setting preferences", ex);
        }
    }

    /**
     * Collect container preferences.
     *
     * @param c The container being XMLed.
     * @return An Element containing the following prefs:
     * <ul>
     * <li> x location
     * <li> y location
     * <li> width
     * <li> height
     * </ul>
     */
    public static Element getPreferences(Container c) {
        Element window = new Element("window");
        window.setAttribute("x", String.valueOf(c.getLocation().x));
        window.setAttribute("y", String.valueOf(c.getLocation().y));
        Dimension size = c.getSize();
        window.setAttribute("width", String.valueOf(size.width));
        window.setAttribute("height", String.valueOf(size.height));
        window.setAttribute("isVisible", String.valueOf(c.isVisible()));
        return window;
    }

    /**
     * Set Container preferences from an XML Element.
     *
     * @param c The Container being set.
     * @param e An Element containing the following prefs:
     * <ul>
     * <li> x location
     * <li> y location
     * <li> width
     * <li> height
     * </ul>
     */
    public static void setPreferences(Container c, Element e, boolean ignorePosition) {
        try {
            int x = e.getAttribute("x").getIntValue();
            int y = e.getAttribute("y").getIntValue();
            int width = e.getAttribute("width").getIntValue();
            int height = e.getAttribute("height").getIntValue();
            if (!ignorePosition) {
                c.setLocation(x, y);
            }
            c.setSize(width, height);
            if (e.getAttribute("isVisible") != null) {
                c.setVisible(e.getAttribute("isVisible").getBooleanValue());
            }
        } catch (org.jdom2.DataConversionException ex) {
            log.warn("Exception setting preferences", ex);
        }
    }

    public static void setPreferences(Container c, Element e) {
        setPreferences(c, e, false);
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(WindowPreferences.class);
}
