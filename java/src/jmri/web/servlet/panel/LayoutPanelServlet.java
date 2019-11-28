package jmri.web.servlet.panel;

import edu.umd.cs.findbugs.annotations.NonNull;
import java.awt.Color;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutShape;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.util.ColorUtil;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return xml (for specified LayoutPanel) suitable for use by external clients
 *
 * @author mstevetodd -- based on PanelServlet.java by rhwood
 */
@WebServlet(name = "LayoutPanelServlet",
        urlPatterns = {"/panel/Layout"})
@ServiceProvider(service = HttpServlet.class)
public class LayoutPanelServlet extends AbstractPanelServlet {

    private final static Logger log = LoggerFactory.getLogger(LayoutPanelServlet.class);

    @Override
    protected String getPanelType() {
        return "LayoutPanel";
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        LayoutEditor editor = (LayoutEditor) getEditor(name);
        if (editor == null) {
            log.warn("Requested LayoutPanel [" + name + "] does not exist.");
            return "ERROR Requested panel [" + name + "] does not exist.";
        }
        Element panel = new Element("panel");

        panel.setAttribute("name", name);
        panel.setAttribute("paneltype", getPanelType());
        panel.setAttribute("height", Integer.toString(editor.getLayoutHeight()));
        panel.setAttribute("width", Integer.toString(editor.getLayoutWidth()));
        panel.setAttribute("panelheight", Integer.toString(editor.getLayoutHeight()));
        panel.setAttribute("panelwidth", Integer.toString(editor.getLayoutWidth()));
        panel.setAttribute("showtooltips", (editor.showToolTip()) ? "yes" : "no");
        panel.setAttribute("controlling", (editor.allControlling()) ? "yes" : "no");
        panel.setAttribute("xscale", Float.toString((float) editor.getXScale()));
        panel.setAttribute("yscale", Float.toString((float) editor.getYScale()));
        panel.setAttribute("mainlinetrackwidth", Integer.toString(editor.getMainlineTrackWidth()));
        panel.setAttribute("sidetrackwidth", Integer.toString(editor.getSidelineTrackWidth()));
        panel.setAttribute("turnoutcircles", (editor.getTurnoutCircles()) ? "yes" : "no");
        panel.setAttribute("turnoutcirclesize", Integer.toString(editor.getTurnoutCircleSize()));
        panel.setAttribute("turnoutdrawunselectedleg", (editor.isTurnoutDrawUnselectedLeg()) ? "yes" : "no");
        if (editor.getBackgroundColor() == null) {
            panel.setAttribute("backgroundcolor", ColorUtil.colorToColorName(Color.lightGray));
        } else {
            panel.setAttribute("backgroundcolor", ColorUtil.colorToColorName(editor.getBackgroundColor()));
        }
        panel.setAttribute("defaulttrackcolor", editor.getDefaultTrackColor());
        panel.setAttribute("defaultoccupiedtrackcolor", editor.getDefaultOccupiedTrackColor());
        panel.setAttribute("defaultalternativetrackcolor", editor.getDefaultAlternativeTrackColor());
        panel.setAttribute("defaulttextcolor", editor.getDefaultTextColor());
        panel.setAttribute("turnoutcirclecolor", editor.getTurnoutCircleColor());
        panel.setAttribute("turnoutcirclethrowncolor", editor.getTurnoutCircleThrownColor());
        panel.setAttribute("turnoutfillcontrolcircles", (editor.isTurnoutFillControlCircles()) ? "yes" : "no");

        // include positionable elements
        List<Positionable> contents = editor.getContents();
        log.debug("Number of positionable elements: {}", contents.size());
        for (Positionable sub : contents) {
            if (sub != null) {
                try {
                    panel.addContent(positionableElement(sub));
                } catch (Exception ex) {
                    log.error("Error storing panel element: " + ex, ex);
                }
            }
        }

        // include LayoutBlocks
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        java.util.Iterator<LayoutBlock> iter = lbm.getNamedBeanSet().iterator();
        SensorManager sm = InstanceManager.sensorManagerInstance();
        int num = 0;
        while (iter.hasNext()) {
            LayoutBlock b = iter.next();
            if (b == null) {
                log.error("LayoutBlock null during LayoutBlock store");
                continue;
            }
            if (b.getUseCount() > 0) {
                // save only those LayoutBlocks that are in use--skip abandoned ones
                Element elem = new Element("layoutblock").setAttribute("systemName", b.getSystemName());
                String uname = b.getUserName();
                if (uname != null && !uname.isEmpty()) {
                    elem.setAttribute("username", uname);
                }
                // get occupancy sensor from layoutblock if it is valid
                if (!b.getOccupancySensorName().isEmpty()) {
                    Sensor s = sm.getSensor(b.getOccupancySensorName());
                    if (s != null) {
                        elem.setAttribute("occupancysensor", s.getSystemName()); //send systemname
                    }
                    //if layoutblock has no occupancy sensor, use one from block, if it is populated
                } else {
                    if (b.getBlock() != null) {
                        Sensor s = b.getBlock().getSensor();
                        if (s != null) {
                            elem.setAttribute("occupancysensor", s.getSystemName()); //send systemname
                        }
                    }
                }

                elem.setAttribute("occupiedsense", Integer.toString(b.getOccupiedSense()));
                elem.setAttribute("trackcolor", ColorUtil.colorToColorName(b.getBlockTrackColor()));
                elem.setAttribute("occupiedcolor", ColorUtil.colorToColorName(b.getBlockOccupiedColor()));
                elem.setAttribute("extracolor", ColorUtil.colorToColorName(b.getBlockExtraColor()));
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
        log.debug("Number of layoutblock elements: {}", num);

        // include LayoutTracks
        List<LayoutTrack> layoutTracks = editor.getLayoutTracks();
        for (Object sub : layoutTracks) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    replaceUserNames(e);
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing panel LayoutTrack element: " + e);
            }
        }
        log.debug("Number of LayoutTrack elements: {}", layoutTracks.size());

        // include LayoutShapes
        List<LayoutShape> layoutShapes = editor.getLayoutShapes();
        for (Object sub : layoutShapes) {
            try {
                Element e = jmri.configurexml.ConfigXmlManager.elementFromObject(sub);
                if (e != null) {
                    panel.addContent(e);
                }
            } catch (Exception e) {
                log.error("Error storing panel LayoutShape element: " + e);
            }
        }
        log.debug("Number of LayoutShape elements: {}", layoutShapes.size());

        //write out formatted document
        Document doc = new Document(panel);
        XMLOutputter fmt = new XMLOutputter();
        fmt.setFormat(Format.getPrettyFormat()
                .setLineSeparator(System.getProperty("line.separator"))
                .setTextMode(Format.TextMode.TRIM));

        return fmt.outputString(doc);
    }

