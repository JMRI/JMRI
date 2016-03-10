package jmri.web.servlet.panel;

import java.awt.Color;
import java.util.List;
import javax.swing.JComponent;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmris.json.JSON;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return xml (for specified LayoutPanel) suitable for use by external clients
 *
 * @author mstevetodd -- based on PanelServlet.java by rhwood
 */
public class LayoutPanelServlet extends AbstractPanelServlet {

    private static final long serialVersionUID = 3008424425552738898L;
    private final static Logger log = LoggerFactory.getLogger(LayoutPanelServlet.class);

    @Override
    protected String getPanelType() {
        return "LayoutPanel";
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        try {
            LayoutEditor editor = (LayoutEditor) getEditor(name);
            Element panel = new Element("panel");

            panel.setAttribute("name", name);
            panel.setAttribute("paneltype", getPanelType());
            panel.setAttribute("height", Integer.toString(editor.getLayoutHeight()));
            panel.setAttribute("width", Integer.toString(editor.getLayoutWidth()));
            panel.setAttribute("panelheight", Integer.toString(editor.getLayoutHeight()));
            panel.setAttribute("panelwidth", Integer.toString(editor.getLayoutWidth()));
            panel.setAttribute("showtooltips", (editor.showTooltip()) ? "yes" : "no");
            panel.setAttribute("controlling", (editor.allControlling()) ? "yes" : "no");
            panel.setAttribute("xscale", Float.toString((float) editor.getXScale()));
            panel.setAttribute("yscale", Float.toString((float) editor.getYScale()));
            panel.setAttribute("mainlinetrackwidth", Integer.toString(editor.getMainlineTrackWidth()));
            panel.setAttribute("sidetrackwidth", Integer.toString(editor.getSideTrackWidth()));
            panel.setAttribute("turnoutcircles", (editor.getTurnoutCircles()) ? "yes" : "no");
            panel.setAttribute("turnoutcirclesize", Integer.toString(editor.getTurnoutCircleSize()));
            panel.setAttribute("turnoutdrawunselectedleg", (editor.getTurnoutDrawUnselectedLeg()) ? "yes" : "no");
            if (editor.getBackgroundColor() == null) {
                panel.setAttribute("backgroundcolor", LayoutEditor.colorToString(Color.lightGray));
            } else {
                panel.setAttribute("backgroundcolor", LayoutEditor.colorToString(editor.getBackgroundColor()));
            }
            panel.setAttribute("defaulttrackcolor", editor.getDefaultTrackColor());
            panel.setAttribute("defaultoccupiedtrackcolor", editor.getDefaultOccupiedTrackColor());
            panel.setAttribute("defaultalternativetrackcolor", editor.getDefaultAlternativeTrackColor());
            panel.setAttribute("defaulttextcolor", editor.getDefaultTextColor());
            panel.setAttribute("turnoutcirclecolor", editor.getTurnoutCircleColor());

            // include positionable elements
            List<Positionable> contents = editor.getContents();
            log.debug("N positionable elements: {}", contents.size());
            for (Positionable sub : contents) {
                if (sub != null) {
                    try {
                        Element e = ConfigXmlManager.elementFromObject(sub);
                        if (e != null) {
                            if ("signalmasticon".equals(e.getName())) {  //insert icon details into signalmast
                                e.addContent(getSignalMastIconsElement(e.getAttributeValue("signalmast")));
                            }
                            try {
                                e.setAttribute(JSON.ID, sub.getNamedBean().getSystemName());
                            } catch (NullPointerException ex) {
                                if (sub.getNamedBean() == null) {
                                    log.debug("{} {} does not have an associated NamedBean", e.getName(), e.getAttribute(JSON.NAME));
                                } else {
                                    log.debug("{} {} does not have a SystemName", e.getName(), e.getAttribute(JSON.NAME));
                                }
                            }
                            parsePortableURIs(e);
                            panel.addContent(e);
                        }
                    } catch (Exception ex) {
                        log.error("Error storing panel element: " + ex, ex);
                    }
                }
            }

            // include PositionablePoints
            int num = editor.pointList.size();
            log.debug("N positionablepoint elements: {}", num);
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    Object sub = editor.pointList.get(i);
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

            // include LayoutBlocks
            LayoutBlockManager tm = InstanceManager.getDefault(LayoutBlockManager.class);
            java.util.Iterator<String> iter = tm.getSystemNameList().iterator();
            SensorManager sm = InstanceManager.sensorManagerInstance();
            num = 0;
            while (iter.hasNext()) {
                String sname = iter.next();
                if (sname == null) {
                    log.error("System name null during LayoutBlock store");
                }
                LayoutBlock b = tm.getBySystemName(sname);
                if (b.getUseCount() > 0) {
                    // save only those LayoutBlocks that are in use--skip abandoned ones
                    Element elem = new Element("layoutblock").setAttribute("systemname", sname);
                    if (!b.getUserName().isEmpty()) {
                        elem.setAttribute("username", b.getUserName());
                    }
                    //don't send invalid sensors
                    if (!b.getOccupancySensorName().isEmpty()) {
                        Sensor s = sm.getSensor(b.getOccupancySensorName());
                        if (s != null) {
                            elem.setAttribute("occupancysensor", s.getSystemName()); //send systemname
                        }
                    }
                    elem.setAttribute("occupiedsense", Integer.toString(b.getOccupiedSense()));
                    elem.setAttribute("trackcolor", LayoutBlock.colorToString(b.getBlockTrackColor()));
                    elem.setAttribute("occupiedcolor", LayoutBlock.colorToString(b.getBlockOccupiedColor()));
                    elem.setAttribute("extracolor", LayoutBlock.colorToString(b.getBlockExtraColor()));
                    if (!b.getMemoryName().isEmpty()) {
                        elem.setAttribute("memory", b.getMemoryName());
                    }
                    if (!b.useDefaultMetric()) {
                        elem.addContent(new Element("metric").addContent(Integer.toString(b.getBlockMetric())));
                    }
                    //add to the panel xml
                    panel.addContent(elem);
                    num++;
                }
            }
            log.debug("N layoutblock elements: {}", num);

            // include LevelXings
            num = editor.xingList.size();
            log.debug("N levelxing elements: {}", num);
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    Object sub = editor.xingList.get(i);
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
            // include LayoutTurnouts
            num = editor.turnoutList.size();
            log.debug("N layoutturnout elements: {}", num);
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    Object sub = editor.turnoutList.get(i);
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
            num = editor.trackList.size();
            log.debug("N tracksegment elements: {}", num);
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    Object sub = editor.trackList.get(i);
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
            // include LayoutSlips
            num = editor.slipList.size();
            log.debug("N layoutSlip elements: {}", num);
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    Object sub = editor.slipList.get(i);
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
            num = editor.turntableList.size();
            log.debug("N turntable elements: {}", num);
            if (num > 0) {
                for (int i = 0; i < num; i++) {
                    Object sub = editor.turntableList.get(i);
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

            //write out formatted document
            Document doc = new Document(panel);
            XMLOutputter fmt = new XMLOutputter();
            fmt.setFormat(Format.getPrettyFormat()
                    .setLineSeparator(System.getProperty("line.separator"))
                    .setTextMode(Format.TextMode.TRIM));

            return fmt.outputString(doc);
        } catch (NullPointerException ex) {
            log.warn("Requested Layout panel [" + name + "] does not exist.");
            return "ERROR Requested panel [" + name + "] does not exist.";
        }
    }

    @Override
    protected String getJsonPanel(String name) {
        // TODO Auto-generated method stub
        return "ERROR JSON support not implemented";
    }

    @Override
    protected JComponent getPanel(String name) {
        return ((LayoutEditor) getEditor(name)).getTargetPanel();
    }
}
