package jmri.web.servlet.panel;

import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.swing.JFrame;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.switchboardEditor.SwitchboardEditor;
import jmri.jmrit.display.switchboardEditor.BeanSwitch;
import jmri.server.json.JSON;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Egbert Broerse (C) 2017 -- based on ControlPanelServlet.java by rhwood
 */
@WebServlet(name = "SwitchboardServlet",
        urlPatterns = {"/panel/Switchboard"})
@ServiceProvider(service = HttpServlet.class)
public class SwitchboardServlet extends AbstractPanelServlet {

    private final static Logger log = LoggerFactory.getLogger(SwitchboardServlet.class);

    @Override
    protected String getPanelType() {
        return "Switchboard";
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        SwitchboardEditor editor = (SwitchboardEditor) getEditor(name);
        if (editor == null) {
            log.warn("Requested Switchboard [" + name + "] does not exist.");
            return "ERROR Requested panel [" + name + "] does not exist.";
        }

        Element panel = new Element("panel");

        JFrame frame = editor.getTargetFrame();

        panel.setAttribute("name", name);
        panel.setAttribute("height", Integer.toString(frame.getContentPane().getHeight()));
        panel.setAttribute("width", Integer.toString(frame.getContentPane().getWidth()));
        panel.setAttribute("panelheight", Integer.toString(editor.getTargetPanel().getHeight()));
        panel.setAttribute("panelwidth", Integer.toString(editor.getTargetPanel().getWidth()));
        // add more properties
        panel.setAttribute("showtooltips", (editor.showToolTip()) ? "yes" : "no");
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

        Element bgColor = new Element("backgroundColor");
        if (editor.getBackgroundColor() == null) { // set to light grey
            bgColor.setAttribute("red", Integer.toString(192));
            bgColor.setAttribute("green", Integer.toString(192));
            bgColor.setAttribute("blue", Integer.toString(192));
        } else {
            bgColor.setAttribute("red", Integer.toString(editor.getBackgroundColor().getRed()));
            bgColor.setAttribute("green", Integer.toString(editor.getBackgroundColor().getGreen()));
            bgColor.setAttribute("blue", Integer.toString(editor.getBackgroundColor().getBlue()));
        }
        panel.addContent(bgColor);

        Element text = new Element("text");
        text.setAttribute("color", editor.getDefaultTextColor());
        text.setAttribute("content", "For now, Switchboards only present buttons in JMRI WebServer.");
        panel.addContent(text);

        // include switches, Bug: how to delete the old ones?
        List<BeanSwitch> _switches = editor.getSwitches(); // call method in SwitchboardEditor
        log.debug("SwbServlet N switches: {}", _switches.size());
        for (BeanSwitch sub : _switches) {
            if (sub != null) {
                try {
                    Element e = ConfigXmlManager.elementFromObject(sub);
                    if (e != null) {
                        log.debug("element name: {}", e.getName());
                        // if (!"button".equals(e.getShape())) { // insert icon details into beanswitch
                        // do sth;
                        // }
                        try {
                            e.setAttribute("label", sub.getNameString());
                            e.setAttribute(JSON.ID, sub.getNamedBean().getSystemName());
                            if (sub.getNamedBean() == null) {
                                e.setAttribute("connected", "false");
                                log.debug("switch {} NOT connected", sub.getNameString());
                            } else {
                                e.setAttribute("connected", "true"); // activate click action via class
                            }
                        } catch (NullPointerException ex) {
                            if (sub.getNamedBean() == null) {
                                log.debug("{} {} does not have an associated NamedBean", e.getName(), e.getAttribute(JSON.NAME));
                            } else {
                                log.debug("{} {} does not have a SystemName", e.getName(), e.getAttribute(JSON.NAME));
                            }
                        }
                        // read shared attribs
                        e.setAttribute("textcolor", editor.getDefaultTextColor());
                        e.setAttribute("type", editor.getSwitchType());
                        e.setAttribute("connection", editor.getSwitchManu());
                        e.setAttribute("shape", editor.getSwitchShape());
                        e.setAttribute("columns", Integer.toString(editor.getColumns()));
                        // process and add
                        parsePortableURIs(e);
                        panel.addContent(e);
                    }
                } catch (Exception ex) {
                    log.error("Error reading xml panel element: " + ex, ex);
                }
            }
        }

        Document doc = new Document(panel);
        XMLOutputter out = new XMLOutputter();
        out.setFormat(Format.getPrettyFormat()
                .setLineSeparator(System.getProperty("line.separator"))
                .setTextMode(Format.TextMode.TRIM));

        return out.outputString(doc);
    }

    @Override
    protected String getJsonPanel(String name) {
        // TODO Auto-generated method stub
        return "ERROR JSON support not implemented";
    }

}
