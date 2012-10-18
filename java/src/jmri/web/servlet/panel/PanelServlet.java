/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jmri.web.servlet.panel;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import jmri.configurexml.ConfigXmlManager;
import jmri.jmrit.display.Positionable;
import jmri.jmrit.display.panelEditor.PanelEditor;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 *
 * @author rhwood
 */
public class PanelServlet extends AbstractPanelServlet {

	private static final long serialVersionUID = -5898335055123037426L;

	@Override
	protected String getPanelType() {
		return "Panel";
	}

	@Override
	protected String getXmlPanel(String name) {
		if (log.isDebugEnabled()) {
			log.debug("Getting " + getPanelType() + " for " + name);
		}
		try {
			PanelEditor editor = (PanelEditor) getEditor(name);

			Element panel = new Element("panel");

			JFrame frame = editor.getTargetFrame();
			log.info("Target Frame [" + frame.getTitle() + "]");
			Dimension size = frame.getSize();
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
			log.warn("Requested Panel [" + name + "] does not exist.");
			return "ERROR Requested panel [" + name + "] does not exist.";
		}
	}

	@Override
	protected String getJsonPanel(String name) {
		if (log.isDebugEnabled()) {
			log.debug("Getting " + getPanelType() + " for " + name);
		}
		try {
			PanelEditor editor = (PanelEditor) getEditor(name);

			ObjectNode root = this.mapper.createObjectNode();
			ObjectNode panel = root.putObject("panel");

			JFrame frame = editor.getTargetFrame();
			log.info("Target Frame [" + frame.getTitle() + "]");
			Dimension size = frame.getSize();
			panel.put("name", name);
			panel.put("height", frame.getContentPane().getHeight());
			panel.put("width", frame.getContentPane().getWidth());
			panel.put("panelheight", frame.getContentPane().getHeight());
			panel.put("panelwidth", frame.getContentPane().getWidth());

			panel.put("showtooltips", editor.showTooltip());
			panel.put("controlling", editor.allControlling());
			if (editor.getBackgroundColor() != null) {
				ObjectNode color = panel.putObject("backgroundColor");
				color.put("red", editor.getBackgroundColor().getRed());
				color.put("green", editor.getBackgroundColor().getGreen());
				color.put("blue", editor.getBackgroundColor().getBlue());
			}

			// include contents
			ArrayNode contents = panel.putArray("contents");
			if (log.isDebugEnabled()) {
				log.debug("N elements: " + editor.getContents().size());
			}
			for (Positionable sub : editor.getContents()) {
				if (sub != null) {
					try {
						// TODO: get all panel contents as JSON
						// I tried using JavaBean Introspection to simply build the contents using Jackson Databindings,
						// but when a panel element has a reference to the panel or to itself as a property, this leads
						// to infinite recursion
					} catch (Exception ex) {
						log.error("Error storing panel element: " + ex, ex);
					}
				}
			}

			return this.mapper.writeValueAsString(root);
		} catch (NullPointerException ex) {
			log.warn("Requested Panel [" + name + "] does not exist.");
			return "ERROR Requested panel [" + name + "] does not exist.";
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR";
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "ERROR";
		}
	}

}
