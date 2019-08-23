package jmri.web.servlet.panel;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.swing.JFrame;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.panelEditor.PanelEditor;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood (C) 2016
 */
@WebServlet(name = "PanelServlet",
        urlPatterns = {
            "/panel",
            "/panel/Panel",
            "/web/showPanel.html" // redirect to /panel/ since ~ 19 Jan 2014
        })
@ServiceProvider(service = HttpServlet.class)
public class PanelServlet extends AbstractPanelServlet {

    private final static Logger log = LoggerFactory.getLogger(PanelServlet.class);

    @Override
    protected String getPanelType() {
        return "Panel"; // NOI18N
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        try {
            PanelEditor editor = (PanelEditor) getEditor(name);
            if (editor == null) {
                log.warn("Requested Panel [" + name + "] does not exist.");
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
            log.debug("Panel has {} elements", contents.size());
            for (Positionable sub : contents) {
                if (sub != null) {
                    try {
                        panel.addContent(positionableElement(sub));
                    } catch (Exception ex) {
                        log.error("Error storing panel element: {}", ex.getMessage(), ex);
                    }
                }
            }

            Document doc = new Document(panel);
            XMLOutputter out = new XMLOutputter();
            out.setFormat(Format.getPrettyFormat()
                    .setLineSeparator(System.getProperty("line.separator"))
                    .setTextMode(Format.TextMode.TRIM));

            return out.outputString(doc);
        } catch (NullPointerException ex) {
            log.warn("Requested Panel [" + name + "] does not exist.");
            return "ERROR Requested panel [" + name + "] does not exist.";
        }
    }

    @Override
    protected String getJsonPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        try {
            PanelEditor editor = (PanelEditor) getEditor(name);
            if (editor == null) {
                log.warn("Requested Panel [" + name + "] does not exist.");
                return "ERROR Requested panel [" + name + "] does not exist.";
            }

            ObjectNode root = this.mapper.createObjectNode();
            ObjectNode panel = root.putObject("panel");

            JFrame frame = editor.getTargetFrame();

            panel.put("name", name);
            panel.put("height", frame.getContentPane().getHeight());
            panel.put("width", frame.getContentPane().getWidth());
            panel.put("panelheight", frame.getContentPane().getHeight());
            panel.put("panelwidth", frame.getContentPane().getWidth());

            panel.put("showtooltips", editor.showToolTip());
            panel.put("controlling", editor.allControlling());
            if (editor.getBackgroundColor() != null) {
                ObjectNode color = panel.putObject("backgroundColor");
                color.put("red", editor.getBackgroundColor().getRed());
                color.put("green", editor.getBackgroundColor().getGreen());
                color.put("blue", editor.getBackgroundColor().getBlue());
            }

            // include contents
            log.debug("N elements: {}", editor.getContents().size());
            for (Positionable sub : editor.getContents()) {
                    try {
                        // TODO: get all panel contents as JSON
                        // I tried using JavaBean Introspection to simply build the contents using Jackson Databindings,
                        // but when a panel element has a reference to the panel or to itself as a property, this leads
                        // to infinite recursion
                        log.debug("missing code, so not processing Positionable {}", sub);
                    } catch (Exception ex) {
                        log.error("Error storing panel element: {}", sub, ex);
                    }
            }

            return this.mapper.writeValueAsString(root);
        } catch (NullPointerException ex) {
            log.warn("Requested Panel [" + name + "] does not exist.");
            return "ERROR Requested panel [" + name + "] does not exist.";
        } catch (JsonGenerationException e) {
            log.error("Error generating JSON", e);
            return "ERROR " + e.getLocalizedMessage();
        } catch (JsonMappingException e) {
            log.error("Error mapping JSON", e);
            return "ERROR " + e.getLocalizedMessage();
        } catch (IOException e) {
            log.error("IOException", e);
            return "ERROR " + e.getLocalizedMessage();
        }
    }
}
