package jmri.jmrit.throttle;

import org.jdom.Element;
import java.awt.Container;

public class WindowPreferences
{
    public Element getPreferences(Container c)
    {
        Element window = new Element("window");
        window.addAttribute("x", String.valueOf(c.getLocation().x));
        window.addAttribute("y", String.valueOf(c.getLocation().y));
        window.addAttribute("width", String.valueOf(c.getWidth()));
        window.addAttribute("height", String.valueOf(c.getHeight()));
        return window;
    }

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