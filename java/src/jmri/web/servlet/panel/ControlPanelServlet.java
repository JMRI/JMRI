/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

import java.util.List;

import javax.swing.JFrame;

import jmri.configurexml.ConfigXmlManager;
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
        if (log.isDebugEnabled()) {
            log.debug("Getting " + getPanelType() + " for " + name);
        }
        try {
            ControlPanelEditor editor = (ControlPanelEditor) getEditor(name);

            Element panel = new Element("panel");

            JFrame frame = editor.getTargetFrame();
            log.info("Target Frame [" + frame.getTitle() + "]");

            panel.setAttribute("name", name);
            panel.setAttribute("height", "" + frame.getContentPane().getHeight());
            panel.setAttribute("width", "" +  frame.getContentPane().getWidth());
            panel.setAttribute("panelheight", "" + frame.getContentPane().getHeight());
            panel.setAttribute("panelwidth", "" +  frame.getContentPane().getWidth());

            panel.setAttribute("showtooltips", "" + (editor.showTooltip() ? "yes" : "no"));
            panel.setAttribute("controlling", "" + (editor.allControlling() ? "yes" : "no"));
            if (editor.getBackgroundColor() != null) {
                Element color = new Element("backgroundColor");
                color.setAttribute("red", "" + editor.getBackgroundColor().getRed());
                color.setAttribute("green", "" + editor.getBackgroundColor().getGreen());
                color.setAttribute("blue", "" + editor.getBackgroundColor().getBlue());
                panel.addContent(color);
            }

            // include contents
            List<Positionable> contents = editor.getContents();
            if (log.isDebugEnabled()) {
                log.debug("N elements: " + contents.size());
            }
            for (Positionable sub : contents) {
                if (sub != null) {
                    try {
                        Element e = ConfigXmlManager.elementFromObject(sub);
                        if (e.getName() == "signalmasticon") {  //insert icon details into signalmast
                            e.addContent(getSignalMastIconsElement(e.getAttributeValue("signalmast")));
                        }
                        if (e != null) {
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

}
