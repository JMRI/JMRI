/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.jmrit.display.modulesEditor.configurexml;

import java.awt.Color;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import jmri.*;
import jmri.configurexml.*;
import jmri.configurexml.XmlAdapter;
import jmri.jmrit.dispatcher.DispatcherFrame;
import jmri.jmrit.display.*;
import jmri.jmrit.display.modulesEditor.ModulesEditor;
import jmri.util.ColorUtil;

import org.jdom2.*;

/**
 * Handle configuration for ModulesEditor panes.
 * <p>
 * Based in part on PanelEditorXml.java
 *
 * @author George Warner Copyright (c) 2020
 */
public class ModulesEditorXml extends AbstractXmlAdapter {

    public ModulesEditorXml() {
    }

    /**
     * Default implementation for storing the contents of a ModulesEditor.
     *
     * @param o Object to store, of type ModulesEditor
     * @return Element containing the complete info
     */
    @Override
    public Element store(Object o) {
        ModulesEditor p = (ModulesEditor) o;

        Element panel = new Element("ModulesEditor");

        panel.setAttribute("class", getClass().getName());
        panel.setAttribute("name", p.getName());
        java.awt.Point loc = p.getLocation();
        panel.setAttribute("x", "" + loc.x);
        panel.setAttribute("y", "" + loc.y);

        java.awt.Dimension size = p.getSize();
        panel.setAttribute("windowheight", "" + size.height);
        panel.setAttribute("windowwidth", "" + size.width);

//        panel.setAttribute("panelheight", "" + p.getLayoutHeight());
//        panel.setAttribute("panelwidth", "" + p.getLayoutWidth());
        panel.setAttribute("editable", "" + (p.isEditable() ? "yes" : "no"));
        panel.setAttribute("drawgrid", "" + (p.isDrawGrid() ? "yes" : "no"));
        panel.setAttribute("snaponadd", "" + (p.isSnapToGridOnAdd() ? "yes" : "no"));
        panel.setAttribute("snaponmove", "" + (p.isSnapToGridOnMove() ? "yes" : "no"));
//        panel.setAttribute("xscale", Float.toString((float) p.getXScale()));
//        panel.setAttribute("yscale", Float.toString((float) p.getYScale()));
        panel.setAttribute("defaulttextcolor", p.getDefaultTextColor());

        if (p.getBackgroundColor() != null) {
            panel.setAttribute("backgroundColor", "" + p.getBackgroundColor());
            //panel.setAttribute("redBackground", "" + p.getBackgroundColor().getRed());
            //panel.setAttribute("greenBackground", "" + p.getBackgroundColor().getGreen());
            //panel.setAttribute("blueBackground", "" + p.getBackgroundColor().getBlue());
        }
        //panel.setAttribute("gridSize", "" + p.getGridSize());
        //panel.setAttribute("gridSize2nd", "" + p.getGridSize2nd());

        //p.resetDirty();
        // note: moving zoom attribute into per-window user preference
        //panel.setAttribute("zoom", Double.toString(p.getZoom()));
        //int num;
        // include contents (Icons and Labels)
        //List<Positionable> contents = p.getContents();
        //for (Positionable sub : contents) {
        //    if (sub != null && sub.storeItem()) {
        //        try {
        //            Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
        //            if (e != null) {
        //                panel.addContent(e);
        //            }
        //        } catch (Exception e) {
        //            log.error("Error storing contents element: ", e);
        //        }
        //    } else {
        //        log.warn("Null entry found when storing panel contents.");
        //    }
        //}
        // include LayoutTracks
//        List<LayoutTrack> layoutTracks = p.getLayoutTracks();
//        num = layoutTracks.size();
//        if (log.isDebugEnabled()) {
//            log.debug("N LayoutTrack elements: {}", num);
//        }
        return panel;
    }   // store

//     private void storeOne(Element panel, Object item) {
//         try {
//             Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(item);
//             if (e != null) {
//                 panel.addContent(e);
//             }
//         } catch (Exception ex) {
//             log.error("Error storing layout item: {}", item, ex);
//         }
//     }

    @Override
    public void load(Element element, Object o) {
        log.error("Invalid method called");
    }