    /**
     * replace userName value of attrName with systemName for type attrType
     *
     * @param e        element to be updated
     * @param beanType bean type to use for userName lookup
     * @param attrName attribute name to replace
     *
     */
    private void replaceUserNameAttribute(@NonNull Element e, @NonNull String beanType, @NonNull String attrName) {

        String sn = "";
        Attribute a = e.getAttribute(attrName);
        if (a == null) {
            return;
        }
        String un = a.getValue();

        switch (beanType) {
            case "turnout":
                Turnout t = InstanceManager.getDefault(TurnoutManager.class).getTurnout(un);
                if (t == null) {
                    return;
                }
                sn = t.getSystemName();
                break;
            case "layoutBlock":
                LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(un);
                if (lb == null) {
                    return;
                }
                sn = lb.getSystemName();
                break;
            default:
                return;
        }
        if (!un.equals(sn)) {
            a.setValue(sn);
            log.debug("systemName '{}' replaced userName '{}' for {}", sn, un, attrName);
        }
    }

    /**
     * replace child element value of attrName with systemName for type attrType
     *
     * @param e         element to be updated
     * @param beanType  bean type to use for userName lookup
     * @param childName child element name whose text will be replaced
     *
     */
    private void replaceUserNameChild(@NonNull Element e, @NonNull String beanType, @NonNull String childName) {

        String sn = "";
        Element c = e.getChild(childName);
        if (c == null) {
            return;
        }
        String un = c.getText();

        switch (beanType) {
            case "turnout":
                Turnout t = InstanceManager.getDefault(TurnoutManager.class).getTurnout(un);
                if (t == null) {
                    return;
                }
                sn = t.getSystemName();
                break;
            default:
                return;
        }
        if (!un.equals(sn)) {
            c.setText(sn);
            log.debug("systemName '{}' replaced userName '{}' for {}", sn, un, childName);
        }
    }

    /**
     * update the element replacing username with systemname for known
     * attributes and children
     *
     * @param e element to be updated
     */
    private void replaceUserNames(Element e) {
        replaceUserNameAttribute(e, "turnout", "turnoutname");
        replaceUserNameAttribute(e, "turnout", "secondturnoutname");

        // block names for turnouts
        replaceUserNameAttribute(e, "layoutBlock", "blockname");
        replaceUserNameAttribute(e, "layoutBlock", "blockbname");
        replaceUserNameAttribute(e, "layoutBlock", "blockcname");
        replaceUserNameAttribute(e, "layoutBlock", "blockdname");

        // block names for level crossings
        replaceUserNameAttribute(e, "layoutBlock", "blocknameac");
        replaceUserNameAttribute(e, "layoutBlock", "blocknamebd");

        replaceUserNameChild(e, "turnout", "turnout");
        replaceUserNameChild(e, "turnout", "turnoutB");
    }

    @Override
    protected String getJsonPanel(String name) {
        // TODO Auto-generated method stub
        return "ERROR JSON support not implemented";
    }
}
