package jmri.web.servlet.panel;

import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.swing.JFrame;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.server.json.JSON;
import jmri.util.ColorUtil;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Egbert Broerse (C) 2017 -- based on ControlPanelServlet.java by rhwood
 */
@WebServlet(name = "SwitchboardServlet",
        urlPatterns = {"/panel/Switchboard"})
public class SwitchboardServlet extends AbstractPanelServlet {

    private final static Logger log = LoggerFactory.getLogger(SwitchboardServlet.class);

    @Override
    protected String getPanelType() {
        return "Switchboard";
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        try {
            SwitchboardEditor editor = (SwitchboardEditor) getEditor(name);

            Element panel = new Element("panel");

            JFrame frame = editor.getTargetFrame();

            panel.setAttribute("name", name);
            panel.setAttribute("height", Integer.toString(frame.getContentPane().getHeight()));
            panel.setAttribute("width", Integer.toString(frame.getContentPane().getWidth()));
            panel.setAttribute("panelheight", Integer.toString(editor.getTargetPanel().getHeight()));
            panel.setAttribute("panelwidth", Integer.toString(editor.getTargetPanel().getWidth()));
            // add more properties
            panel.setAttribute("showtooltips", (editor.showTooltip()) ? "yes" : "no");
            panel.setAttribute("controlling", (editor.allControlling()) ? "yes" : "no");

            panel.setAttribute("hideunconnected", (editor.hideUnconnected()) ? "yes" : "no");
            panel.setAttribute("rangemin", Integer.toString(editor.getPanelMenuRangeMin()));
            panel.setAttribute("rangemax", Integer.toString(editor.getPanelMenuRangeMax()));
            panel.setAttribute("type", editor.getSwitchType());
            panel.setAttribute("connection", editor.getSwitchManu());
            panel.setAttribute("shape", editor.getSwitchShape());
            panel.setAttribute("columns", Integer.toString(editor.getColumns()));
            panel.setAttribute("defaulttextcolor", editor.getDefaultTextColor());
            log.debug("webserver Switchboard attribs ready");
            Element color = new Element("backgroundColor");
            if (editor.getBackgroundColor() == null) { // set to light grey
                color.setAttribute("red", Integer.toString(192));
                color.setAttribute("green", Integer.toString(192));
                color.setAttribute("blue", Integer.toString(192));
            } else {
                color.setAttribute("red", Integer.toString(editor.getBackgroundColor().getRed()));
                color.setAttribute("green", Integer.toString(editor.getBackgroundColor().getGreen()));
                color.setAttribute("blue", Integer.toString(editor.getBackgroundColor().getBlue()));
            }
            panel.addContent(color);

            // include switches
//            List<BeanSwitch> contents = editor.getSwitches(); // TODO add method to swbEditor
//            log.debug("N elements: {}", contents.size());
//            for (Positionable sub : contents) {
//                if (sub != null) {
//                    try {
//                        Element e = ConfigXmlManager.elementFromObject(sub);
//                        if (e != null) {
//                            if ("button".equals(e.getName())) {  //insert slider details into switch
//                                //e.addContent(getSignalMastIconsElement(e.getAttributeValue("signalmast")));
//                            }
//                            try {
//                                e.setAttribute(JSON.ID, sub.getNamedBean().getSystemName());
//                            } catch (NullPointerException ex) {
//                                if (sub.getNamedBean() == null) {
//                                    log.debug("{} {} does not have an associated NamedBean", e.getName(), e.getAttribute(JSON.NAME));
//                                } else {
//                                    log.debug("{} {} does not have a SystemName", e.getName(), e.getAttribute(JSON.NAME));
//                                }
//                            }
//                            parsePortableURIs(e);
//                            panel.addContent(e);
//                        }
//                    } catch (Exception ex) {
//                        log.error("Error storing panel element: " + ex, ex);
//                    }
//                }
//            }

            Document doc = new Document(panel);
            XMLOutputter out = new XMLOutputter();
            out.setFormat(Format.getPrettyFormat()
                    .setLineSeparator(System.getProperty("line.separator"))
                    .setTextMode(Format.TextMode.TRIM));

            return out.outputString(doc);
        } catch (NullPointerException ex) {
            log.warn("Requested Switchboard [" + name + "] does not exist.");
            return "ERROR Requested Switchboard [" + name + "] does not exist.";
        }
    }

    @Override
    protected String getJsonPanel(String name) {
        // TODO Auto-generated method stub
        return "ERROR JSON support not implemented";
    }
}
