package jmri.jmrit.display.panelEditor.configurexml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import javax.swing.JFrame;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.panelEditor.PanelEditor;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for {@link PanelEditor} panes.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2002
 * @version $Revision$
 */
public class PanelEditorXml extends AbstractXmlAdapter {

    public PanelEditorXml() {
    }

    /**
     * Default implementation for storing the contents of a PanelEditor
     *
     * @param o Object to store, of type PanelEditor
     * @return Element containing the complete info
     */
    public Element store(Object o) {
        PanelEditor p = (PanelEditor) o;
        Element panel = new Element("paneleditor");

        JFrame frame = p.getTargetFrame();
        Dimension size = frame.getSize();
        Point posn = frame.getLocation();

        panel.setAttribute("class", "jmri.jmrit.display.panelEditor.configurexml.PanelEditorXml");
        panel.setAttribute("name", "" + frame.getTitle());
        panel.setAttribute("x", "" + posn.x);
        panel.setAttribute("y", "" + posn.y);
        panel.setAttribute("height", "" + size.height);
        panel.setAttribute("width", "" + size.width);
        panel.setAttribute("editable", "" + (p.isEditable() ? "yes" : "no"));
        panel.setAttribute("positionable", "" + (p.allPositionable() ? "yes" : "no"));
        //panel.setAttribute("showcoordinates", ""+(p.showCoordinates()?"yes":"no"));
        panel.setAttribute("showtooltips", "" + (p.showTooltip() ? "yes" : "no"));
        panel.setAttribute("controlling", "" + (p.allControlling() ? "yes" : "no"));
        panel.setAttribute("hide", p.isVisible() ? "no" : "yes");
        panel.setAttribute("panelmenu", p.isPanelMenuVisible() ? "yes" : "no");
        panel.setAttribute("scrollable", p.getScrollable());
        if (p.getBackgroundColor() != null) {
            panel.setAttribute("redBackground", "" + p.getBackgroundColor().getRed());
            panel.setAttribute("greenBackground", "" + p.getBackgroundColor().getGreen());
            panel.setAttribute("blueBackground", "" + p.getBackgroundColor().getBlue());
        }

        // include contents
        List<Positionable> contents = p.getContents();
        if (log.isDebugEnabled()) {
            log.debug("N elements: " + contents.size());
        }
        for (int i = 0; i < contents.size(); i++) {
            Positionable sub = contents.get(i);
            if (sub != null && sub.storeItem()) {
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel element: " + e);
                    e.printStackTrace();
                }
            }
        }

        return panel;
    }

    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a PanelEditor object, then register and fill it, then pop it in a
     * JFrame
     *
     * @param shared Top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        try {
            x = shared.getAttribute("x").getIntValue();
            y = shared.getAttribute("y").getIntValue();
            height = shared.getAttribute("height").getIntValue();
            width = shared.getAttribute("width").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert PanelEditor's attribute");
            result = false;
        }
        // find the name
        String name = "Panel";
        if (shared.getAttribute("name") != null) {
            name = shared.getAttribute("name").getValue();
        }
        // confirm that panel hasn't already been loaded
        if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
            log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
            result = false;
        }
        PanelEditor panel = new PanelEditor(name);
        //panel.makeFrame(name);
        jmri.jmrit.display.PanelMenu.instance().addEditorPanel(panel);
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);

        panel.setTitle();

        // Load editor option flags. This has to be done before the content 
        // items are loaded, to preserve the individual item settings
        Attribute a;
        boolean value = true;
        if ((a = shared.getAttribute("editable")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllEditable(value);

        value = true;
        if ((a = shared.getAttribute("positionable")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllPositionable(value);

        /*
         value = false;
         if ((a = element.getAttribute("showcoordinates"))!=null && a.getValue().equals("yes"))
         value = true;
         panel.setShowCoordinates(value);
         */
        value = true;
        if ((a = shared.getAttribute("showtooltips")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllShowTooltip(value);

        value = true;
        if ((a = shared.getAttribute("controlling")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllControlling(value);

        value = false;
        if ((a = shared.getAttribute("hide")) != null && a.getValue().equals("yes")) {
            value = true;
        }
        panel.setShowHidden(value);

        value = true;
        if ((a = shared.getAttribute("panelmenu")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setPanelMenuVisible(value);

        String state = "both";
        if ((a = shared.getAttribute("scrollable")) != null) {
            state = a.getValue();
        }
        panel.setScroll(state);

        // set color if needed
        try {
            int red = shared.getAttribute("redBackground").getIntValue();
            int blue = shared.getAttribute("blueBackground").getIntValue();
            int green = shared.getAttribute("greenBackground").getIntValue();
            panel.setBackgroundColor(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }
        //set the (global) editor display widgets to their flag settings
        panel.initView();

        // load the contents with their individual option settings
        List<Element> items = shared.getChildren();
        for (int i = 0; i < items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via " + adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                // and do it
                adapter.load(item, panel);
                if (!panel.loadOK()) {
                    result = false;
                }
            } catch (Exception e) {
                log.error("Exception while loading " + item.getName() + ":" + e);
                result = false;
                e.printStackTrace();
            }
        }
        panel.disposeLoadData();     // dispose of url correction data

        // display the results, with the editor in back
        panel.pack();
        panel.setAllEditable(panel.isEditable());

        // we don't pack the target frame here, because size was specified
        // TODO: Work out why, when calling this method, panel size is increased
        // vertically (at least on MS Windows)
        panel.getTargetFrame().setVisible(true);    // always show the panel

        // register the resulting panel for later configuration
        InstanceManager.configureManagerInstance().registerUser(panel);

        // reset the size and position, in case the display caused it to change
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);
        return result;
    }

    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }

    private final static Logger log = LoggerFactory.getLogger(PanelEditorXml.class.getName());

}
