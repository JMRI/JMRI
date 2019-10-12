package jmri.jmrit.display.panelEditor.configurexml;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.List;
import javax.swing.JFrame;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.display.PanelMenu;
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
    @Override
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
        panel.setAttribute("showtooltips", "" + (p.showToolTip() ? "yes" : "no"));
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
        for (Positionable sub : contents) {
            if (sub != null && sub.storeItem()) {
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (RuntimeException e) {
                    log.error("Error storing panel element", e);
                }
            }
        }

        return panel;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a PanelEditor object, then register and fill it, then pop it in a
     * JFrame.
     *
     * @param shared Top level Element to unpack.
     * @return true if successful
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        Attribute a;
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        try {
            if ((a = shared.getAttribute("x")) != null) {
                x = a.getIntValue();
            }
            if ((a = shared.getAttribute("y")) != null) {
                y = a.getIntValue();
            }
            if ((a = shared.getAttribute("height")) != null) {
                height = a.getIntValue();
            }
            if ((a = shared.getAttribute("width")) != null) {
                width = a.getIntValue();
            }
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
        if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
            result = false;
        }

        // If available, override location and size with machine dependent values
        if (!InstanceManager.getDefault(apps.gui.GuiLafPreferencesManager.class).isEditorUseOldLocSize()) {
            jmri.UserPreferencesManager prefsMgr = InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
            if (prefsMgr != null) {

                java.awt.Point prefsWindowLocation = prefsMgr.getWindowLocation(name);
                if (prefsWindowLocation != null) {
                    x = (int) prefsWindowLocation.getX();
                    y = (int) prefsWindowLocation.getY();
                }

                java.awt.Dimension prefsWindowSize = prefsMgr.getWindowSize(name);
                if (prefsWindowSize != null && prefsWindowSize.getHeight() != 0 && prefsWindowSize.getWidth() != 0) {
                    height = (int) prefsWindowSize.getHeight();
                    width = (int) prefsWindowSize.getWidth();
                }
            }
        }

        PanelEditor panel = new PanelEditor(name);
        panel.setTitle();
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(panel);

        // Load editor option flags. This has to be done before the content
        // items are loaded, to preserve the individual item settings
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
        panel.setAllShowToolTip(value);

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
        List<Element> panelItems = shared.getChildren();
        for (Element item : panelItems) {
            // get the class, hence the adapter object to do loading
            String adapterName = item.getAttribute("class").getValue();
            log.debug("load via {}", adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).getDeclaredConstructor().newInstance();
                // and do it
                adapter.load(item, panel);
                if (!panel.loadOK()) {
                    result = false;
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
                    | jmri.configurexml.JmriConfigureXmlException | java.lang.reflect.InvocationTargetException e) {
                log.error("Exception while loading {}", item.getName(), e);
                result = false;
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
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(panel);
        }

        // reset the size and position, in case the display caused it to change
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);
        return result;
    }

    @Override
    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }

    private final static Logger log = LoggerFactory.getLogger(PanelEditorXml.class);

}
