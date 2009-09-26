package jmri.jmrit.throttle;

import org.jdom.Element;
import java.awt.Container;
import java.awt.Dimension;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;

/**
 * A helper class for getting and setting XML attributes of a JInternalFrame.
 */
public class WindowPreferences
{
    /**
     * Collect JInternalFrame preferences.
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
    public Element getPreferences(JInternalFrame c)
    {
        Element window = getPreferences((Container)c);
        window.setAttribute("isIconified", String.valueOf( c.isIcon() ) );
        return window;
    }

    /**
     * Set JInternalFrame preferences from an XML Element.
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
    public void setPreferences(JInternalFrame c, Element e)
    {
    	setPreferences((Container)c, e);
        try
        {
            if (e.getAttribute("isIconified") != null)
            	c.setIcon( e.getAttribute("isIconified").getBooleanValue() );
        }
        catch (org.jdom.DataConversionException ex) {
            System.out.println(ex);
        } catch (PropertyVetoException ex) {
        	System.out.println(ex);
		} 
    }
    
    /**
     * Collect container preferences.
     * @param c The container being XMLed.
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
        window.setAttribute("x", String.valueOf(c.getLocation().x));
        window.setAttribute("y", String.valueOf(c.getLocation().y));
        Dimension size = c.getSize();
        window.setAttribute("width", String.valueOf(size.width));
        window.setAttribute("height", String.valueOf(size.height));
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
        catch (org.jdom.DataConversionException ex) {
            System.out.println(ex);
        }
    }
}
