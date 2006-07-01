package jmri.jmrit.throttle;

import org.jdom.Element;
import java.awt.Container;

/**
 * A helper class for getting and setting XML attributes of a Container.
 */
public class WindowPreferences
{
    /**
     * Collect Container preferences.
     * @param c The Container being XMLed.
     * @return An Element containing the following prefs:
     * <ul>
     * <li> x location
     * <li> y location
     * <li> width
     * <li> height
     * </ul>
     */
    public Element getPreferences(Container c)
    {
        Element window = new Element("window");
        window.addAttribute("x", String.valueOf(c.getLocation().x));
        window.addAttribute("y", String.valueOf(c.getLocation().y));
        window.addAttribute("width", String.valueOf(c.getWidth()));
        window.addAttribute("height", String.valueOf(c.getHeight()));
        return window;
    }

    /**
     * Set Container preferences from an XML Element.
     * @param c The Container being set.
     * @param e An Element containing the following prefs:
     * <ul>
     * <li> x location
     * <li> y location
     * <li> width
     * <li> height
     * </ul>
     */
    public void setPreferences(Container c, Element e)
    {
        try
        {
            int x = e.getAttribute("x").getIntValue();
            int y = e.getAttribute("y").getIntValue();
            int width = e.getAttribute("width").getIntValue();
            int height = e.getAttribute("height").getIntValue();
            c.setLocation(x, y);
            c.setSize(width, height);
        }
        catch (org.jdom.DataConversionException ex)
        {
            System.out.println(ex);
        }
    }
}