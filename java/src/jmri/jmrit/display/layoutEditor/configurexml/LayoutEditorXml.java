package jmri.jmrit.display.layoutEditor.configurexml;

import java.awt.Color;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import jmri.InstanceManager;
import jmri.configurexml.AbstractXmlAdapter;
import jmri.configurexml.XmlAdapter;
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
 *
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
        p.resetDirty();
        panel.setAttribute("openDispatcher", p.getOpenDispatcherOnLoad() ? "yes" : "no");
        panel.setAttribute("useDirectTurnoutControl", p.getDirectTurnoutControl() ? "yes" : "no");

        // include contents (Icons and Labels)
        List<Positionable> contents = p.getContents();
        int num = contents.size();
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Positionable sub = contents.get(i);
                if (sub != null && sub.storeItem()) {
                    try {
                        Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                        if (e != null) {
                            panel.addContent(e);
                        }
                    } catch (Exception e) {
                        log.error("Error storing panel contents element: " + e);
                    }
                } else {
                    log.warn("Null entry found when storing panel contents.");
                }
            }
        }

        // include LayoutTurnouts
        num = p.turnoutList.size();
        if (log.isDebugEnabled()) {
            log.debug("N layoutturnout elements: " + num);
        }
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Object sub = p.turnoutList.get(i);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel layoutturnout element: " + e);
                }
            }
        }

        // include TrackSegments
        num = p.trackList.size();
        if (log.isDebugEnabled()) {
            log.debug("N tracksegment elements: " + num);
        }
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Object sub = p.trackList.get(i);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel tracksegment element: " + e);
                }
            }
        }
        // include PositionablePoints
        num = p.pointList.size();
        if (log.isDebugEnabled()) {
            log.debug("N positionablepoint elements: " + num);
        }
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Object sub = p.pointList.get(i);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel positionalpoint element: " + e);
                }
            }
        }
        // include LevelXings
        num = p.xingList.size();
        if (log.isDebugEnabled()) {
            log.debug("N levelxing elements: " + num);
        }
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Object sub = p.xingList.get(i);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel levelxing element: " + e);
                }
            }
        }
        // include LayoutSlips
        num = p.slipList.size();
        if (log.isDebugEnabled()) {
            log.debug("N layoutSlip elements: " + num);
        }
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Object sub = p.slipList.get(i);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel layoutSlip element: " + e);
                }
            }
        }
        // include LayoutTurntables
        num = p.turntableList.size();
        if (log.isDebugEnabled()) {
            log.debug("N turntable elements: " + num);
        }
        if (num > 0) {
            for (int i = 0; i < num; i++) {
                Object sub = p.turntableList.get(i);
                try {
                    Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        panel.addContent(e);
                    }
                } catch (Exception e) {
                    log.error("Error storing panel turntable element: " + e);
                }
            }
        }
        return panel;
    }

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
            // For compatibility with previous versions, try and see if height and width tags are contained in the file
            if ((a = shared.getAttribute("height")) != null) {
                windowHeight = a.getIntValue();
                panelHeight = windowHeight - 60;
            }
            if ((a = shared.getAttribute("width")) != null) {
                windowWidth = a.getIntValue();
                panelWidth = windowWidth - 18;
            }
            // For files created by the new version, retrieve window and panel sizes
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
            log.error("failed to convert LayoutEditor's attribute");
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
        if (shared.getAttribute("name") != null) {
            name = shared.getAttribute("name").getValue();
        }
        if (jmri.jmrit.display.PanelMenu.instance().isPanelNameUsed(name)) {
            JFrame frame = new JFrame("DialogDemo");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            log.warn("File contains a panel with the same name (" + name + ") as an existing panel");
            int n = JOptionPane.showConfirmDialog(frame,
                    java.text.MessageFormat.format(rb.getString("DuplicatePanel"),
                            new Object[]{name}),
                    rb.getString("DuplicatePanelTitle"),
                    JOptionPane.YES_NO_OPTION);
            if (n == JOptionPane.NO_OPTION) {
                return false;
            }
        }
        String defaultColor = "black";
        String defaultTextColor = "black";
        if (shared.getAttribute("defaulttrackcolor") != null) {
            defaultColor = shared.getAttribute("defaulttrackcolor").getValue();
        }
        if (shared.getAttribute("defaulttextcolor") != null) {
            defaultTextColor = shared.getAttribute("defaulttextcolor").getValue();
        }
        String turnoutCircleColor = "track";  //default to using use default track color for circle color
        if (shared.getAttribute("turnoutcirclecolor") != null) {
            turnoutCircleColor = shared.getAttribute("turnoutcirclecolor").getValue();
        }
        int turnoutCircleSize = 2;
        if (shared.getAttribute("turnoutcirclesize") != null) {
            try {
                turnoutCircleSize = shared.getAttribute("turnoutcirclesize").getIntValue();
            } catch (DataConversionException e1) {
                //leave at default if cannot convert
                log.warn("unable to convert turnoutcirclesize");
            }
        }
        boolean turnoutDrawUnselectedLeg = true;
        if ((a = shared.getAttribute("turnoutdrawunselectedleg")) != null && a.getValue().equals("no")) {
            turnoutDrawUnselectedLeg = false;
        }
        // create the objects
        LayoutEditor panel = new LayoutEditor(name);
        panel.setLayoutName(name);
        panel.setMainlineTrackWidth(mainlinetrackwidth);
        panel.setSideTrackWidth(sidetrackwidth);
        panel.setDefaultTrackColor(defaultColor);
        panel.setDefaultTextColor(defaultTextColor);
        panel.setTurnoutCircleColor(turnoutCircleColor);
        panel.setTurnoutCircleSize(turnoutCircleSize);
        panel.setTurnoutDrawUnselectedLeg(turnoutDrawUnselectedLeg);
        panel.setXScale(xScale);
        panel.setYScale(yScale);
        // turnout size parameters
        double sz = 20.0;
        a = shared.getAttribute("turnoutbx");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setTurnoutBX(sz);
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("turnoutcx");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setTurnoutCX(sz);
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("turnoutwid");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setTurnoutWid(sz);
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("xoverlong");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setXOverLong(sz);
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("xoverhwid");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setXOverHWid(sz);
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        a = shared.getAttribute("xovershort");
        if (a != null) {
            try {
                sz = (Float.parseFloat(a.getValue()));
                panel.setXOverShort(sz);
            } catch (Exception e) {
                log.error("failed to convert to float - " + a.getValue());
                result = false;
            }
        }
        // grid size parameter
        int iz = 10; // thisw value is never used but it's the default 
        a = shared.getAttribute("gridSize");
        if (a != null) {
            try {
                iz = (Integer.parseInt(a.getValue()));
                panel.setGridSize(iz);
            } catch (Exception e) {
                log.error("failed to convert to int - " + a.getValue());
                result = false;
            }
        }
        
        // set contents state
        String slValue = "both";
        if ((a = shared.getAttribute("sliders")) != null && a.getValue().equals("no")) {
            slValue = "none";
        }
        if ((a = shared.getAttribute("scrollable")) != null) {
            slValue = a.getValue();
        }

        boolean edValue = true;
        if ((a = shared.getAttribute("editable")) != null && a.getValue().equals("no")) {
            edValue = false;
        }

        boolean value = true;
        if ((a = shared.getAttribute("positionable")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllPositionable(value);

        value = true;
        if ((a = shared.getAttribute("controlling")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setAllControlling(value);

        value = true;
        if ((a = shared.getAttribute("animating")) != null && a.getValue().equals("no")) {
            value = false;
        }
        panel.setTurnoutAnimation(value);

        boolean hbValue = true;
        if ((a = shared.getAttribute("showhelpbar")) != null && a.getValue().equals("no")) {
            hbValue = false;
        }

        boolean dgValue = false;
        if ((a = shared.getAttribute("drawgrid")) != null && a.getValue().equals("yes")) {
            dgValue = true;
        }

        boolean sgaValue = false;
        if ((a = shared.getAttribute("snaponadd")) != null && a.getValue().equals("yes")) {
            sgaValue = true;
        }

        boolean sgmValue = false;
        if ((a = shared.getAttribute("snaponmove")) != null && a.getValue().equals("yes")) {
            sgmValue = true;
        }

        boolean aaValue = false;
        if ((a = shared.getAttribute("antialiasing")) != null && a.getValue().equals("yes")) {
            aaValue = true;
        }

        value = false;
        if ((a = shared.getAttribute("turnoutcircles")) != null && a.getValue().equals("yes")) {
            value = true;
        }
        panel.setTurnoutCircles(value);

        value = false;
        if ((a = shared.getAttribute("tooltipsnotedit")) != null && a.getValue().equals("yes")) {
            value = true;
        }
        panel.setTooltipsNotEdit(value);

        value = false;
        if ((a = shared.getAttribute("autoblkgenerate")) != null && a.getValue().equals("yes")) {
            value = true;
        }
        panel.setAutoBlockAssignment(value);

        value = true;
        if ((a = shared.getAttribute("tooltipsinedit")) != null && a.getValue().equals("no")) {
            value = false;
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
        if (shared.getAttribute("useDirectTurnoutControl") != null) {
            if (shared.getAttribute("useDirectTurnoutControl").getValue().equals("yes")) {
                panel.setDirectTurnoutControl(true);
            }
        }
        // Set editor's option flags, load content after 
        // this so that individual item flags are set as saved
        panel.initView();

        // load the contents
        List<Element> items = shared.getChildren();
        for (int i = 0; i < items.size(); i++) {
            // get the class, hence the adapter object to do loading
            Element item = items.get(i);
            String adapterName = item.getAttribute("class").getValue();
            if (log.isDebugEnabled()) {
                String id = "<null>";
                try {
                    id = item.getAttribute("ident").getValue();
                } catch (Exception e) {
                }
                log.debug("Load " + id + " for [" + panel.getName() + "] via " + adapterName);
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
        panel.setAllEditable(edValue);  // set first since other attribute use this setting
        panel.setShowHelpBar(hbValue);
        panel.setDrawGrid(dgValue);
        panel.setSnapOnAdd(sgaValue);
        panel.setSnapOnMove(sgmValue);
        panel.setAntialiasingOn(aaValue);
        panel.setScroll(slValue);
        panel.pack();
        panel.setLayoutDimensions(windowWidth, windowHeight, x, y, panelWidth, panelHeight);
        panel.setVisible(true);    // always show the panel
        panel.resetDirty();

        // register the resulting panel for later configuration
        InstanceManager.getOptionalDefault(jmri.ConfigureManager.class).registerUser(panel);
        if (jmri.InstanceManager.transitManagerInstance().getSystemNameList().size() > 0) {
            if (shared.getAttribute("openDispatcher") != null) {
                if (shared.getAttribute("openDispatcher").getValue().equals("yes")) {
                    panel.setOpenDispatcherOnLoad(true);
                    jmri.jmrit.dispatcher.DispatcherFrame.instance();
                } else {
                    panel.setOpenDispatcherOnLoad(false);
                }
            }
        }
        return result;
    }

    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }

    private final static Logger log = LoggerFactory.getLogger(LayoutEditorXml.class.getName());

}
