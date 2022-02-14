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
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.util.ColorUtil;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for {@link SwitchboardEditor} panes.
 *
 * @author Bob Jacobsen Copyright (c) 2002
 * @author Egbert Broerse Copyright (c) 2017, 2020
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

        panel.setAttribute("class", "jmri.jmrit.display.switchboardEditor.configurexml.SwitchboardEditorXml");
        panel.setAttribute("name", "" + frame.getTitle());
        // size and position are managed by Prefsmanager
        panel.setAttribute("editable", "" + (p.isEditable() ? "yes" : "no"));
        panel.setAttribute("showtooltips", "" + (p.showToolTip() ? "yes" : "no"));
        panel.setAttribute("controlling", "" + (p.allControlling() ? "yes" : "no"));
        panel.setAttribute("hide", p.isVisible() ? "no" : "yes");
        panel.setAttribute("panelmenu", p.isPanelMenuVisible() ? "yes" : "no");
        panel.setAttribute("scrollable", p.getScrollable());
        panel.setAttribute("hideunconnected", "" + (p.hideUnconnected() ? "yes" : "no"));
        panel.setAttribute("autoitemrange", "" + (p.autoItemRange() ? "yes" : "no"));
        panel.setAttribute("rangemin", "" + p.getPanelMenuRangeMin());
        panel.setAttribute("rangemax", "" + p.getPanelMenuRangeMax());
        panel.setAttribute("type", p.getSwitchType());
        panel.setAttribute("connection", p.getSwitchManu());
        panel.setAttribute("shape", p.getSwitchShape());
        panel.setAttribute("rows", "" + p.getRows());
        panel.setAttribute("total", "" + p.getTotal()); // total number of items displayed
        panel.setAttribute("showusername", "" + p.showUserName());
        panel.setAttribute("iconscale", "" + p.getIconScale());
        panel.setAttribute("defaulttextcolor", p.getDefaultTextColor());
        panel.setAttribute("activecolor", p.getActiveSwitchColor()); // fetched directly from Editor by Servlet
        panel.setAttribute("inactivecolor", p.getInactiveSwitchColor()); // user-settable since 4.21.3
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
        Attribute a;
        // find coordinates
        int x = 0;
        int y = 0;
        int height = 400;
        int width = 300;
        int rangemin = 1;
        int rangemax = 32;
        int rows = 4;
        int iconscale = 100;
        String type;
        String connection;
        String shape;
        String name;

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
            log.error("failed to convert Switchboard's attribute");
            result = false;
        }
        // find the name
        name = "Switchboard"; // this will be replaced by the name as stored NOI18N
        if (shared.getAttribute("name") != null) {
            name = shared.getAttribute("name").getValue();
        }
        // confirm that panel hasn't already been loaded
        if (InstanceManager.getDefault(EditorManager.class).contains(name)) {
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
            result = false;
        }

        // If available, override location and size with machine dependent values
        if (!InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).isEditorUseOldLocSize()) {
            jmri.UserPreferencesManager prefsMgr = InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
            if (prefsMgr != null) {

                Point prefsWindowLocation = prefsMgr.getWindowLocation(name);
                if (prefsWindowLocation != null) {
                    x = (int) prefsWindowLocation.getX();
                    y = (int) prefsWindowLocation.getY();
                }

                Dimension prefsWindowSize = prefsMgr.getWindowSize(name);
                if (prefsWindowSize != null && prefsWindowSize.getHeight() != 0 && prefsWindowSize.getWidth() != 0) {
                    height = (int) prefsWindowSize.getHeight();
                    width = (int) prefsWindowSize.getWidth();
                }
            }
        }

        SwitchboardEditor panel = new SwitchboardEditor(name);
        //panel.makeFrame(name);
        InstanceManager.getDefault(EditorManager.class).add(panel);
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);

        panel.setTitle();

        // Load editor option flags. This has to be done before the content
        // items are loaded, to preserve the individual item settings.
        boolean value = true;

        if ((a = shared.getAttribute("editable")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllEditable(value);
        value = (a = shared.getAttribute("showtooltips")) == null || !a.getValue().equals("no");
        panel.setAllShowToolTip(value);
        value = (a = shared.getAttribute("controlling")) == null || !a.getValue().equals("no");
        panel.setAllControlling(value);
        value = (a = shared.getAttribute("hide")) != null && a.getValue().equals("yes");
        panel.setShowHidden(value);
        value = (a = shared.getAttribute("panelmenu")) == null || !a.getValue().equals("no");
        panel.setPanelMenuVisible(value);

        String state = "both";
        if ((a = shared.getAttribute("scrollable")) != null) {
            state = a.getValue();
        }
        panel.setScroll(state);
        value = (a = shared.getAttribute("hideunconnected")) != null && a.getValue().equals("yes");
        panel.setHideUnconnected(value);
        value = (a = shared.getAttribute("autoitemrange")) == null || !a.getValue().equals("no");
        panel.setAutoItemRange(value);

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
        log.debug("SwitchBoard connection choice set to {}", connection);

        shape = shared.getAttribute("shape").getValue();
        panel.setSwitchShape(shape);

        if (shared.getAttribute("columns") != null) {
            try { // migration of old naming, dropped since 4.21.2
                rows = shared.getAttribute("columns").getIntValue();
            } catch (org.jdom2.DataConversionException e) {
                log.error("failed to convert Switchboard's row (formerly column) count");
                result = false;
            }
        }
        if (shared.getAttribute("rows") != null) {
            try {
                rows = shared.getAttribute("rows").getIntValue();
            } catch (org.jdom2.DataConversionException e) {
                log.error("failed to convert Switchboard's row count");
                result = false;
            }
        }
        panel.setRows(rows); // if 0, autoRows is selected (handled in Editor)

        a = shared.getAttribute("showusername");
        if (a == null || a.getValue().equals("yes")) {
            panel.setShowUserName(1); // default and migration
        } else if (a.getValue().equals("no")) {
            panel.setShowUserName(0); // only how system name
        } else {
            panel.setShowUserName(2); // display name (one, prefer user name)
        }

        if (shared.getAttribute("iconscale") != null) {
            try {
                iconscale = shared.getAttribute("iconscale").getIntValue();
            } catch (org.jdom2.DataConversionException e) {
                log.error("failed to convert Switchboard's icon scale setting");
                result = false;
            }
        }
        panel.setIconScale(iconscale);

        Color defaultTextColor = Color.BLACK;
        if (shared.getAttribute("defaulttextcolor") != null) {
            String color = shared.getAttribute("defaulttextcolor").getValue();
            try {
                defaultTextColor = ColorUtil.stringToColor(color);
            } catch (IllegalArgumentException ex) {
                log.error("Invalid defaulttextcolor {} using black", color);
            }
        }
        panel.setDefaultTextColor(defaultTextColor);
        // set color if needed
        try {
            int red = shared.getAttribute("redBackground").getIntValue();
            int blue = shared.getAttribute("blueBackground").getIntValue();
            int green = shared.getAttribute("greenBackground").getIntValue();
            panel.setDefaultBackgroundColor(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }
        // activecolor
        Color activeColor = Color.RED;
        if (shared.getAttribute("activecolor") != null) {
            String color = shared.getAttribute("activecolor").getValue();
            try {
                activeColor = ColorUtil.stringToColor(color);
            } catch (IllegalArgumentException ex) {
                log.error("Invalid activecolor {}, using red", color);
            }
        }
        panel.setDefaultActiveColor(activeColor);
        // inactivecolor
        Color inactiveColor = Color.GREEN;
        if (shared.getAttribute("inactivecolor") != null) {
            String color = shared.getAttribute("inactivecolor").getValue();
            try {
                inactiveColor = ColorUtil.stringToColor(color);
            } catch (IllegalArgumentException ex) {
                log.error("Invalid inactivecolor {}, using green", color);
            }
        }
        panel.setDefaultInactiveColor(inactiveColor);
        // set the (global) editor display widgets to their flag settings
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
                    | jmri.configurexml.JmriConfigureXmlException
                    | java.lang.reflect.InvocationTargetException e) {
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
        panel.getTargetFrame().setVisible(true); // always show the panel

        // register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(panel);
        }

        // reset the size and position, in case the display caused it to change
        panel.getTargetFrame().setLocation(x, y);
        panel.getTargetFrame().setSize(width, height);
        panel.updatePressed(); // required, picks up panel color
        log.debug("Switchboard ready");
        return result;
    }

    @Override
    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }

    private final static Logger log = LoggerFactory.getLogger(SwitchboardEditorXml.class);

}