    /**
     * Create a ModulesEditor object, then register and fill it, then pop it in
     * a JFrame
     *
     * @param shared Top level Element to unpack.
     */
    @Override
    public boolean load(Element shared, Element perNode) {
        boolean result = true;
        Attribute a;

        // find coordinates
//        int x, y;

        // separate sizes for window and panel are used
//        int windowHeight = 400;
//        int windowWidth = 300;
//        int panelHeight, panelWidth;
//        try {
//            if ((a = shared.getAttribute("x")) != null) {
//                x = a.getIntValue();
//            }
//            if ((a = shared.getAttribute("y")) != null) {
//                y = a.getIntValue();
//            }

            // For compatibility with previous versions, try and
            // see if height and width tags are contained in the file
//            if ((a = shared.getAttribute("height")) != null) {
//                windowHeight = a.getIntValue();
//                panelHeight = windowHeight - 60;
//            }
//            if ((a = shared.getAttribute("width")) != null) {
//                windowWidth = a.getIntValue();
//                panelWidth = windowWidth - 18;
//            }

            // For files created by the new version,
            // retrieve window and panel sizes
//            if ((a = shared.getAttribute("windowheight")) != null) {
//                windowHeight = a.getIntValue();
//            }
//            if ((a = shared.getAttribute("windowwidth")) != null) {
//                windowWidth = a.getIntValue();
//            }
//            if ((a = shared.getAttribute("panelheight")) != null) {
//                panelHeight = a.getIntValue();
//            }
//            if ((a = shared.getAttribute("panelwidth")) != null) {
//                panelWidth = a.getIntValue();
//            }
//        } catch (DataConversionException e) {
//            log.error("failed to convert ModulesEditor attribute");
//            result = false;
//        }

//        double xScale;
//        double yScale;
//        if ((a = shared.getAttribute("xscale")) != null) {
//            try {
//                xScale = (Float.parseFloat(a.getValue()));
//            } catch (NumberFormatException e) {
//                log.error("failed to convert xscale attribute to float - {}", a.getValue());
//                result = false;
//            }
//        }
//        if ((a = shared.getAttribute("yscale")) != null) {
//            try {
//                yScale = (Float.parseFloat(a.getValue()));
//            } catch (NumberFormatException e) {
//                log.error("failed to convert yscale attribute to float - {}", a.getValue());
//                result = false;
//            }
//        }

        // find the name
        String name = "";
        if ((a = shared.getAttribute("name")) != null) {
            name = a.getValue();
        }
        if (InstanceManager.getDefault(EditorManager.class).contains(name)) {
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
//        if (!InstanceManager.getDefault(jmri.util.gui.GuiLafPreferencesManager.class).isEditorUseOldLocSize()) {
//            jmri.UserPreferencesManager prefsMgr = InstanceManager.getNullableDefault(jmri.UserPreferencesManager.class);
//            if (prefsMgr != null) {
//                String windowFrameRef = "jmri.jmrit.display.layoutEditor.ModulesEditor:" + name;
//
//                java.awt.Point prefsWindowLocation = prefsMgr.getWindowLocation(windowFrameRef);
//                if (prefsWindowLocation != null) {
//                    x = (int) prefsWindowLocation.getX();
//                    y = (int) prefsWindowLocation.getY();
//                }
//
//                java.awt.Dimension prefsWindowSize = prefsMgr.getWindowSize(windowFrameRef);
//                if (prefsWindowSize != null && prefsWindowSize.getHeight() != 0 && prefsWindowSize.getWidth() != 0) {
//                    windowHeight = (int) prefsWindowSize.getHeight();
//                    windowWidth = (int) prefsWindowSize.getWidth();
//                }
//            }
//        }

        ModulesEditor panel = new ModulesEditor(name);
        InstanceManager.getDefault(EditorManager.class).add(panel);

        // create the objects
//        panel.setXScale(xScale);
//        panel.setYScale(yScale);
        String color = ColorUtil.ColorBlack;
        try {
            if ((a = shared.getAttribute("defaulttextcolor")) != null) {
                color = a.getValue();
                panel.setDefaultTextColor(ColorUtil.stringToColor(color));
            }
        } catch (IllegalArgumentException e) {
            panel.setDefaultTextColor(Color.BLACK);
            log.error("Invalid defaulttextcolor {}; using black", color);
        }

//        // grid size parameter
//        if ((a = shared.getAttribute("gridSize")) != null) {
//            try {
//                panel.setGridSize(Integer.parseInt(a.getValue()));
//            } catch (NumberFormatException e) {
//                log.error("failed to convert gridSize to int - {}", a.getValue());
//                result = false;
//            }
//        }
//
//        // second grid size parameter
//        if ((a = shared.getAttribute("gridSize2nd")) != null) {
//            try {
//                panel.setGridSize2nd(Integer.parseInt(a.getValue()));
//            } catch (NumberFormatException e) {
//                log.error("failed to convert gridSize2nd to int - {}", a.getValue());
//                result = false;
//            }
//        }
        try {
            panel.setDrawGrid(shared.getAttribute("drawgrid").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert drawgrid attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing drawgrid attribute");
        }

        try {
            panel.setSnapToGridOnAdd(shared.getAttribute("snaponadd").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert snaponadd attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing snaponadd attribute");
        }

        try {
            panel.setSnapToGridOnMove(shared.getAttribute("snaponmove").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert snaponmove attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing snaponmove attribute");
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
                    log.debug("Load {} for [{}] via {}", id, panel.getName(), adapterName);
                } catch (NullPointerException e) {
                    log.debug("Load layout object for [{}] via {}", panel.getName(), adapterName);
                    log.debug("Load layout object for [{}] via {}", panel.getName(), adapterName);
                } catch (RuntimeException e) {
                    throw e;
                } catch (Exception e) {
                }
            }
            try {
                // get the class name, including migrations
                adapterName = jmri.configurexml.ConfigXmlManager.currentClassName(adapterName);
                // get the adapter object
                XmlAdapter adapter = (XmlAdapter) Class.forName(adapterName).getDeclaredConstructor().newInstance();
                // and load with it
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
        //panel.setConnections();
        // display the results
        try {
            // set first since other attribute use this setting
            panel.setAllEditable(shared.getAttribute("editable").getBooleanValue());
        } catch (DataConversionException e) {
            log.warn("unable to convert editable attribute");
        } catch (NullPointerException e) {  // considered normal if the attribute is not present
            log.debug("missing editable attribute");
        }

        panel.pack();
        //panel.setLayoutDimensions(windowWidth, windowHeight, x, y, panelWidth, panelHeight);
        panel.setVisible(true);    // always show the panel
        //panel.resetDirty();

        // register the resulting panel for later configuration
        ConfigureManager cm = InstanceManager.getNullableDefault(jmri.ConfigureManager.class);
        if (cm != null) {
            cm.registerUser(panel);
        }

        return result;
    }   // load

    @Override
    public int loadOrder() {
        return jmri.Manager.PANELFILES;
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ModulesEditorXml.class);
}
