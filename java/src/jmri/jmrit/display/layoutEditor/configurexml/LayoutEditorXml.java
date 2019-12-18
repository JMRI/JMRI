package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.util.List;
import java.util.stream.Collectors;
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
import jmri.jmrit.display.layoutEditor.LayoutShape;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.LayoutTrackDrawingOptions;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LayoutTurntable;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import jmri.util.ColorUtil;
import org.jdom2.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handle configuration for LayoutEditor panes.
 * <p>
 * Based in part on PanelEditorXml.java
 *
 * @author Dave Duchamp Copyright (c) 2007
 * @author George Warner Copyright (c) 2017-2018
 */
public class LayoutEditorXml extends AbstractXmlAdapter {

    public LayoutEditorXml() {
    }

    /**
     * Default implementation for storing the contents of a LayoutEditor.
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
        if (InstanceManager.getDefault(apps.gui.GuiLafPreferencesManager.class).isEditorUseOldLocSize()) {
            panel.setAttribute("x", "" + p.getUpperLeftX());
            panel.setAttribute("y", "" + p.getUpperLeftY());
            panel.setAttribute("windowheight", "" + p.getWindowHeight());
            panel.setAttribute("windowwidth", "" + p.getWindowWidth());
        } else {
            // Use real location and size
            java.awt.Point loc = p.getLocation();
            panel.setAttribute("x", "" + loc.x);
            panel.setAttribute("y", "" + loc.y);

            java.awt.Dimension size = p.getSize();
            panel.setAttribute("windowheight", "" + size.height);
            panel.setAttribute("windowwidth", "" + size.width);
        }
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
        panel.setAttribute("sidetrackwidth", "" + p.getSidelineTrackWidth());
        panel.setAttribute("defaulttrackcolor", p.getDefaultTrackColor());
        panel.setAttribute("defaultoccupiedtrackcolor", p.getDefaultOccupiedTrackColor());
        panel.setAttribute("defaultalternativetrackcolor", p.getDefaultAlternativeTrackColor());
        panel.setAttribute("defaulttextcolor", p.getDefaultTextColor());
        String turnoutCircleColor = p.getTurnoutCircleColor();
        panel.setAttribute("turnoutcirclecolor", turnoutCircleColor);
        String turnoutCircleThrownColor = p.getTurnoutCircleThrownColor();
        // optional attributes
        if (!turnoutCircleColor.equals(turnoutCircleThrownColor)) {
            panel.setAttribute("turnoutcirclethrowncolor", turnoutCircleThrownColor);
        }
        if (p.isTurnoutFillControlCircles()) {
            panel.setAttribute("turnoutfillcontrolcircles", "yes");
        }

        panel.setAttribute("turnoutcirclesize", "" + p.getTurnoutCircleSize());
        panel.setAttribute("turnoutdrawunselectedleg", (p.isTurnoutDrawUnselectedLeg() ? "yes" : "no"));
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

        // store layout track drawing options
        try {
            LayoutTrackDrawingOptions ltdo = p.getLayoutTrackDrawingOptions();
            Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(ltdo);
            if (e != null) {
                panel.addContent(e);
            }
        } catch (Exception e) {
            log.error("Error storing contents element: " + e);
        }

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

        // include LayoutTracks
        List<LayoutTrack> layoutTracks = p.getLayoutTracks();
        num = layoutTracks.size();
        if (log.isDebugEnabled()) {
            log.debug("N LayoutTrack elements: " + num);
        }

        // uncomment this (!!!temporarly!!!) to save alphanumerically sorted by ID
//        log.error("DO NOT LEAVE THIS ENABLED FOR PRODUCTION: ORGINAL ORDER MUST BE MAINTAINED.");
//        Collections.sort(layoutTracks, new Comparator<LayoutTrack>() {
//            @Override
//            public int compare(LayoutTrack t1, LayoutTrack t2) {
//                AlphanumComparator ac = new AlphanumComparator();
//                return ac.compare(t1.getId(), t2.getId());
//            }
//        });

        // Because some people (like me) like to edit their panel.xml files
        // directly we're going to group the layout tracks by class before
        // storing them. Note: No other order is effected; They should exist
        // in the saved file in the order that they were created (ether at
        // panel file load time or later by the users in the editor).
        List<LayoutTrack> orderedList = layoutTracks.stream() // next line excludes LayoutSlips
                .filter(item -> ((item instanceof LayoutTurnout) && !(item instanceof LayoutSlip)))
                .map(item -> (LayoutTurnout) item)
                .collect(Collectors.toList());
        orderedList.addAll(layoutTracks.stream()
                .filter(item -> item instanceof TrackSegment)
                .map(item -> (TrackSegment) item)
                .collect(Collectors.toList()));
        orderedList.addAll(layoutTracks.stream()
                .filter(item -> item instanceof PositionablePoint)
                .map(item -> (PositionablePoint) item)
                .collect(Collectors.toList()));
        orderedList.addAll(layoutTracks.stream()
                .filter(item -> item instanceof LevelXing)
                .map(item -> (LevelXing) item)
                .collect(Collectors.toList()));
        orderedList.addAll(layoutTracks.stream()
                .filter(item -> item instanceof LayoutSlip)
                .map(item -> (LayoutSlip) item)
                .collect(Collectors.toList()));
        orderedList.addAll(layoutTracks.stream()
                .filter(item -> item instanceof LayoutTurntable)
                .map(item -> (LayoutTurntable) item)
                .collect(Collectors.toList()));

        for (LayoutTrack lt : orderedList) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(lt);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing layoutturnout element: " + e);
            }
        }

        // include Layout Shapes
        for (LayoutShape ls : p.getLayoutShapes()) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(ls);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing layout shape element: " + e);
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
            if ((a = shared.getAttribute("x")) != null) {
                x = a.getIntValue();
            }
            if ((a = shared.getAttribute("y")) != null) {
                y = a.getIntValue();
            }

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
        } catch (DataConversionException e) {
            log.error("failed to convert LayoutEditor attribute");
            result = false;
        }

        double xScale = 1.0;
        double yScale = 1.0;
        if ((a = shared.getAttribute("xscale")) != null) {
            try {
                xScale = (Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert xscale attribute to float - " + a.getValue());
                result = false;
            }
        }
        if ((a = shared.getAttribute("yscale")) != null) {
            try {
                yScale = (Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert yscale attribute to float - " + a.getValue());
                result = false;
            }
        }

        // find the name
        String name = "";
        if ((a = shared.getAttribute("name")) != null) {
            name = a.getValue();
        }
        if (InstanceManager.getDefault(PanelMenu.class).isPanelNameUsed(name)) {
            JFrame frame = new JFrame("DialogDemo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            log.warn("File contains a panel with the same name ({}) as an existing panel", name);
            int n = JOptionPane.showConfirmDialog(frame,
                    Bundle.getMessage("DuplicatePanel", name),
                    Bundle.getMessage("DuplicatePanelTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return false;
            }
        }

        // If available, override location and size with machine dependent values
        if (!InstanceManager.getDefault(apps.gui.GuiLafPreferencesManager.class).isEditorUseOldLocSize()) {
            jmri.UserPreferencesManager prefsMgr = InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
            if (prefsMgr != null) {
                String windowFrameRef = "jmri.jmrit.display.layoutEditor.LayoutEditor:" + name;

                java.awt.Point prefsWindowLocation = prefsMgr.getWindowLocation(windowFrameRef);
                if (prefsWindowLocation != null) {
                    x = (int) prefsWindowLocation.getX();
                    y = (int) prefsWindowLocation.getY();
                }

                java.awt.Dimension prefsWindowSize = prefsMgr.getWindowSize(windowFrameRef);
                if (prefsWindowSize != null && prefsWindowSize.getHeight() != 0 && prefsWindowSize.getWidth() != 0) {
                    windowHeight = (int) prefsWindowSize.getHeight();
                    windowWidth = (int) prefsWindowSize.getWidth();
                }
            }
        }

        LayoutEditor panel = new LayoutEditor(name);
        panel.setLayoutName(name);
        InstanceManager.getDefault(PanelMenu.class).addEditorPanel(panel);

        // create the objects
        panel.setMainlineTrackWidth(mainlinetrackwidth);
        panel.setSidelineTrackWidth(sidetrackwidth);
        panel.setXScale(xScale);
        panel.setYScale(yScale);

        String color = ColorUtil.ColorDarkGray;
        try {
            if ((a = shared.getAttribute("defaulttrackcolor")) != null) {
                color = a.getValue();
            }
            panel.setDefaultTrackColor(ColorUtil.stringToColor(color));
        } catch (IllegalArgumentException e) {
            panel.setDefaultTrackColor(Color.BLACK);
            log.error("Invalid defaulttrackcolor {}; using black", color);
        }

        color = ColorUtil.ColorBlack;
        try {
            if ((a = shared.getAttribute("defaulttextcolor")) != null) {
                color = a.getValue();
            }
            panel.setDefaultTextColor(ColorUtil.stringToColor(color));
        } catch (IllegalArgumentException e) {
            panel.setDefaultTextColor(Color.BLACK);
            log.error("Invalid defaulttextcolor {}; using black", color);
        }

        color = "track";  //default to using use default track color for circle color
        try {
            if ((a = shared.getAttribute("turnoutcirclecolor")) != null) {
                color = a.getValue();
            }
            panel.setTurnoutCircleColor(ColorUtil.stringToColor(color));
        } catch (IllegalArgumentException e) {
            panel.setTurnoutCircleColor(Color.BLACK);
            log.error("Invalid color {}; using black", color);
        }

        // default to using turnout circle color just set
        try {
            if ((a = shared.getAttribute("turnoutcirclethrowncolor")) != null) {
                color = a.getValue();
            }
            panel.setTurnoutCircleThrownColor(ColorUtil.stringToColor(color));
        } catch (IllegalArgumentException e) {
            panel.setTurnoutCircleThrownColor(Color.BLACK);
            log.error("Invalid color {}; using black", color);
        }

        try {   // the "turnoutfillcontrolcircles" attribute has a default="no" value in the schema;
            // it will always return a "no" attribute if the attribute is not present.
            panel.setTurnoutFillControlCircles(shared.getAttribute("turnoutfillcontrolcircles").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert turnoutfillcontrolcircles attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing turnoutfillcontrolcircles attribute");
        }

        if ((a = shared.getAttribute("turnoutcirclesize")) != null) {
            try {
                panel.setTurnoutCircleSize(a.getIntValue());
            } catch (DataConversionException e) {
                log.warn("unable to convert turnoutcirclesize");
            }
        }

        try {
            panel.setTurnoutDrawUnselectedLeg(shared.getAttribute("turnoutdrawunselectedleg").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert turnoutdrawunselectedleg attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing turnoutdrawunselectedleg attribute");
        }

        // turnout size parameters
        if ((a = shared.getAttribute("turnoutbx")) != null) {
            try {
                panel.setTurnoutBX(Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert turnoutbx to float - " + a.getValue());
                result = false;
            }
        }

        if ((a = shared.getAttribute("turnoutcx")) != null) {
            try {
                panel.setTurnoutCX(Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert turnoutcx to float - " + a.getValue());
                result = false;
            }
        }

        if ((a = shared.getAttribute("turnoutwid")) != null) {
            try {
                panel.setTurnoutWid(Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert turnoutwid to float - " + a.getValue());
                result = false;
            }
        }

        if ((a = shared.getAttribute("xoverlong")) != null) {
            try {
                panel.setXOverLong(Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert xoverlong to float - " + a.getValue());
                result = false;
            }
        }
        if ((a = shared.getAttribute("xoverhwid")) != null) {
            try {
                panel.setXOverHWid(Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert xoverhwid to float - " + a.getValue());
                result = false;
            }
        }
        if ((a = shared.getAttribute("xovershort")) != null) {
            try {
                panel.setXOverShort(Float.parseFloat(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert xovershort to float - " + a.getValue());
                result = false;
            }
        }
        // grid size parameter
        if ((a = shared.getAttribute("gridSize")) != null) {
            try {
                panel.setGridSize(Integer.parseInt(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert gridSize to int - " + a.getValue());
                result = false;
            }
        }

        // second grid size parameter
        if ((a = shared.getAttribute("gridSize2nd")) != null) {
            try {
                panel.setGridSize2nd(Integer.parseInt(a.getValue()));
            } catch (NumberFormatException e) {
                log.error("failed to convert gridSize2nd to int - " + a.getValue());
                result = false;
            }
        }

        try {
            panel.setAllPositionable(shared.getAttribute("positionable").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert positionable attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing positionable attribute");

        }

        try {
            panel.setAllControlling(shared.getAttribute("controlling").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert controlling attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing controlling attribute");
        }

        try {
            panel.setTurnoutAnimation(shared.getAttribute("animating").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert animating attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing animating attribute");
        }

        try {
            panel.setDrawGrid(shared.getAttribute("drawgrid").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert drawgrid attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing drawgrid attribute");
        }

        try {
            panel.setSnapOnAdd(shared.getAttribute("snaponadd").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert snaponadd attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing snaponadd attribute");
        }

        try {
            panel.setSnapOnMove(shared.getAttribute("snaponmove").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert snaponmove attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing snaponmove attribute");
        }

        try {
            panel.setTurnoutCircles(shared.getAttribute("turnoutcircles").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert turnoutcircles attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing turnoutcircles attribute");
        }

        try {
            panel.setTooltipsNotEdit(shared.getAttribute("tooltipsnotedit").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert tooltipsnotedit attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing tooltipsnotedit attribute");
        }

        try {
            panel.setAutoBlockAssignment(shared.getAttribute("autoblkgenerate").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert autoblkgenerate attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing autoblkgenerate attribute");
        }

        try {
            panel.setTooltipsInEdit(shared.getAttribute("tooltipsinedit").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert tooltipsinedit attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing tooltipsinedit attribute");
        }

        // set default track color
        if ((a = shared.getAttribute("defaulttrackcolor")) != null) {
            try {
                panel.setDefaultTrackColor(ColorUtil.stringToColor(a.getValue()));
            } catch (IllegalArgumentException e) {
                panel.setDefaultTrackColor(Color.BLACK);
                log.error("Invalid color {}; using black", a.getValue());
            }
        }
        // set default occupied track color
        if ((a = shared.getAttribute("defaultoccupiedtrackcolor")) != null) {
            try {
                panel.setDefaultOccupiedTrackColor(ColorUtil.stringToColor(a.getValue()));
            } catch (IllegalArgumentException e) {
                panel.setDefaultOccupiedTrackColor(Color.BLACK);
                log.error("Invalid color {}; using black", a.getValue());
            }
        }
        // set default alternative track color
        if ((a = shared.getAttribute("defaultalternativetrackcolor")) != null) {
            try {
                panel.setDefaultAlternativeTrackColor(ColorUtil.stringToColor(a.getValue()));
            } catch (IllegalArgumentException e) {
                panel.setDefaultAlternativeTrackColor(Color.BLACK);
                log.error("Invalid color {}; using black", a.getValue());
            }
        }
        try {
            int red = shared.getAttribute("redBackground").getIntValue();
            int blue = shared.getAttribute("blueBackground").getIntValue();
            int green = shared.getAttribute("greenBackground").getIntValue();
            Color backgroundColor = new Color(red, green, blue);
            panel.setDefaultBackgroundColor(backgroundColor);
            panel.setBackgroundColor(backgroundColor);
        } catch (DataConversionException e) {
            log.warn("Could not parse color attributes!");
        } catch (NullPointerException e) {  // considered normal if the attributes are not present
            log.debug("missing backbround color attributes");
        }

        try {
            panel.setDirectTurnoutControl(shared.getAttribute("useDirectTurnoutControl").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert Layout Editor useDirectTurnoutControl attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing useDirectTurnoutControl attribute");
        }

        // Set editor's option flags, load content after
        // this so that individual item flags are set as saved
        panel.initView();

        // load the contents
        for (Element item : shared.getChildren()) {
            // get the class, hence the adapter object to do loading
            String adapterName = item.getAttribute("class").getValue();
            adapterName = jmri.configurexml.ConfigXmlManager.currentClassName(adapterName);

            if (log.isDebugEnabled()) {
                String id = "<null>";
                try {
                    id = item.getAttribute("name").getValue();
                    log.debug("Load " + id + " for [" + panel.getName() + "] via " + adapterName);
                } catch (NullPointerException e) {
                    log.debug("Load layout object for [" + panel.getName() + "] via " + adapterName);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                }
            }
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

        // final initialization of objects
        panel.setConnections();

        // display the results
        try {
            // set first since other attribute use this setting
            panel.setAllEditable(shared.getAttribute("editable").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert editable attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing editable attribute");
        }

        try {
            panel.setShowHelpBar(shared.getAttribute("showhelpbar").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert showhelpbar attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing showhelpbar attribute");
        }

        try {
            panel.setAntialiasingOn(shared.getAttribute("antialiasing").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert antialiasing attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing antialiasing attribute");
        }

        // set contents state
        String slValue = "both";
        try {
            boolean value = shared.getAttribute("sliders").getBooleanValue();
            slValue = value ? "both" : "none";
        } catch (DataConversionException e) {
            log.warn("unable to convert sliders attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing sliders attribute");
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
        if (jmri.InstanceManager.getDefault(jmri.TransitManager.class).getNamedBeanSet().size() > 0) {
            try {
                boolean value = shared.getAttribute("openDispatcher").getBooleanValue();
                panel.setOpenDispatcherOnLoad(value);
                if (value) {
                    DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);
                    df.loadAtStartup();
                }
            } catch (DataConversionException e) {
                log.warn("unable to convert openDispatcher attribute");
            } catch (NullPointerException e) {  // considered normal if the attribute is not present
                log.debug("missing openDispatcher attribute");
            }
        }
        return result;
    }   // load

    @Override
    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }
    private final static Logger log = LoggerFactory.getLogger(LayoutEditorXml.class);
}
