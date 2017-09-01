package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jmri.ConfigureManager;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.dispatcher.DispatcherFrame;
import jmri.jmrit.display.PanelMenu;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.util.ColorUtil;
import org.jdom2.Attribute;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for LayoutEditor panes.
 * <p>
 * Based in part on PanelEditorXml.java
 *
 * @author Dave Duchamp Copyright (c) 2007
 */
public class LayoutEditorXml extends AbstractXmlAdapter {

    public LayoutEditorXml() {
    }

    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    /**
     * Default implementation for storing the contents of a LayoutEditor
     *
     * @param o Object to store, of type LayoutEditor
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        LayoutEditor p = (LayoutEditor) o;

        Element panel = new Element("LayoutEditor");

        panel.setAttribute("class", getClass().getName());
        panel.setAttribute("name", p.getLayoutName());
        panel.setAttribute("x", "" + p.getUpperLeftX());
        panel.setAttribute("y", "" + p.getUpperLeftY());
        // From this version onwards separate sizes for window and panel are stored the
        // following two statements allow files written here to be read in 2.2 and before
        panel.setAttribute("height", "" + p.getLayoutHeight());
        panel.setAttribute("width", "" + p.getLayoutWidth());
        // From this version onwards separate sizes for window and panel are stored
        panel.setAttribute("windowheight", "" + p.getWindowHeight());
        panel.setAttribute("windowwidth", "" + p.getWindowWidth());
        panel.setAttribute("panelheight", "" + p.getLayoutHeight());
        panel.setAttribute("panelwidth", "" + p.getLayoutWidth());
        panel.setAttribute("sliders", "" + (p.getScroll() ? "yes" : "no")); // deprecated
        panel.setAttribute("scrollable", "" + p.getScrollable());
        panel.setAttribute("editable", "" + (p.isEditable() ? "yes" : "no"));
        panel.setAttribute("positionable", "" + (p.allPositionable() ? "yes" : "no"));
        panel.setAttribute("controlling", "" + (p.allControlling() ? "yes" : "no"));
        panel.setAttribute("animating", "" + (p.isAnimating() ? "yes" : "no"));
        panel.setAttribute("showhelpbar", "" + (p.getShowHelpBar() ? "yes" : "no"));
        panel.setAttribute("drawgrid", "" + (p.getDrawGrid() ? "yes" : "no"));
        panel.setAttribute("snaponadd", "" + (p.getSnapOnAdd() ? "yes" : "no"));
        panel.setAttribute("snaponmove", "" + (p.getSnapOnMove() ? "yes" : "no"));
        panel.setAttribute("antialiasing", "" + (p.getAntialiasingOn() ? "yes" : "no"));
        panel.setAttribute("turnoutcircles", "" + (p.getTurnoutCircles() ? "yes" : "no"));
        panel.setAttribute("tooltipsnotedit", "" + (p.getTooltipsNotEdit() ? "yes" : "no"));
        panel.setAttribute("tooltipsinedit", "" + (p.getTooltipsInEdit() ? "yes" : "no"));
        panel.setAttribute("mainlinetrackwidth", "" + p.getMainlineTrackWidth());
        panel.setAttribute("xscale", Float.toString((float) p.getXScale()));
        panel.setAttribute("yscale", Float.toString((float) p.getYScale()));
        panel.setAttribute("sidetrackwidth", "" + p.getSideTrackWidth());
        panel.setAttribute("defaulttrackcolor", p.getDefaultTrackColor());
        panel.setAttribute("defaultoccupiedtrackcolor", p.getDefaultOccupiedTrackColor());
        panel.setAttribute("defaultalternativetrackcolor", p.getDefaultAlternativeTrackColor());
        panel.setAttribute("defaulttextcolor", p.getDefaultTextColor());
        panel.setAttribute("turnoutcirclecolor", p.getTurnoutCircleColor());
        panel.setAttribute("turnoutcirclesize", "" + p.getTurnoutCircleSize());
        panel.setAttribute("turnoutdrawunselectedleg", (p.getTurnoutDrawUnselectedLeg() ? "yes" : "no"));
        panel.setAttribute("turnoutbx", Float.toString((float) p.getTurnoutBX()));
        panel.setAttribute("turnoutcx", Float.toString((float) p.getTurnoutCX()));
        panel.setAttribute("turnoutwid", Float.toString((float) p.getTurnoutWid()));
        panel.setAttribute("xoverlong", Float.toString((float) p.getXOverLong()));
        panel.setAttribute("xoverhwid", Float.toString((float) p.getXOverHWid()));
        panel.setAttribute("xovershort", Float.toString((float) p.getXOverShort()));
        panel.setAttribute("autoblkgenerate", "" + (p.getAutoBlockAssignment() ? "yes" : "no"));
        if (p.getBackgroundColor() != null) {
            panel.setAttribute("redBackground", "" + p.getBackgroundColor().getRed());
            panel.setAttribute("greenBackground", "" + p.getBackgroundColor().getGreen());
            panel.setAttribute("blueBackground", "" + p.getBackgroundColor().getBlue());
        }
        panel.setAttribute("gridSize", "" + p.getGridSize());
        panel.setAttribute("gridSize2nd", "" + p.getGridSize2nd());

        p.resetDirty();
        panel.setAttribute("openDispatcher", p.getOpenDispatcherOnLoad() ? "yes" : "no");
        panel.setAttribute("useDirectTurnoutControl", p.getDirectTurnoutControl() ? "yes" : "no");

        // note: moving zoom attribute into per-window user preference
        //panel.setAttribute("zoom", Double.toString(p.getZoom()));
        int num;

        // include contents (Icons and Labels)
        List<Positionable> contents = p.getContents();
        for (Positionable sub : contents) {
            if (sub != null && sub.storeItem()) {
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing contents element: " + e);
                }
            } else {
                log.warn("Null entry found when storing panel contents.");
            }
        }

        // include LayoutTurnouts
        num = p.turnoutList.size();
        if (log.isDebugEnabled()) {
            log.debug("N layoutturnout elements: " + num);
        }

        for (Object sub : p.turnoutList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing layoutturnout element: " + e);
            }
        }

        // include TrackSegments
        num = p.trackList.size();
        if (log.isDebugEnabled()) {
            log.debug("N tracksegment elements: " + num);
        }

        for (Object sub : p.trackList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing tracksegment element: " + e);
            }
        }

        // include PositionablePoints
        num = p.pointList.size();
        if (log.isDebugEnabled()) {
            log.debug("N positionablepoint elements: " + num);
        }

        for (Object sub : p.pointList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing positionalpoint element: " + e);
            }
        }

        // include LevelXings
        num = p.xingList.size();
        if (log.isDebugEnabled()) {
            log.debug("N levelxing elements: " + num);
        }

        for (Object sub : p.xingList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing levelxing element: " + e);
            }
        }

        // include LayoutSlips
        num = p.slipList.size();
        if (log.isDebugEnabled()) {
            log.debug("N layoutSlip elements: " + num);
        }

        for (Object sub : p.slipList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing layoutSlip element: " + e);
            }
        }

        // include LayoutTurntables
        num = p.turntableList.size();
        if (log.isDebugEnabled()) {
            log.debug("N turntable elements: " + num);
        }

        for (Object sub : p.turntableList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing turntable element: " + e);
            }
        }
        return panel;
    }   // store

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a LayoutEditor object, then register and fill it, then pop it in a
     * JFrame
     *
     * @param shared Top level Element to unpack.
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        Attribute a;
        // find coordinates
        int x = 0;
        int y = 0;
        // From this version onwards separate sizes for window and panel are used
        int windowHeight = 400;
        int windowWidth = 300;
        int panelHeight = 340;
        int panelWidth = 280;
        int sidetrackwidth = 3;
        int mainlinetrackwidth = 3;
        try {
            x = shared.getAttribute("x").getIntValue();
            y = shared.getAttribute("y").getIntValue();

            // For compatibility with previous versions, try and
            // see if height and width tags are contained in the file
            if ((a = shared.getAttribute("height")) != null) {
                windowHeight = a.getIntValue();
                panelHeight = windowHeight - 60;
            }
            if ((a = shared.getAttribute("width")) != null) {
                windowWidth = a.getIntValue();
                panelWidth = windowWidth - 18;
            }

            // For files created by the new version, 
            // retrieve window and panel sizes
            if ((a = shared.getAttribute("windowheight")) != null) {
                windowHeight = a.getIntValue();
            }
            if ((a = shared.getAttribute("windowwidth")) != null) {
                windowWidth = a.getIntValue();
            }
            if ((a = shared.getAttribute("panelheight")) != null) {
                panelHeight = a.getIntValue();
            }
            if ((a = shared.getAttribute("panelwidth")) != null) {
                panelWidth = a.getIntValue();
            }

            mainlinetrackwidth = shared.getAttribute("mainlinetrackwidth").getIntValue();
            sidetrackwidth = shared.getAttribute("sidetrackwidth").getIntValue();
        } catch (org.jdom2.DataConversionException e) {
            log.error("failed to convert LayoutEditor attribute");
            result = false;
        }

        double xScale = 1.0;
        double yScale = 1.0;
        a = shared.getAttribute("xscale");
        if (a != null) {
            try {
                xScale = (Float.parseFloat(a.getValue()));
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("yscale");
        if (a != null) {
            try {
                yScale = (Float.parseFloat(a.getValue()));
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }

        // find the name and default track color
        String name = "";
        a = shared.getAttribute("name");
        if (a != null) {
            name = a.getValue();
        }
        if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
            JFrame frame = new JFrame("DialogDemo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
            int n = JOptionPane.showConfirmDialog(frame,
                    java.text.MessageFormat.format(rb.getString("DuplicatePanel"),
                            new Object[]{name}),
                    rb.getString("DuplicatePanelTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        LayoutEditor panel = new LayoutEditor(name);
        panel.setLayoutName(name);
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(panel);

        // create the objects
        panel.setMainlineTrackWidth(mainlinetrackwidth);
        panel.setSideTrackWidth(sidetrackwidth);
        panel.setXScale(xScale);
        panel.setYScale(yScale);

        String defaultColor = "black";
        a = shared.getAttribute("defaulttrackcolor");
        if (a != null) {
            defaultColor = a.getValue();
        }
        panel.setDefaultTrackColor(defaultColor);

        String defaultTextColor = "black";
        a = shared.getAttribute("defaulttextcolor");
        if (a != null) {
            defaultTextColor = a.getValue();
        }
        panel.setDefaultTextColor(defaultTextColor);

        String turnoutCircleColor = "track";  //default to using use default track color for circle color
        a = shared.getAttribute("turnoutcirclecolor");
        if (a != null) {
            turnoutCircleColor = a.getValue();
        }
        panel.setTurnoutCircleColor(turnoutCircleColor);

        int turnoutCircleSize = 2;
        a = shared.getAttribute("turnoutcirclesize");
        if (a != null) {
            try {
                turnoutCircleSize = a.getIntValue();
            } catch (DataConversionException e1) {
                //leave at default if cannot convert
                log.warn("unable to convert turnoutcirclesize");
            }
        }
        panel.setTurnoutCircleSize(turnoutCircleSize);

        boolean value = true;
        try {
            value = shared.getAttribute("turnoutdrawunselectedleg").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setTurnoutDrawUnselectedLeg(value);

        // turnout size parameters
        double sz = 20.0;
        a = shared.getAttribute("turnoutbx");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setTurnoutBX(sz);
            } catch (Exception e) {
                log.error("failed to convert turnoutbx to float - " + a.getValue());
                result = false;
            }
        }

        a = shared.getAttribute("turnoutcx");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setTurnoutCX(sz);
            } catch (Exception e) {
                log.error("failed to convert turnoutcx to float - " + a.getValue());
                result = false;
            }
        }

        a = shared.getAttribute("turnoutwid");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setTurnoutWid(sz);
            } catch (Exception e) {
                log.error("failed to convert turnoutwid to float - " + a.getValue());
                result = false;
            }
        }

        a = shared.getAttribute("xoverlong");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setXOverLong(sz);
            } catch (Exception e) {
                log.error("failed to convert xoverlong to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("xoverhwid");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setXOverHWid(sz);
            } catch (Exception e) {
                log.error("failed to convert xoverhwid to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("xovershort");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setXOverShort(sz);
            } catch (Exception e) {
                log.error("failed to convert xovershort to float - " + a.getValue());
                result = false;
            }
        }
        // grid size parameter
        int iz = 10; // this value is never used but it's the default
        a = shared.getAttribute("gridSize");
        if (a != null) {
            try {
                iz = (Integer.parseInt(a.getValue()));
                panel.setGridSize(iz);
            } catch (Exception e) {
                log.error("failed to convert gridSize to int - " + a.getValue());
                result = false;
            }
        }

        // second grid size parameter
        iz = 10; // this value is never used but it's the default
        a = shared.getAttribute("gridSize2nd");
        if (a != null) {
            try {
                iz = (Integer.parseInt(a.getValue()));
                panel.setGridSize2nd(iz);
            } catch (Exception e) {
                log.error("failed to convert gridSize2nd to int - " + a.getValue());
                result = false;
            }
        }

        value = true;
        try {
            value = shared.getAttribute("positionable").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setAllPositionable(value);

        value = true;
        try {
            value = shared.getAttribute("controlling").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setAllControlling(value);

        value = true;
        try {
            value = shared.getAttribute("animating").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setTurnoutAnimation(value);

        value = false;
        try {
            value = shared.getAttribute("drawgrid").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setDrawGrid(value);

        value = false;
        try {
            value = shared.getAttribute("snaponadd").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setSnapOnAdd(value);

        value = false;
        try {
            value = shared.getAttribute("snaponmove").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setSnapOnMove(value);

        value = false;
        try {
            value = shared.getAttribute("turnoutcircles").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setTurnoutCircles(value);

        value = false;
        try {
            value = shared.getAttribute("tooltipsnotedit").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setTooltipsNotEdit(value);

        value = false;
        try {
            value = shared.getAttribute("autoblkgenerate").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setAutoBlockAssignment(value);

        value = true;
        try {
            value = shared.getAttribute("tooltipsinedit").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setTooltipsInEdit(value);

        // set default track color
        if ((a = shared.getAttribute("defaulttrackcolor")) != null) {
            panel.setDefaultTrackColor(a.getValue());
        }
        // set default track color
        if ((a = shared.getAttribute("defaultoccupiedtrackcolor")) != null) {
            panel.setDefaultOccupiedTrackColor(a.getValue());
        }
        // set default track color
        if ((a = shared.getAttribute("defaultalternativetrackcolor")) != null) {
            panel.setDefaultAlternativeTrackColor(a.getValue());
        }
        try {
            int red = shared.getAttribute("redBackground").getIntValue();
            int blue = shared.getAttribute("blueBackground").getIntValue();
            int green = shared.getAttribute("greenBackground").getIntValue();
            panel.setDefaultBackgroundColor(ColorUtil.colorToString(new Color(red, green, blue)));
            panel.setBackgroundColor(new Color(red, green, blue));
        } catch (org.jdom2.DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
        }

        value = false;
        try {
            value = shared.getAttribute("useDirectTurnoutControl").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setDirectTurnoutControl(value);

        // Set editor's option flags, load content after
        // this so that individual item flags are set as saved
        panel.initView();

        // load the contents
        List<Element> items = shared.getChildren();
        for (Element item : shared.getChildren()) {
            // get the class, hence the adapter object to do loading
            String adapterName = item.getAttribute("class").getValue();

            if (log.isDebugEnabled()) {
                String id = "<null>";
                try {
                    id = item.getAttribute("ident").getValue();
                    log.debug("Load " + id + " for [" + panel.getName() + "] via " + adapterName);
                } catch (Exception e) {
                    log.debug("Load layout object for [" + panel.getName() + "] via " + adapterName);
                }
            }
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

        // final initialization of objects
        panel.setConnections();

        // display the results
        value = true;
        try {
            value = shared.getAttribute("editable").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setAllEditable(value);  // set first since other attribute use this setting

        value = true;
        try {
            value = shared.getAttribute("showhelpbar").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setShowHelpBar(value);

        value = false;
        try {
            value = shared.getAttribute("antialiasing").getBooleanValue();
        } catch (Exception e) {
        }
        panel.setAntialiasingOn(value);

        // set contents state
        String slValue = "both";
        try {
            value = shared.getAttribute("sliders").getBooleanValue();
            slValue = value ? "both" : "none";
        } catch (Exception e) {
        }
        if ((a = shared.getAttribute("scrollable")) != null) {
            slValue = a.getValue();
        }
        panel.setScroll(slValue);

        panel.pack();
        panel.setLayoutDimensions(windowWidth, windowHeight, x, y, panelWidth, panelHeight);
        panel.setVisible(true);    // always show the panel
        panel.resetDirty();

        // register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(panel);
        }
        //open Dispatcher frame if any Transits are defined, and open Dispatcher flag set on
        if (jmri.InstanceManager.getDefault(jmri.TransitManager.class).getSystemNameList().size() > 0) {
            try {
                value = shared.getAttribute("openDispatcher").getBooleanValue();
                panel.setOpenDispatcherOnLoad(value);
                if (value) {
                    DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);
                    df.loadAtStartup();
                }
            } catch (Exception e) {
            }
        }
        return result;
    }   // load

    @Override
    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }
    private final static Logger log = LoggerFactory.getLogger(LayoutEditorXml.class.getName());
}
