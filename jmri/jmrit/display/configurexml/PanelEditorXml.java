package jmri.jmrit.display.configurexml;

import jmri.InstanceManager;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.PanelEditor;
import java.awt.Dimension;
import java.awt.Point;

import javax.swing.JFrame;
import javax.swing.JLayeredPane;

import com.sun.java.util.collections.List;
import org.jdom.*;

/**
 * Handle configuration for {@link PanelEditor} panes.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision: 1.18 $
 */
public class PanelEditorXml implements XmlAdapter {

    public PanelEditorXml() {}

    /**
     * Default implementation for storing the contents of a
     * PanelEditor
     * @param o Object to store, of type PanelEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PanelEditor p = (PanelEditor)o;
        Element panel = new Element("paneleditor");

        Dimension size = p.getTarget().getSize();
        Point posn = p.getFrame().getLocation();

        panel.addAttribute("class", "jmri.jmrit.display.configurexml.PanelEditorXml");
        panel.addAttribute("name", ""+p.getFrame().getTitle());
        panel.addAttribute("x", ""+posn.x);
        panel.addAttribute("y", ""+posn.y);
        panel.addAttribute("height", ""+size.height);
        panel.addAttribute("width", ""+size.width);
        panel.addAttribute("editable", ""+(p.isEditable()?"yes":"no"));
        panel.addAttribute("positionable", ""+(p.isPositionable()?"yes":"no"));
        panel.addAttribute("controlling", ""+(p.isControlling()?"yes":"no"));
        panel.addAttribute("hide", p.isVisible()?"no":"yes");

        // include contents

        if (log.isDebugEnabled()) log.debug("N elements: "+p.contents.size());
        for (int i=0; i<p.contents.size(); i++) {
            Object sub = p.contents.get(i);
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e!=null) panel.addContent(e);
            } catch (Exception e) {
                log.error("Error storing panel element: "+e);
                e.printStackTrace();
            }
        }

        return panel;
    }


    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a PanelEditor object, then
     * register and fill it, then pop it in a JFrame
     * @param element Top level Element to unpack.
     */
    public void load(Element element) {
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        try {
            x = element.getAttribute("x").getIntValue();
            y = element.getAttribute("y").getIntValue();
            height = element.getAttribute("height").getIntValue();
            width = element.getAttribute("width").getIntValue();
        } catch ( org.jdom.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
        }
        // find the name
        String name = "Panel";
        if (element.getAttribute("name")!=null)
            name = element.getAttribute("name").getValue();
        // create the objects
        PanelEditor panel = new PanelEditor();
        panel.makeFrame(name, width, height);
        panel.getFrame().setLocation(x,y);

        panel.setTitle();

        // load the contents
        List items = element.getChildren();
        for (int i = 0; i<items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = (Element)items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via "+adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter)Class.forName(adapterName).newInstance();
                // and do it
                adapter.load(item, panel);
            } catch (Exception e) {
                log.error("Exception while loading "+item.getName()+":"+e);
                e.printStackTrace();
            }
        }

        // set contents state
        Attribute a;
        boolean value = true;
        if ((a = element.getAttribute("editable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllEditable(value);

        value = true;
        if ((a = element.getAttribute("positionable"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllPositionable(value);

        value = true;
        if ((a = element.getAttribute("controlling"))!=null && a.getValue().equals("no"))
            value = false;
        panel.setAllControlling(value);

        boolean hide = false;
        if ((a = element.getAttribute("hide"))!=null && a.getValue().equals("yes"))
            hide = true;

        // display the results, with the editor in back
        panel.pack();

        if (!hide) panel.show();
        panel.getFrame().pack();
        panel.getFrame().show();

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(panel);

    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(PanelEditorXml.class.getName());

}