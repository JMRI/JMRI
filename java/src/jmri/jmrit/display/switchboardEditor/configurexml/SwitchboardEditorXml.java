package jmri.jmrit.display.switchboardEditor.configurexml;

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
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for {@link SwitchboardEditor} panes.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Egbert Broerse Copyright (c) 2017
 */
public class SwitchboardEditorXml extends AbstractXmlAdapter {

    public SwitchboardEditorXml() {
    }

    /**
     * Default implementation for storing the contents of a SwitchboardEditor.
     * Storing of beanswitch properties for use on web panel
     * {@link SwitchboardEditorXml}
     *
     * @param o Object to store, of type SwitchboardEditor
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        SwitchboardEditor p = (SwitchboardEditor) o;
        Element panel = new Element("switchboardeditor");

        JFrame frame = p.getTargetFrame();
        Dimension size = frame.getSize();
        Point posn = frame.getLocation();

        panel.setAttribute("class", "jmri.jmrit.display.switchboardEditor.configurexml.SwitchboardEditorXml");
        panel.setAttribute("name", "" + frame.getTitle());
        panel.setAttribute("x", "" + posn.x);
        panel.setAttribute("y", "" + posn.y);
        panel.setAttribute("height", "" + size.height);
        panel.setAttribute("width", "" + size.width);
        panel.setAttribute("editable", "" + (p.isEditable() ? "yes" : "no"));
        panel.setAttribute("showtooltips", "" + (p.showToolTip() ? "yes" : "no"));
        panel.setAttribute("controlling", "" + (p.allControlling() ? "yes" : "no"));
        panel.setAttribute("hide", p.isVisible() ? "no" : "yes");
        panel.setAttribute("panelmenu", p.isPanelMenuVisible() ? "yes" : "no");
        panel.setAttribute("scrollable", p.getScrollable());
        panel.setAttribute("hideunconnected", "" + (p.hideUnconnected() ? "yes" : "no"));
        panel.setAttribute("rangemin", "" + p.getPanelMenuRangeMin());
        panel.setAttribute("rangemax", "" + p.getPanelMenuRangeMax());
        panel.setAttribute("type", p.getSwitchType());
        panel.setAttribute("connection", p.getSwitchManu());
        panel.setAttribute("shape", p.getSwitchShape());
        panel.setAttribute("columns", "" + p.getColumns());
        panel.setAttribute("defaulttextcolor", p.getDefaultTextColor());
        if (p.getBackgroundColor() != null) {
            panel.setAttribute("redBackground", "" + p.getBackgroundColor().getRed());
            panel.setAttribute("greenBackground", "" + p.getBackgroundColor().getGreen());
            panel.setAttribute("blueBackground", "" + p.getBackgroundColor().getBlue());
        }

        // include contents (not used to store Switchboards on disk as
        // all config is stored at Panel level).
        return panel;
    }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a SwitchboardEditor object, then register and fill it, then pop it
     * in a JFrame.
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
        int rangemin = 1;
        int rangemax = 32;
        int columns = 4;
        String type = "T";
        String connection = "I";
        String shape = "key";
        String name;

        try {
            x = shared.getAttribute("x").getIntValue();
            y = shared.getAttribute("y").getIntValue();
            height = shared.getAttribute("height").getIntValue();
            width = shared.getAttribute("width").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert Switchboard's attribute");
            result = false;
        }
        // find the name
        name = "Switchboard"; // this will be replaced by the name as stored NOI18N
        if (shared.getAttribute("name") != null) {
            name = shared.getAttribute("name").getValue();
        }
        // confirm that panel hasn't already been loaded
        if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
            log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
            result = false;
        }
        SwitchboardEditor panel = new SwitchboardEditor(name);
        //panel.makeFrame(name);
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(panel);
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);

        panel.setTitle();

        // Load editor option flags. This has to be done before the content
        // items are loaded, to preserve the individual item settings.
        Attribute a;
        boolean value = true;

        if ((a = shared.getAttribute("editable")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllEditable(value);

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

        value = false;
        if ((a = shared.getAttribute("hideunconnected")) != null && a.getValue().equals("yes")) {
            value = true;
        }
        panel.setHideUnconnected(value);

        try {
            rangemin = shared.getAttribute("rangemin").getIntValue();
            rangemax = shared.getAttribute("rangemax").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert Switchboard's range");
            result = false;
        }
        panel.setPanelMenuRangeMin(rangemin);
        panel.setPanelMenuRangeMax(rangemax);

        type = shared.getAttribute("type").getValue();
        panel.setSwitchType(type);

        connection = shared.getAttribute("connection").getValue();
        panel.setSwitchManu(connection);

        shape = shared.getAttribute("shape").getValue();
        panel.setSwitchShape(shape);

        try {
            columns = shared.getAttribute("columns").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert Switchboard's column count");
            result = false;
        }
        panel.setColumns(columns);

        String defaultTextColor = "black";
        if (shared.getAttribute("defaulttextcolor") != null) {
            defaultTextColor = shared.getAttribute("defaulttextcolor").getValue();
        }
        panel.setDefaultTextColor(defaultTextColor);
        // set color if needed
        try {
            int red = shared.getAttribute("redBackground").getIntValue();
            int blue = shared.getAttribute("blueBackground").getIntValue();
            int green = shared.getAttribute("greenBackground").getIntValue();
            //panel.setBackground(new Color(red, green, blue));
            panel.setDefaultBackgroundColor(new Color(red, green, blue));
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
            log.debug("load via {}", adapterName);
            try {
                XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).newInstance();
                // and do it
                adapter.load(item, panel);
                if (!panel.loadOK()) {
                    result = false;
                }
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException
                    | jmri.configurexml.JmriConfigureXmlException
                    | RuntimeException e) {
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
        panel.updatePressed();
        return result;
    }

    @Override
    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditorXml.class);

}
