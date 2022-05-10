package jmri.web.servlet.panel;

import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.swing.JFrame;

import jmri.jmrit.display.IndicatorTrackIcon;
import jmri.jmrit.display.IndicatorTurnoutIcon;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Return xml (for specified ControlPanel) suitable for use by external clients.
 * <p>
 * See JMRI Web Server - Panel Servlet Help in help/en/html/web/PanelServlet.shtml for an example description of
 * the interaction between the Web Servlets, the Web Browser and the JMRI application.
 *
 * @author Randall Wood (C) 2016
 */
@WebServlet(name = "ControlPanelServlet",
        urlPatterns = {"/panel/ControlPanel"})
@ServiceProvider(service = HttpServlet.class)
public class ControlPanelServlet extends AbstractPanelServlet {

    @Override
    protected String getPanelType() {
        return "ControlPanel";
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        ControlPanelEditor editor = (ControlPanelEditor) getEditor(name);
        if (editor == null) {
            log.warn("Requested ControlPanel [{}] does not exist.", name);
            return "ERROR Requested panel [" + name + "] does not exist.";
        }

        Element panel = new Element("panel");

        JFrame frame = editor.getTargetFrame();

        panel.setAttribute("name", name);
        panel.setAttribute("height", Integer.toString(frame.getContentPane().getHeight()));
        panel.setAttribute("width", Integer.toString(frame.getContentPane().getWidth()));
        panel.setAttribute("panelheight", Integer.toString(editor.getTargetPanel().getHeight()));
        panel.setAttribute("panelwidth", Integer.toString(editor.getTargetPanel().getWidth()));

        panel.setAttribute("showtooltips", (editor.showToolTip()) ? "yes" : "no");
        panel.setAttribute("controlling", (editor.allControlling()) ? "yes" : "no");
        if (editor.getBackgroundColor() != null) {
            Element color = new Element("backgroundColor");
            color.setAttribute("red", Integer.toString(editor.getBackgroundColor().getRed()));
            color.setAttribute("green", Integer.toString(editor.getBackgroundColor().getGreen()));
            color.setAttribute("blue", Integer.toString(editor.getBackgroundColor().getBlue()));
            panel.addContent(color);
        }

        // include contents
        List<Positionable> contents = editor.getContents();
        log.debug("N elements: {}", contents.size());
        for (Positionable sub : contents) {
            if (sub != null) {
                Element e = new Element("temp");
                try {
                    e = positionableElement(sub);
                } catch (Exception ex) {
                    log.error("Error storing panel element: {}", ex, ex);
                }
                // where required, add special stuff to positionable here to use in Web Server
                switch (e.getName()) {
                    case "indicatorturnouticon" :
                        // if separate occ.sensor was set on icon, names for sensor plus the turnout were
                        // already copied to e as part of 'contents'
                        Element elem = new Element("oblocksysname");
                        if (((IndicatorTurnoutIcon) sub).getOccBlock() != null) { // optional for CPE, not read on load
                            String itoioblock = ((IndicatorTurnoutIcon) sub).getOccBlock().getSystemName();
                            elem.addContent(itoioblock);
                            log.debug("CP-SERVLET ITOI = {}", itoioblock);
                        } else {
                            elem.addContent("none"); // NOI18N
                            log.debug("no oblocksensor configured on ITOI {}", sub.getNameString());
                        }
                        e.addContent(elem);
                        break;
                    case "indicatortrackicon" :
                        // if separate occ.sensor was set on icon, its name was already copied to e as part of 'contents'
                        elem = new Element("oblocksysname");
                        if (((IndicatorTrackIcon) sub).getOccBlock() != null) { // optional for CPE, not read on load
                            String itioblock = ((IndicatorTrackIcon) sub).getOccBlock().getSystemName();
                            elem.addContent(itioblock);
                            log.debug("CP-SERVLET ITI = {}", itioblock);
                        } else {
                            elem.addContent("none"); // NOI18N
                            log.debug("no oblocksensor configured on ITI {}", sub.getNameString());
                        }
                        e.addContent(elem);
                        break;
                    case "" :
                    default :
                        // nothing extra
                }
                panel.addContent(e);
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

    private final static Logger log = LoggerFactory.getLogger(ControlPanelServlet.class);

}
