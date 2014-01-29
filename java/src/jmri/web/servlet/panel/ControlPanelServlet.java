/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

import java.util.List;
import javax.swing.JComponent;
import javax.swing.JFrame;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmris.json.JSON;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author rhwood
 */
public class ControlPanelServlet extends AbstractPanelServlet {

    private static final long serialVersionUID = -8086671279145186127L;

    @Override
    protected String getPanelType() {
        return "ControlPanel";
    }

    @Override
    protected String getXmlPanel(String name) {
        log.debug("Getting {} for {}", getPanelType(), name);
        try {
            ControlPanelEditor editor = (ControlPanelEditor) getEditor(name);

            Element panel = new Element("panel");

            JFrame frame = editor.getTargetFrame();

            panel.setAttribute("name", name);
            panel.setAttribute("height", Integer.toString(frame.getContentPane().getHeight()));
            panel.setAttribute("width", Integer.toString(frame.getContentPane().getWidth()));
            panel.setAttribute("panelheight", Integer.toString(editor.getTargetPanel().getHeight()));
            panel.setAttribute("panelwidth", Integer.toString(editor.getTargetPanel().getWidth()));

            panel.setAttribute("showtooltips", (editor.showTooltip()) ? "yes" : "no");
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

            Document doc = new Document(panel);
            XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());

            return out.outputString(doc);
        } catch (NullPointerException ex) {
            log.warn("Requested ControlPanel [" + name + "] does not exist.");
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
        return ((ControlPanelEditor) getEditor(name)).getTargetPanel();
    }
}
